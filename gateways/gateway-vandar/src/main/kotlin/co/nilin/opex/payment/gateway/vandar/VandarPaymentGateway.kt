package co.nilin.opex.payment.gateway.vandar

import co.nilin.opex.payment.gateway.vandar.data.TxResponse
import com.opex.payment.core.Gateways
import com.opex.payment.core.model.CreateInvoiceResponse
import com.opex.payment.core.model.InvoiceDTO
import com.opex.payment.core.model.InvoiceStatus
import com.opex.payment.core.model.VerifyInvoiceResponse
import com.opex.payment.core.spi.PaymentGateway
import co.nilin.opex.payment.gateway.vandar.proxy.VandarProxy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component(Gateways.Vandar)
class VandarPaymentGateway(private val proxy: VandarProxy) : PaymentGateway {

    private val logger = LoggerFactory.getLogger(VandarPaymentGateway::class.java)

    //@Value("#{environment.VANDAR_API_KEY}")
    @Value("\${app.vandar.api-key}")
    private lateinit var apiKey: String

    @Value("\${app.vandar.redirect-url}")
    private lateinit var redirectUrl: String

    override suspend fun create(invoice: InvoiceDTO): CreateInvoiceResponse {
        val response = with(invoice) {
            proxy.createTransactionToken(
                apiKey,
                amount.toLong(),
                callbackUrl,
                mobile,
                reference,
                description,
                cardNumber,
                nationalCode
            )
        }
        return CreateInvoiceResponse(response.token)
    }

    override suspend fun verify(invoice: InvoiceDTO): VerifyInvoiceResponse {
        val tx = proxy.fetchTxData(apiKey, invoice.gatewayId!!)
        validateTransaction(invoice, tx)

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

    private fun validateTransaction(invoice: InvoiceDTO, response: TxResponse) {
        if (invoice.amount != response.realAmount.toDouble() || invoice.reference != response.factorNumber)
            throw IllegalStateException("transaction data invalid")
    }

    override suspend fun createRedirectUrl(invoice: InvoiceDTO): String {
        return "$redirectUrl/${invoice.gatewayId}"
    }
}