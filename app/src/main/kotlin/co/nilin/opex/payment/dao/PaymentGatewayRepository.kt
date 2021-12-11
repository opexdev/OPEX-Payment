package co.nilin.opex.payment.dao

import co.nilin.opex.payment.model.PaymentGatewayModel
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PaymentGatewayRepository : ReactiveCrudRepository<PaymentGatewayModel, Long> {

    fun findByName(name: String): Mono<PaymentGatewayModel?>

}