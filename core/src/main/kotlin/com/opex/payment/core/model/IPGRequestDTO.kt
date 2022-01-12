package com.opex.payment.core.model

import java.time.LocalDateTime

data class IPGRequestDTO(
    val id: Long,
    val invoiceId: Long,
    val requestId: String,
    val isExpired: Boolean,
    val isPaid: Boolean,
    val createDate: LocalDateTime
)