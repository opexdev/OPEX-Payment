package co.nilin.opex.payment.dao

import co.nilin.opex.payment.model.IPGRequest
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface IPGRequestRepository : ReactiveCrudRepository<IPGRequest, Long> {

    fun findByRequestId(requestId: String): Mono<IPGRequest>


    fun findByInvoiceId(invoiceId: Long): Mono<IPGRequest>


    @Query("SELECT * FROM ipg_request WHERE invoice_id = :invoiceId AND is_expired = FALSE AND is_paid = FALSE")
    fun findOpenRequest(invoiceId: Long): Mono<IPGRequest>

}