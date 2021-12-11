package com.opex.payment.gateway.vandar.data

abstract class BaseResponse(
    val status: Int = -1,
    val error: List<String> = emptyList()
)