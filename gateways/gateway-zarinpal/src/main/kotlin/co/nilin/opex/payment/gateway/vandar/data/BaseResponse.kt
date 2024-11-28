package co.nilin.opex.payment.gateway.vandar.data

open class BaseResponse(
    val status: Int = -1,
    val errors: List<String> = emptyList()
)