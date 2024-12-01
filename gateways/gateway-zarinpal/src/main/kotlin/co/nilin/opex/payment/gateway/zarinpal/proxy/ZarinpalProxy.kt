package co.nilin.opex.payment.gateway.zarinpal.proxy

import co.nilin.opex.payment.gateway.zarinpal.data.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.opex.payment.core.model.Currency
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class ZarinpalProxy(private val client: WebClient) {

    @Value("\${app.zarinpal.ipg-url}")
    private lateinit var ipgUrl: String


    private val logger = LoggerFactory.getLogger(ZarinpalProxy::class.java)

    suspend fun createTransactionToken(
            merchantId: String,
            amount: Long,
            callbackUrl: String,
            mobile: String? = null,
            factorNumber: String? = null,
            description: String? = null,
            card: String? = null,
            nationalCode: String? = null,
            currency: Currency? = null
    ): CreateTokenResponse {
        val request =
                CreateTokenRequest(merchantId, amount, callbackUrl, currency, description, MedataData(mobile, null))
        return client.post()
                .uri("$ipgUrl/request.json")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<CreateTokenResponse>()
                .awaitSingle()
    }

    data class FetchTxRequest(@JsonProperty("merchant_id") val merchantId: String, val token: String)

    suspend fun fetchTxData(merchantId: String, token: String): TxResponse {
        return client.post()
                .uri("$ipgUrl/inquiry.json")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(FetchTxRequest(merchantId, token)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<TxResponse>()
                .awaitSingle()
    }

    data class VerifyPaymentRequest(
            @JsonProperty("merchant_id") val merchantId: String,
            val authority: String,
            val amount: Long? = null
    )

    suspend fun verifyTransaction(merchantId: String, token: String, amount: Long): VerifyResponse {
        logger.info("merchantId : $merchantId")
        logger.info("authority : $token")
        logger.info("amount : $amount")

        return client.post()
                .uri("$ipgUrl/verify.json")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(VerifyPaymentRequest(merchantId, token, amount)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<VerifyResponse>()
                .awaitSingle()
    }

}