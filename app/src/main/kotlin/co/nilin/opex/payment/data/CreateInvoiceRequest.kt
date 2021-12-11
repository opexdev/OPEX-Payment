package co.nilin.opex.payment.data

import com.opex.payment.core.model.Currency

data class CreateInvoiceRequest(
    val userId: String,
    val reference: String,
    val amount: Double,
    val callbackUrl:String,
    val currency: Currency,
    val paymentGatewayName: String?,
    val description: String?,
    val cardNumber: String?,
    val nationalCode: String?,
)
