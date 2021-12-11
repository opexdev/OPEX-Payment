package com.opex.payment.core.model

import java.time.LocalDateTime

data class Invoice(
    val id: Long,
    val userId: String,
    val reference: String,
    val amount: Double,
    val callbackUrl: String,
    val currency: Currency,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
    val status: InvoiceStatus,
    val gatewayId:String?,
    val gatewayStatus: String?,
    val description: String?,
    val cardNumber: String?,
    val mobile:String?,
    val nationalCode: String?
)