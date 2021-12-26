package co.nilin.opex.payment.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.opex.payment.core.model.Currency
import com.opex.payment.core.model.InvoiceStatus
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InvoiceResponse(
    val userId: String,
    val amount: Double,
    val callbackUrl: String,
    val currency: Currency,
    val reference: String,
    val id: String?,
    val status: InvoiceStatus?,
    val description: String?,
    val createDate: LocalDateTime
)