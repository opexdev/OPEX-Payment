package co.nilin.opex.payment.controller

import co.nilin.opex.payment.data.CancelOrderResponse
import co.nilin.opex.payment.data.RequestPaymentRequest
import co.nilin.opex.payment.data.RequestPaymentResponse
import co.nilin.opex.payment.service.PaymentService
import co.nilin.opex.payment.utils.jwtAuthentication
import co.nilin.opex.payment.utils.tryOrNull
import com.opex.payment.core.error.AppError
import com.opex.payment.core.error.AppException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/v1/payment")
class PaymentController(private val paymentService: PaymentService) {
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

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


    data class PayResponse(val paymentURL: String)

    @GetMapping("/pay/{reference}")
    suspend fun pay(@PathVariable reference: String): PayResponse {
        val url = paymentService.pay(reference)
        logger.info("ipg url : $url")
        return PayResponse(url)
    }

    @PostMapping("/verify/{ipg-token}")
    suspend fun verify(@PathVariable("ipg-token") id: String, @RequestParam status: String) {
        paymentService.verifyInvoice(id, status)
    }

    @PostMapping("/cancel/{reference}")
    suspend fun cancel(principal: Principal, @PathVariable reference: String): CancelOrderResponse {
        val invoice = paymentService.cancel(principal, reference)
        return CancelOrderResponse(invoice.reference)
    }

}