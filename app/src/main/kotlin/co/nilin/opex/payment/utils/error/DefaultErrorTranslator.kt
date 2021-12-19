package co.nilin.opex.payment.utils.error

import org.springframework.stereotype.Component

@Component
class DefaultErrorTranslator {

    fun translate(ex: AppException): ExceptionResponse {
        return DefaultExceptionResponse(
            ex.error.name,
            ex.error.code,
            ex.message ?: ex.error.message,
            ex.status ?: ex.error.status,
            ex.data,
            ex.crimeScene
        )
    }
}