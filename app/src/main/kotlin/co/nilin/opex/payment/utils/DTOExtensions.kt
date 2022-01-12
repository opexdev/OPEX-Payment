package co.nilin.opex.payment.utils

import co.nilin.opex.payment.model.IPGRequest
import co.nilin.opex.payment.model.Invoice
import com.opex.payment.core.model.IPGRequestDTO
import com.opex.payment.core.model.InvoiceDTO

fun Invoice.asInvoiceDTO() = InvoiceDTO(
    id,
    userId,
    reference,
    amount,
    callbackUrl,
    currency,
    createDate,
    updateDate,
    status,
    gatewayStatus,
    description,
    cardNumber,
    mobile,
    nationalCode,
    isNotified
)

fun IPGRequest.asIPGRequestDTO() = IPGRequestDTO(
    id!!,
    invoiceId,
    requestId,
    isExpired,
    isPaid,
    createDate
)