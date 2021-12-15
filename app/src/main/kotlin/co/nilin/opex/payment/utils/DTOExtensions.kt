package co.nilin.opex.payment.utils

import co.nilin.opex.payment.model.Invoice
import com.opex.payment.core.model.InvoiceDTO

fun Invoice.toInvoiceDTO() = InvoiceDTO(
    id,
    userId,
    reference,
    amount,
    callbackUrl,
    currency,
    createDate,
    updateDate,
    status,
    gatewayRequestId,
    gatewayStatus,
    description,
    cardNumber,
    mobile,
    nationalCode,
    isNotified
)