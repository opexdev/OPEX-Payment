package co.nilin.opex.payment.utils

import co.nilin.opex.payment.model.InvoiceModel
import com.opex.payment.core.model.Invoice

fun InvoiceModel.toInvoiceDTO() = Invoice(
    id!!,
    userId,
    reference,
    amount,
    callbackUrl,
    currency,
    createDate,
    updateDate,
    status,
    gatewayId,
    gatewayStatus,
    description,
    cardNumber,
    mobile,
    nationalCode
)