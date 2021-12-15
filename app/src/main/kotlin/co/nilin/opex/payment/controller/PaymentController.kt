package co.nilin.opex.payment.controller

import co.nilin.opex.payment.data.RequestPaymentRequest
import co.nilin.opex.payment.data.RequestPaymentResponse
import co.nilin.opex.payment.service.PaymentService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.bind.annotation.*
import java.nio.file.attribute.GroupPrincipal
import java.security.Principal
import kotlin.properties.Delegates

@RestController
@RequestMapping("/v1/payment")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping("/request")
    suspend fun createPayment(
        principal: Principal,
        @RequestBody request: RequestPaymentRequest
    ): RequestPaymentResponse {
        val data = paymentService.createNewInvoice(principal, request)
        return RequestPaymentResponse(data.invoice.reference, data.invoice.gatewayRequestId!!, data.redirectUrl)
    }

    @PostMapping("/verify/{id}")
    suspend fun verifyPayment(@PathVariable id: String, @RequestParam status: String) {
        paymentService.verifyInvoice(id, status)
    }

}