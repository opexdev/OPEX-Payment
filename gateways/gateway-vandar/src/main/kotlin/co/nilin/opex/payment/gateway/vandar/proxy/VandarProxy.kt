package co.nilin.opex.payment.gateway.vandar.proxy

import co.nilin.opex.payment.gateway.vandar.data.CreateTokenRequest
import co.nilin.opex.payment.gateway.vandar.data.CreateTokenResponse
import co.nilin.opex.payment.gateway.vandar.data.TxResponse
import co.nilin.opex.payment.gateway.vandar.data.VerifyResponse
import com.fasterxml.jackson.annotation.JsonProperty
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
class VandarProxy(private val client: WebClient) {

    @Value("\${app.vandar.ipg-url}")
    private lateinit var ipgUrl: String

    @Value("\${app.vandar.data-url}")
    private lateinit var dataUrl: String

    private val logger = LoggerFactory.getLogger(VandarProxy::class.java)

    suspend fun createTransactionToken(
        apiKey: String,
        amount: Long,
        callbackUrl: String,
        mobile: String? = null,
        factorNumber: String? = null,
        description: String? = null,
        card: String? = null,
        nationalCode: String? = null
    ): CreateTokenResponse {
        val request =
            CreateTokenRequest(apiKey, amount, callbackUrl, mobile, factorNumber, description, card, nationalCode)
        return client.post()
            .uri("$ipgUrl/send")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CreateTokenResponse>()
            .awaitSingle()
    }

    data class FetchTxRequest(@JsonProperty("api_key") val apiKey: String, val token: String)

    suspend fun fetchTxData(apiKey: String, token: String): TxResponse {
        return client.post()
            .uri("$dataUrl/2step/transaction")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(FetchTxRequest(apiKey, token)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TxResponse>()
            .awaitSingle()
    }

    suspend fun verifyTransaction(apiKey: String, token: String): VerifyResponse {
        return client.post()
            .uri("$ipgUrl/verify")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(FetchTxRequest(apiKey, token)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<VerifyResponse>()
            .awaitSingle()
    }

}