package co.nilin.opex.payment.gateway.vandar.data

abstract class BaseResponse(
    val status: Int = -1,
    val errors: List<String> = emptyList()
)