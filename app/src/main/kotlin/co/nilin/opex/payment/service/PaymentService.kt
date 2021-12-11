package co.nilin.opex.payment.service

import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.dao.PaymentGatewayRepository
import co.nilin.opex.payment.data.CreateInvoiceRequest
import co.nilin.opex.payment.model.InvoiceModel
import co.nilin.opex.payment.model.PaymentGatewayModel
import co.nilin.opex.payment.utils.toInvoiceDTO
import com.opex.payment.core.Gateways
import com.opex.payment.core.spi.PaymentGateway
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val beanFactory: BeanFactory,
    private val invoiceRepository: InvoiceRepository,
    private val gatewayRepository: PaymentGatewayRepository
) {

    suspend fun createNewInvoice(request: CreateInvoiceRequest): InvoiceModel {
        val gatewayModel = getGateway(request.paymentGatewayName)
        val service = getGatewayService(gatewayModel.name)
        val invoice = with(request) {
            InvoiceModel(
                userId,
                reference,
                amount,
                callbackUrl,
                currency,
                gatewayModel.id!!
            )
        }

        val response = service.create(invoice.toInvoiceDTO())

        invoice.gatewayId = response.gatewayId
        return invoiceRepository.save(invoice).awaitFirst()
    }

    suspend fun getGateway(name: String?): PaymentGatewayModel {
        val gateway = gatewayRepository.findByName(name ?: Gateways.Vandar)
            .awaitFirstOrNull() ?: throw IllegalStateException("gateway not found")

        if (!gateway.isEnabled)
            throw IllegalStateException("gateway is disabled")

        return gateway
    }

    suspend fun getGatewayService(name: String): PaymentGateway {
        return beanFactory.getBean<PaymentGateway>(name)
    }

}