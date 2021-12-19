package co.nilin.opex.payment.utils.error

import org.springframework.http.HttpStatus

class AppException(
    val error: AppError,
    message: String? = null,
    val status: HttpStatus? = null,
    val data: Any? = null,
    val crimeScene: Class<*>? = null
) : RuntimeException(message ?: error.message)