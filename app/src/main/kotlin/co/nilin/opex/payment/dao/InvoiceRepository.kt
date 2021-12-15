package co.nilin.opex.payment.dao

import co.nilin.opex.payment.model.Invoice
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface InvoiceRepository : ReactiveCrudRepository<Invoice, Long> {

    fun findByGatewayRequestId(gatewayRequestId: String): Mono<Invoice?>

    fun findByReference(reference: String): Mono<Invoice?>

    @Query("SELECT * FROM invoice WHERE status = 'New' AND create_date < :time")
    fun findAllUnverifiedOlderThan(time: LocalDateTime): Flux<Invoice>
}