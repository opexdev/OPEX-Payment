package com.opex.payment.core.model

import java.time.LocalDateTime

data class InvoiceDTO(
    val id: Long?,
    val userId: String,
    val reference: String,
    val amount: Double,
    val callbackUrl: String,
    val currency: Currency,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
    val status: InvoiceStatus,
    val gatewayStatus: String?,
    val description: String?,
    val cardNumber: String?,
    val mobile: String?,
    val nationalCode: String?,
    val isNotified: Boolean
)