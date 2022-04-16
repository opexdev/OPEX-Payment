package co.nilin.opex.payment.controller

import co.nilin.opex.payment.data.CancelOrderResponse
import co.nilin.opex.payment.data.RequestPaymentRequest
import co.nilin.opex.payment.data.RequestPaymentResponse
import co.nilin.opex.payment.service.PaymentService
import com.opex.payment.core.error.AppError
import com.opex.payment.core.error.AppException
import co.nilin.opex.payment.utils.jwtAuthentication
import co.nilin.opex.payment.utils.redirectTo
import co.nilin.opex.payment.utils.tryOrNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/v1/payment")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping("/request")
    suspend fun create(
        principal: Principal,
        @RequestBody request: RequestPaymentRequest
    ): RequestPaymentResponse {
        val claims = try {
            principal.jwtAuthentication().token.claims
        } catch (e: Exception) {
            throw AppException(AppError.BadRequest, "Invalid authentication")
        }

        val mobile = tryOrNull { claims["phone_number"] as String? }
        val card = tryOrNull { claims["bank_card"] as String? }
        val nationalCode = tryOrNull { claims["national_code"] as String? }

        val invoice = paymentService.createNewInvoice(principal, request, mobile, card, nationalCode)
        return RequestPaymentResponse(invoice.reference)
    }

    @GetMapping("/pay/{reference}")
    suspend fun pay(@PathVariable reference: String): ResponseEntity<*> {
        val url = paymentService.pay(reference)
        return redirectTo(url)
    }

    @PostMapping("/verify/{id}")
    suspend fun verify(@PathVariable id: String, @RequestParam status: String) {
        paymentService.verifyInvoice(id, status)
    }

    @PostMapping("/cancel/{reference}")
    suspend fun cancel(principal: Principal, @PathVariable reference: String): CancelOrderResponse {
        val invoice = paymentService.cancel(principal, reference)
        return CancelOrderResponse(invoice.reference)
    }

}