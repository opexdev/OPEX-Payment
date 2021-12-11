package co.nilin.opex.payment.data

data class VerifyOrderResponse(val status: VerifyOrderStatus)

enum class VerifyOrderStatus {
    Successful, Failed, Unknown
}
