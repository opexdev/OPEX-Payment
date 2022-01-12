package co.nilin.opex.payment.controller

import co.nilin.opex.payment.data.InvoiceResponse
import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.service.InvoiceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/v1/invoice")
class InvoiceController(private val invoiceService: InvoiceService) {

    @GetMapping
    suspend fun getAllInvoicesForUser(principal: Principal): List<InvoiceResponse> {
        return invoiceService.getUserInvoices(principal.name).map { it.asInvoiceResponse() }
    }

    @GetMapping("/open")
    suspend fun getAllOpenInvoicesForUser(principal: Principal): List<InvoiceResponse> {
        return invoiceService.getUserOpenInvoices(principal.name).map { it.asInvoiceResponse() }
    }

    @GetMapping("/{reference}")
    suspend fun getInvoice(principal: Principal, @PathVariable reference: String): InvoiceResponse {
        return invoiceService.getInvoice(principal.name, reference).asInvoiceResponse()
    }

    private fun Invoice.asInvoiceResponse() = InvoiceResponse(
        userId,
        amount,
        callbackUrl,
        currency,
        reference,
        status,
        description,
        createDate
    )

}