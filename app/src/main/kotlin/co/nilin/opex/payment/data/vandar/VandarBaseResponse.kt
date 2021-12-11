package co.nilin.opex.payment.data.vandar

abstract class VandarBaseResponse(
    val status: Int = -1,
    val error: List<String> = emptyList()
)