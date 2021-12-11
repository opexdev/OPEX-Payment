package co.nilin.opex.payment.dao

import co.nilin.opex.payment.model.ServiceProvider
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ServiceProviderRepository : ReactiveCrudRepository<ServiceProvider, Long> {

    fun findByName(name: String): Mono<ServiceProvider?>

}