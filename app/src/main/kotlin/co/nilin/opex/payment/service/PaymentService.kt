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
import co.nilin.opex.payment.utils.error.AppError
import co.nilin.opex.payment.utils.error.AppException
import co.nilin.opex.payment.utils.asInvoiceDTO
import co.nilin.opex.payment.utils.equalsAny
import com.opex.payment.core.Gateways
import com.opex.payment.core.model.InvoiceStatus
import com.opex.payment.core.spi.PaymentGateway
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
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

    @Transactional
    suspend fun createNewInvoice(
        principal: Principal,
        request: RequestPaymentRequest,
        mobile: String?,
        cardNumber: String?,
        nationalCode: String?,
    ): Invoice {
        val gatewayModel = selectGateway()
        val userOpenInvoices = invoiceRepository.findByUserIdAndStatus(principal.name, InvoiceStatus.Open)
            .collectList()
            .awaitFirstOrElse { emptyList() }

        if (userOpenInvoices.isNotEmpty())
            throw AppException(AppError.OpenPayments)

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
        val invoice = invoiceRepository.findByReference(reference)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Payment not found")

        val payInterval = Interval(2, TimeUnit.MINUTES).getLocalDateTime()
        if (invoice.lastPayAttempt != null && invoice.lastPayAttempt!! > payInterval)
            throw AppException(AppError.PaymentLocked)

        invoice.lastPayAttempt = LocalDateTime.now()
        invoice.updateDate = LocalDateTime.now()
        invoiceRepository.save(invoice).awaitFirst()

        val previousRequest = ipgRequestRepository.findOpenRequest(invoice.id!!).awaitFirstOrNull()
        if (previousRequest != null) {
            previousRequest.isExpired = true
            ipgRequestRepository.save(previousRequest).awaitFirst()
        }

        //TODO select gateway here
        val gatewayModel = gatewayRepository.findById(invoice.paymentGatewayId).awaitFirst()
        val service = getGatewayService(gatewayModel.name)
        val response = service.create(invoice.asInvoiceDTO())

        val ipgRequest = ipgRequestRepository.save(
            IPGRequest(
                invoice.id!!,
                response.gatewayId
            )
        ).awaitFirst()

        return service.createRedirectUrl(ipgRequest.asIPGRequestDTO())
    }

    @Transactional
    suspend fun verifyInvoice(requestId: String, status: String): Invoice {
        val request = ipgRequestRepository.findByRequestId(requestId)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Payment not found")

        var invoice = invoiceRepository.findById(request.invoiceId)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Payment not found")

        val gatewayModel = gatewayRepository.findById(invoice.paymentGatewayId).awaitFirst()
        val service = getGatewayService(gatewayModel.name)

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
            invoice.isNotified = opexBridgeService.notifyDeposit(invoice)
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

    private suspend fun selectGateway(name: String = Gateways.Vandar): PaymentGatewayModel {
        val gateway = gatewayRepository.findByName(name)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Gateway not found")

        if (!gateway.isEnabled)
            throw AppException(AppError.BadRequest, "Gateway is disabled")

        return gateway
    }

    private fun getGatewayService(name: String): PaymentGateway {
        return beanFactory.getBean<PaymentGateway>(name)
    }

}