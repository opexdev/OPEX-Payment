package co.nilin.opex.payment.service

import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.dao.PaymentGatewayRepository
import co.nilin.opex.payment.data.InvoiceAndUrl
import co.nilin.opex.payment.data.RequestPaymentRequest
import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.model.PaymentGatewayModel
import co.nilin.opex.payment.utils.equalsAny
import co.nilin.opex.payment.utils.error.AppError
import co.nilin.opex.payment.utils.error.AppException
import co.nilin.opex.payment.utils.toInvoiceDTO
import com.opex.payment.core.Gateways
import com.opex.payment.core.model.InvoiceStatus
import com.opex.payment.core.spi.PaymentGateway
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.Principal
import java.time.LocalDateTime

@Service
class PaymentService(
    private val beanFactory: BeanFactory,
    private val opexBridgeService: OpexBridgeService,
    private val invoiceRepository: InvoiceRepository,
    private val gatewayRepository: PaymentGatewayRepository
) {

    @Transactional
    suspend fun createNewInvoice(principal: Principal, request: RequestPaymentRequest): InvoiceAndUrl {
        val gatewayModel = getGateway(request.paymentGatewayName)
        val service = getGatewayService(gatewayModel.name)
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

        val response = service.create(invoice.toInvoiceDTO())

        invoice.gatewayRequestId = response.gatewayId
        val saved = invoiceRepository.save(invoice).awaitFirst()
        val redirect = service.createRedirectUrl(saved.toInvoiceDTO())
        return InvoiceAndUrl(saved, redirect)
    }

    suspend fun verifyInvoice(requestId: String, status: String): Invoice {
        var invoice = invoiceRepository.findByGatewayRequestId(requestId)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Payment not found")

        val gatewayModel = gatewayRepository.findById(invoice.paymentGatewayId).awaitFirst()
        val service = getGatewayService(gatewayModel.name)

        if (invoice.status.equalsAny(InvoiceStatus.Expired, InvoiceStatus.Failed, InvoiceStatus.Undefined))
            throw AppException(AppError.VerificationNotAllowed)

        if (invoice.status == InvoiceStatus.Done)
            throw AppException(AppError.AlreadyVerified)

        if (invoice.status == InvoiceStatus.New) {
            val response = service.verify(invoice.toInvoiceDTO())
            if (response.status == InvoiceStatus.Undefined)
                throw AppException(AppError.VerificationFailed)

            invoice.status = response.status
            invoice.updateDate = LocalDateTime.now()
            invoice = invoiceRepository.save(invoice).awaitFirst()
        }

        if (!invoice.isNotified) {
            invoice.isNotified = opexBridgeService.notifyDeposit(invoice)
            invoice.updateDate = LocalDateTime.now()
            invoice = invoiceRepository.save(invoice).awaitFirst()
        }

        return invoice
    }

    private suspend fun getGateway(name: String?): PaymentGatewayModel {
        val gateway = gatewayRepository.findByName(name ?: Gateways.Vandar)
            .awaitFirstOrNull() ?: throw AppException(AppError.NotFound, "Gateway not found")

        if (!gateway.isEnabled)
            throw AppException(AppError.BadRequest, "Gateway is disabled")

        return gateway
    }

    private fun getGatewayService(name: String): PaymentGateway {
        return beanFactory.getBean<PaymentGateway>(name)
    }

}