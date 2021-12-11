package com.opex.payment.gateway.jibit.data

import java.util.*

data class OrderResponse(
    val id: String?,
    val amount: Long,
    val currency: Currency?,
    val referenceNumber: String?,
    val userIdentifier: String?,
    val callbackUrl: String?,
    val description: String?,
    val additionalData: String?,
    val status: OrderStatus?,
    val initPayerIp: String?,
    val redirectPayerIp: String?,
    val createdAt: Date?,
    val modifiedAt: Date?,
    val expirationDate: Date?,
    val payerCard: String?,
    val nationalCode: String?,
)

enum class OrderStatus {
    SUCCESS, FAILED, UNKNOWN, EXPIRED, IN_PROGRESS
}
