package co.nilin.opex.payment.data

data class CancelOrderResponse(
    val reference: String,
    val canceled: Boolean = true
)