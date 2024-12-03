package co.nilin.opex.payment.service

import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.model.Invoice
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class SaveInvoiceTx(private val invoiceRepository: InvoiceRepository) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    suspend fun forceInvoiceUpdate(invoice: Invoice): Invoice {

        return invoiceRepository.save(invoice).awaitFirst()

    }
}
