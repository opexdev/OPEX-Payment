package co.nilin.opex.payment.dao

import co.nilin.opex.payment.model.Invoice
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository : ReactiveCrudRepository<Invoice, Long>