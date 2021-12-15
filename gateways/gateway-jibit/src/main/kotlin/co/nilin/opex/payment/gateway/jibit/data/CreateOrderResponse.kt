package co.nilin.opex.payment.gateway.jibit.data

data class CreateOrderResponse(
    val orderIdentifier: String,
    val referenceNumber: String,
    val pspSwitchingUrl: String
)
