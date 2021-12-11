package co.nilin.opex.payment.proxy

import co.nilin.opex.payment.data.*
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
class JibitPPGProxy(private val client: WebClient) {

    private val logger = LoggerFactory.getLogger(JibitPPGProxy::class.java)

    @Value("app.jibit-url")
    private lateinit var baseUrl: String

    suspend fun generateToken(apiKey: String, secretKey: String): GenerateTokenResponse {
        logger.info("fetching token")
        return client.post()
            .uri("$baseUrl/tokens/generate")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(GenerateTokenRequest(apiKey, secretKey)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<GenerateTokenResponse>()
            .awaitSingle()
    }

    suspend fun refreshToken(accessToken: String, refreshToken: String): GenerateTokenResponse {
        logger.info("refreshing token")
        return client.post()
            .uri("$baseUrl/tokens/refresh")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(RefreshTokenRequest(accessToken, refreshToken)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<GenerateTokenResponse>()
            .awaitSingle()
    }

    suspend fun createOrder(accessToken: String, request: CreateOrderRequest): CreateOrderResponse {
        logger.info("creating order")
        return client.post()
            .uri("$baseUrl/orders")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization: Bearer $accessToken")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CreateOrderResponse>()
            .awaitSingle()
    }

    suspend fun verifyOrder(accessToken: String, orderIdentifier: String): VerifyOrderResponse {
        logger.info("verifying order $orderIdentifier")
        return client.get()
            .uri("$baseUrl/orders/$orderIdentifier/verify")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization: Bearer $accessToken")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<VerifyOrderResponse>()
            .awaitSingle()
    }

    suspend fun getOrder(accessToken: String, orderIdentifier: String): OrderResponse {
        logger.info("verifying order $orderIdentifier")
        return client.get()
            .uri("$baseUrl/orders/$orderIdentifier")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization: Bearer $accessToken")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<OrderResponse>()
            .awaitSingle()
    }

}