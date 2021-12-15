package co.nilin.opex.payment.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionController {

    private val logger = LoggerFactory.getLogger(ExceptionController::class.java)

    @ExceptionHandler(Throwable::class)
    fun handle(e: Throwable): String {
        logger.error("Generic error", e)
        return "{error:${e.message}}"
    }

}