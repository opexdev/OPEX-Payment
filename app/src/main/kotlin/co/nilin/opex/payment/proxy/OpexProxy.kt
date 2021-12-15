package co.nilin.opex.payment.proxy

import com.opex.payment.core.model.Currency
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.body
import reactor.core.publisher.Mono

@Component
class OpexProxy(private val client: WebClient) {

    data class DepositRequest(val userId: String, val amount: Double, val currency: Currency)

    suspend fun deposit(userId: String, amount: Double, currency: Currency) {
        client.post()
            .uri("")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(DepositRequest(userId, amount, currency)))
            .retrieve()
            .awaitBody<String>()
    }

}