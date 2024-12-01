package co.nilin.opex.payment.service

import co.nilin.opex.payment.dao.IPGRequestRepository
import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.dao.PaymentGatewayRepository
import co.nilin.opex.payment.data.RequestPaymentRequest
import co.nilin.opex.payment.model.IPGRequest
import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.model.PaymentGatewayModel
import co.nilin.opex.payment.utils.Interval
import co.nilin.opex.payment.utils.asIPGRequestDTO
import co.nilin.opex.payment.utils.asInvoiceDTO
import co.nilin.opex.payment.utils.equalsAny
import com.opex.payment.core.error.AppError
import com.opex.payment.core.error.AppException
import com.opex.payment.core.model.InvoiceStatus
import com.opex.payment.core.spi.PaymentGateway
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.Principal
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class PaymentService(
    private val beanFactory: BeanFactory,
    private val opexBridgeService: OpexBridgeService,
    private val invoiceRepository: InvoiceRepository,
    private val gatewayRepository: PaymentGatewayRepository,
    private val ipgRequestRepository: IPGRequestRepository,
) {

    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    @Transactional
    suspend fun createNewInvoice(
        principal: Principal,
        request: RequestPaymentRequest,
        mobile: String?,
        cardNumber: String?,
        nationalCode: String?,
    ): Invoice {
        //todo change in gateway selection
        val gatewayModel = selectGateway(name = null)
//        val userOpenInvoices = invoiceRepository.findByUserIdAndStatus(principal.name, InvoiceStatus.Open)
//            .collectList()
//            .awaitFirstOrElse { emptyList() }
//
//        if (userOpenInvoices.isNotEmpty())
//            throw AppException(AppError.OpenPayments)

        val invoice = with(request) {
            Invoice(
                principal.name,
                amount,
                callbackUrl,
                currency,
                gatewayModel.id!!,
                cardNumber = cardNumber,
                description = description,
                mobile = mobile,
                nationalCode = nationalCode
            )
        }

        return invoiceRepository.save(invoice).awaitFirst()
    }

    /**
     * @return url for redirecting to IPG
     */
    @Transactional
    suspend fun pay(reference: String): String {
        //TODO Where to redirect the error when invoice is null?
        val invoice = invoiceRepository.findByReference(reference)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Payment not found")

//        val payInterval = Interval(2, TimeUnit.MINUTES).getLocalDateTime()
//        if (invoice.lastPayAttempt != null && invoice.lastPayAttempt!! > payInterval) {
//            logger.error("pay(): Invoice ${invoice.reference} locked")
//            return createErrorRedirect(AppException(AppError.PaymentLocked), invoice)
//        }
//
//        if (invoice.status != InvoiceStatus.Open) {
//            logger.error("pay(): Invoice ${invoice.reference} not allowed")
//            return createErrorRedirect(AppException(AppError.PaymentNotAllowed), invoice)
//        }
//
//        invoice.lastPayAttempt = LocalDateTime.now()
//        invoice.updateDate = LocalDateTime.now()
//        invoiceRepository.save(invoice).awaitFirst()

        val previousRequest = ipgRequestRepository.findOpenRequest(invoice.id!!).awaitFirstOrNull()
        if (previousRequest != null) {
            logger.info("Expiring previous request for invoice ${invoice.reference}")
            previousRequest.isExpired = true
            ipgRequestRepository.save(previousRequest).awaitFirst()
        }

        //TODO select gateway here
        val gatewayModel = gatewayRepository.findById(invoice.paymentGatewayId).awaitFirst()
        val service = getGatewayService(gatewayModel.name)
        val response = try {
            service.create(invoice.asInvoiceDTO())
        } catch (ex: AppException) {
            logger.error("Error creating ipg request for invoice ${invoice.reference}", ex)
            return createErrorRedirect(ex, invoice)
            throw AppException(AppError.Error)
        }

        val ipgRequest = ipgRequestRepository.save(
            IPGRequest(
                invoice.id!!,
                response.gatewayId
            )
        ).awaitFirst()

        return service.createRedirectUrl(ipgRequest.asIPGRequestDTO())
    }

    @Transactional
    suspend fun verifyInvoice(ipgToken: String, status: String): Invoice {

        val request = ipgRequestRepository.findByRequestId(ipgToken)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Payment not found")

        var invoice = invoiceRepository.findById(request.invoiceId)
                .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Payment not found")


        val gatewayModel = gatewayRepository.findById(invoice.paymentGatewayId).awaitFirst()
        val service = getGatewayService(gatewayModel.name)

        logger.info("invoice status : ${invoice.status}")


        if (invoice.status.equalsAny(InvoiceStatus.Expired, InvoiceStatus.Canceled, InvoiceStatus.Failed))
            throw AppException(AppError.PaymentNotAllowed)

        if (invoice.status == InvoiceStatus.Done)
            throw AppException(AppError.AlreadyVerified)

        if (invoice.status == InvoiceStatus.Open) {
            val response = service.verify(invoice.asInvoiceDTO(), request.asIPGRequestDTO())
            if (response.status == InvoiceStatus.Undefined)
                throw AppException(AppError.VerificationFailed)

            request.isPaid = response.status == InvoiceStatus.Done
            ipgRequestRepository.save(request).awaitFirst()

            invoice.status = response.status
            invoice.updateDate = LocalDateTime.now()
            invoice = invoiceRepository.save(invoice).awaitFirst()
        }

        if (invoice.status == InvoiceStatus.Done && !invoice.isNotified) {
            invoice.isNotified = try {
                opexBridgeService.notifyDeposit(invoice)
            } catch (e: Exception) {
                logger.error("Failed to notify the core system for invoice ${invoice.reference}", e)
                false
            }
            invoice.updateDate = LocalDateTime.now()
            invoice = invoiceRepository.save(invoice).awaitFirst()
        }

        return invoice
    }

    suspend fun cancel(principal: Principal, reference: String): Invoice {
        val invoice = invoiceRepository.findByReference(reference).awaitFirstOrNull()
            ?: throw AppException(AppError.NotFound, "Payment not found")

        if (principal.name != invoice.userId)
            throw AppException(AppError.Forbidden)

        invoice.apply {
            status = InvoiceStatus.Canceled
            updateDate = LocalDateTime.now()
        }
        return invoiceRepository.save(invoice).awaitFirst()
    }

    private suspend fun selectGateway(name: String?): PaymentGatewayModel {
        val gateway = name?.let { gatewayRepository.findByName(name)?.awaitFirstOrNull() }
            ?: gatewayRepository.findAll()?.awaitFirstOrNull()
        if (gateway?.isEnabled == true)
            return gateway
        else
            throw AppException(AppError.BadRequest, "Gateway is disabled")
    }

    private fun getGatewayService(name: String): PaymentGateway {
        return beanFactory.getBean<PaymentGateway>(name)
    }

    private fun createErrorRedirect(ex: AppException, invoice: Invoice): String {
        val url = invoice.callbackUrl
        return "$url?payment_status=FAILED&status_code=${ex.status.value()}&error_code=${ex.error.code}&error=${ex.error.name}&message=${ex.message}"
    }

}