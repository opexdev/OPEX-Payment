package co.nilin.opex.payment.gateway.jibit.data

data class VerifyOrderResponse(val status: VerifyOrderStatus)

enum class VerifyOrderStatus {
    Successful, Failed, Unknown
}
