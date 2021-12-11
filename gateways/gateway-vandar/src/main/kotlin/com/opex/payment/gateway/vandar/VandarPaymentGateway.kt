package com.opex.payment.gateway.vandar

import com.opex.payment.core.Gateways
import com.opex.payment.core.model.CreateInvoiceResponse
import com.opex.payment.core.model.Invoice
import com.opex.payment.core.model.InvoiceStatus
import com.opex.payment.core.model.VerifyInvoiceResponse
import com.opex.payment.core.spi.PaymentGateway
import com.opex.payment.gateway.vandar.proxy.VandarProxy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier(Gateways.Vandar)
class VandarPaymentGateway(private val proxy: VandarProxy) : PaymentGateway {

    private val apiKey = ""

    override suspend fun create(invoice: Invoice): CreateInvoiceResponse {
        val response = with(invoice) {
            proxy.createTransactionToken(
                apiKey,
                amount.toLong(),
                callbackUrl,
                mobile,
                reference,
                description,
                cardNumber
            )
        }
        return CreateInvoiceResponse(response.token)
    }

    override suspend fun verify(invoice: Invoice): VerifyInvoiceResponse {
        val response = proxy.verifyTransaction(apiKey, invoice.gatewayId!!)
        val status = when (response.status) {
            0 -> throw IllegalArgumentException("")
            1 -> InvoiceStatus.Done
            2 -> InvoiceStatus.Done
            3 -> InvoiceStatus.Expired
            else -> InvoiceStatus.Failed
        }
        return VerifyInvoiceResponse(status)
    }
}