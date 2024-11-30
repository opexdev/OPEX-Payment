package co.nilin.opex.payment.gateway.zarinpal

import co.nilin.opex.payment.gateway.zarinpal.data.BaseResponse
import co.nilin.opex.payment.gateway.zarinpal.data.TxResponse
import co.nilin.opex.payment.gateway.zarinpal.data.VerifyResponse
import co.nilin.opex.payment.gateway.zarinpal.proxy.ZarinpalProxy
import com.fasterxml.jackson.databind.ObjectMapper
import com.opex.payment.core.Gateways
import com.opex.payment.core.error.AppError
import com.opex.payment.core.error.AppException
import com.opex.payment.core.model.*
import com.opex.payment.core.spi.PaymentGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component(Gateways.Zarinpal)
class ZarinpalPaymentGateway(private val proxy: ZarinpalProxy, private val mapper: ObjectMapper) : PaymentGateway {

    private val logger = LoggerFactory.getLogger(ZarinpalPaymentGateway::class.java)

    @Value("\${app.zarinpal.api-key}")
    private lateinit var apiKey: String

    @Value("\${app.zarinpal.redirect-url}")
    private lateinit var redirectUrl: String

    override suspend fun create(invoice: InvoiceDTO): CreateInvoiceResponse {
        val response = try {
            with(invoice) {
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
        } catch (e: WebClientResponseException) {
            val body = mapper.readValue(e.responseBodyAsByteArray, BaseResponse::class.java)
            val errors = body.errors.map { String(it.toByteArray(), Charsets.UTF_8) }
            logger.error("status: ${e.statusCode} - errors: $errors")
            logger.error("create() error", e)
            throw AppException(AppError.UnknownGatewayError)
        }
        return CreateInvoiceResponse(response.data?.authority!!)
    }

    override suspend fun verify(invoice: InvoiceDTO, request: IPGRequestDTO): VerifyInvoiceResponse {
//        val tx = proxy.fetchTxData(apiKey, request.requestId)
//        validateTransaction(invoice, tx)

        val status = try {
            val response = proxy.verifyTransaction(apiKey, request.requestId,invoice.amount.toLong())
            if (response.data.code == 100) InvoiceStatus.Done else InvoiceStatus.Undefined
        } catch (e: WebClientResponseException) {
            val body = mapper.readValue(e.responseBodyAsByteArray, VerifyResponse::class.java)
            val errors = body.errors.map { String(it.toByteArray(), Charsets.UTF_8) }
            logger.error("status: ${e.statusCode} - errors: $errors")
            logger.error("verify() error", e)

            when (body.data.code) {
                100 -> InvoiceStatus.Done
                101 -> InvoiceStatus.Done
                else -> InvoiceStatus.Failed
            }
        }
        return VerifyInvoiceResponse(status)
    }

//    private fun validateTransaction(invoice: InvoiceDTO, response: TxResponse) {
//        if (invoice.amount != response.data.() || invoice.reference != response.factorNumber)
//            throw IllegalStateException("transaction data invalid")
//    }

    override suspend fun createRedirectUrl(request: IPGRequestDTO): String {
        return "$redirectUrl/${request.requestId}"
    }
}