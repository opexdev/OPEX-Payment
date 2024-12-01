package co.nilin.opex.payment.proxy

import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.wallet.core.model.otc.LoginRequest
import co.nilin.opex.wallet.core.model.otc.LoginResponse
import com.opex.payment.core.model.Currency
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.body
import reactor.core.publisher.Mono
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class OpexProxy(@Qualifier("loadBalanced") private val client: WebClient,
                private val environment: Environment
) {

    @Value("\${app.opex.wallet-url}")
    private lateinit var walletUrl: String


    @Value("\${app.opex.auth-url}")
    private lateinit var baseUrl: String


    @Value("\${app.opex.client-id}")
    private lateinit var clientId: String

    @Value("\${app.opex.client-secret}")
    private lateinit var clientSecret: String
    suspend fun extractToken(): String? {
        if (environment.activeProfiles.contains("otc"))
            return getToken(LoginRequest(clientId, clientSecret)).data.accessToken
        return null
    }

    suspend fun getToken(loginRequest: LoginRequest): LoginResponse {
        return client.post()
                .uri(URI.create("${baseUrl}/api/v1/login"))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .body(
                        BodyInserters.fromFormData("mobile", loginRequest.clientId)
                                .with("password", loginRequest.clientSecret)
                )
                .retrieve()
                .onStatus({ t -> t.isError }, {
                    it.createException()
                })
                .bodyToMono(typeRef<LoginResponse>())
                .awaitFirst()
    }

    data class DepositRequest(
            val userId: String,
            val amount: Double,
            val currency: Currency,
            val reference: String,
            val description: String?
    )

    data class DepositResponse(val success: Boolean)

    suspend fun deposit(invoice: Invoice): DepositResponse {
        val request = with(invoice) { DepositRequest(userId, amount, currency, reference, description) }
        val token = extractToken()
        return client.post()
                .uri("$walletUrl/payment/internal/deposit")
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        token?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(request))
                .retrieve()
                .awaitBody()
    }

}