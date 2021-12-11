package com.opex.payment.gateway.vandar.proxy

import com.opex.payment.gateway.vandar.data.BaseResponse
import com.opex.payment.gateway.vandar.data.CreateTokenResponse
import com.opex.payment.gateway.vandar.data.TxResponse
import com.opex.payment.gateway.vandar.data.VerifyResponse
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.lang.StringBuilder

@Component
class VandarProxy(private val client: WebClient) {

    @Value("app.vandar.ipg-url")
    private lateinit var ipgUrl: String

    @Value("app.vandar.data-url")
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
        comment: String? = null
    ): CreateTokenResponse {
        val params = StringBuilder().apply {
            append("api_key=$apiKey")
            append("&amount=$amount")
            append("&callback_url=$callbackUrl")
            mobile?.let { append("&mobile_number=$mobile") }
            factorNumber?.let { append("&factorNumber=$factorNumber") }
            description?.let { append("&description=$description") }
            card?.let { append("&valid_card_number=$card") }
            comment?.let { append("&comment=$comment") }
            toString()
        }

        return client.post()
            .uri("$ipgUrl/send?$params")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CreateTokenResponse>()
            .awaitSingle()
    }

    data class FetchTxRequest(val apiKey: String, val token: String)

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