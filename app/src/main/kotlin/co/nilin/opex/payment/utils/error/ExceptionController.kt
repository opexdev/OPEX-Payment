package co.nilin.opex.payment.utils.error

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.opex.payment.core.error.AppError
import com.opex.payment.core.error.AppException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ServerWebInputException
import java.nio.charset.StandardCharsets
import java.util.*

@RestControllerAdvice
class ExceptionController(
    private val mapper: ObjectMapper,
    private val translator: DefaultErrorTranslator
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WebClientErrorResponse(
        val timestamp: Date?,
        val path: String?,
        val status: Int?,
        val error: String?,
        val message: String?,
        val code: Int?
    )

    private val logger = LoggerFactory.getLogger(ExceptionController::class.java)

    @ExceptionHandler(AppException::class)
    fun handle(e: AppException): ResponseEntity<ExceptionResponse> {
        val error = translator.translate(e)
        if (error is DefaultExceptionResponse)
            logger.error("Opex error happened at ${e.crimeScene?.name}", e)
        else
            logger.error("Opex error", e)
        return response(error)
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handle(e: WebClientResponseException): ResponseEntity<ExceptionResponse> {
        logger.error("Webclient error", e)
        return try {
            val body = mapper.readValue(
                e.responseBodyAsByteArray.toString(StandardCharsets.UTF_8),
                WebClientErrorResponse::class.java
            )

            val opexError = AppError.findByCode(body.code)
            val er = translator.translate(AppException(opexError ?: AppError.InternalServerError))
            response(er)
        } catch (ex: Exception) {
            val opEx = AppException(AppError.InternalServerError)
            val er = translator.translate(opEx)
            response(er)
        }
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleMissingServletRequestParameter(ex: ServerWebInputException): ResponseEntity<ExceptionResponse> {
        logger.error("Web input error", ex)
        val name = ex.methodParameter?.parameterName

        val error = AppError.InvalidRequestParam
        val er = translator.translate(
            AppException(
                error,
                String.format(error.message!!, name)
            )
        )
        return response(er)
    }

    @ExceptionHandler(Throwable::class)
    fun handle(e: Throwable): ResponseEntity<ExceptionResponse> {
        logger.error("Generic error", e)
        val opexException =
            AppException(status = HttpStatus.INTERNAL_SERVER_ERROR, error = AppError.InternalServerError)
        val error = translator.translate(opexException)
        return response(error)
    }

    private fun response(er: ExceptionResponse): ResponseEntity<ExceptionResponse> =
        ResponseEntity.status(er.status).body(er)

}