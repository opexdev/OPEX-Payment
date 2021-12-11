package com.opex.payment.gateway.vandar.data

data class VerifyResponse(
    val amount: String,
    val realAmount: Long,
    val wage: String,
    val transId: Long,
    val factorNumber: String?,
    val mobile: String?,
    val description: String?,
    val cardNumber: String?,
    val paymentDate: String?,
    val cid: String?,
    val message: String?
) : BaseResponse()