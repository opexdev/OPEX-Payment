package co.nilin.opex.payment.data

import com.opex.payment.core.model.Currency

data class RequestPaymentRequest(
    val amount: Double,
    val callbackUrl:String,
    val currency: Currency,
    val description: String?,
    val mobile: String?,
    val cardNumber: String?,
    val nationalCode: String?,
)
