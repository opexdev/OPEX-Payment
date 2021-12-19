package co.nilin.opex.payment.proxy

import co.nilin.opex.payment.model.Invoice
import com.opex.payment.core.model.Currency
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.body
import reactor.core.publisher.Mono

@Component
class OpexProxy(private val client: WebClient) {

    @Value("\${app.opex.wallet-url}")
    private lateinit var walletUrl: String

    data class DepositRequest(
        val userId: String,
        val amount: Double,
        val currency: Currency,
        val reference: String,
        val description: String?
    )

    data class DepositResponse(val success: Boolean)

    suspend fun deposit(invoice: Invoice): DepositResponse {
        val request = with(invoice) {
            DepositRequest(userId, amount, currency, reference, description)
        }
        return client.post()
            .uri("$walletUrl/payment/internal/deposit")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request))
            .retrieve()
            .awaitBody()
    }

}