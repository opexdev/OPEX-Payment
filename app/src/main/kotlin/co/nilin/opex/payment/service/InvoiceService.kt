package co.nilin.opex.payment.service

import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.utils.error.AppError
import co.nilin.opex.payment.utils.error.AppException
import com.opex.payment.core.model.InvoiceStatus
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class InvoiceService(private val invoiceRepository: InvoiceRepository) {

    suspend fun getUserInvoices(userId: String): List<Invoice> {
        return invoiceRepository.findAllByUserId(userId).collectList().awaitFirstOrElse { emptyList() }
    }

    suspend fun getUserOpenInvoices(userId: String): List<Invoice> {
        return invoiceRepository.findByUserIdAndStatus(userId, InvoiceStatus.Open)
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    suspend fun getInvoice(userId: String, reference: String, limitAccess: Boolean = false): Invoice {
        val invoice = invoiceRepository.findByReference(reference).awaitFirstOrNull()
            ?: throw AppException(AppError.NotFound)

        if (invoice.userId != userId && limitAccess)
            throw AppException(AppError.Forbidden)

        return invoice
    }

}