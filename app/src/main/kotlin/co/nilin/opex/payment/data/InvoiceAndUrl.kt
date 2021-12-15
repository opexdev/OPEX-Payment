package co.nilin.opex.payment.data

import co.nilin.opex.payment.model.Invoice

data class InvoiceAndUrl(
    val invoice:Invoice,
    val redirectUrl:String,
)