package co.nilin.opex.payment.utils.error

import org.springframework.http.HttpStatus

enum class AppError(val code: Int, val message: String?, val status: HttpStatus) {

    Error(1000, "Generic error", HttpStatus.INTERNAL_SERVER_ERROR),
    InternalServerError(1001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BadRequest(1002, "Bad request", HttpStatus.BAD_REQUEST),
    UnAuthorized(1003, "Unauthorized", HttpStatus.UNAUTHORIZED),
    Forbidden(1004, "Forbidden", HttpStatus.FORBIDDEN),
    NotFound(1005, "Not found", HttpStatus.NOT_FOUND),
    InvalidRequestParam(1020, "Parameter '%s' is either missing or invalid", HttpStatus.BAD_REQUEST),

    AlreadyVerified(13000, "This payment is already verified", HttpStatus.BAD_REQUEST),
    VerificationFailed(13001, "Unable to verify", HttpStatus.INTERNAL_SERVER_ERROR),
    VerificationNotAllowed(13002, "Verification is not allowed for this payment", HttpStatus.FORBIDDEN),
    PaymentNotAllowed(13003, "Payment is not allowed for this payment", HttpStatus.FORBIDDEN),
    OpenPayments(13004, "Found open payments for user. Finish or cancel open payments", HttpStatus.BAD_REQUEST),
    PaymentLocked(13005, "Payment is locked for this invoice. Please try again later", HttpStatus.BAD_REQUEST);

    companion object {
        fun findByCode(code: Int?): AppError? {
            code ?: return null
            return values().find { it.code == code }
        }
    }

}