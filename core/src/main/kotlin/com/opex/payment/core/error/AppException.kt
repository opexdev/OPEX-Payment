package com.opex.payment.core.error

import org.springframework.http.HttpStatus

class AppException(
    val error: AppError,
    message: String? = null,
    val status: HttpStatus = error.status,
    val data: Any? = null,
    val crimeScene: Class<*>? = null
) : RuntimeException(message ?: error.message)