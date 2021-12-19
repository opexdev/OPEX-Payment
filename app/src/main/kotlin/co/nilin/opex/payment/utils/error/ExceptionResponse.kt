package co.nilin.opex.payment.utils.error

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus

abstract class ExceptionResponse(@JsonIgnore val status: HttpStatus)