package com.opex.payment.gateway.jibit.data

data class CreateOrderRequest(
    val amount: Long, // The amount of order
    val referenceNumber: String, // A string value from merchant side to trace order in our system
    val userIdentifier: String, // Identifies the user in the merchant system. It can be mobile number
    val callbackUrl: String,
    val currency: Currency = Currency.RIALS, // Represents the currency type. Possible values are TOMAN, RIALS
    val description: String? = null,
    val additionalData: String? = null,
    val payerCardNumber: String? = null,
    val nationalCode: String? = null,
    val forcePayerCardNumber: Boolean = false,
)


