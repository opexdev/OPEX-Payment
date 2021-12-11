package co.nilin.opex.payment.data

data class CreateOrderResponse(
    val orderIdentifier: String,
    val referenceNumber: String,
    val pspSwitchingUrl: String
)
