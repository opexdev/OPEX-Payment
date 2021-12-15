package co.nilin.opex.payment.data

data class RequestPaymentResponse(val reference: String, val id: String, val redirectUrl: String)