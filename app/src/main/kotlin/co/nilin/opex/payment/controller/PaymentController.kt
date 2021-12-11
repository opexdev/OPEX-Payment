package co.nilin.opex.payment.controller

import co.nilin.opex.payment.data.CreateInvoiceRequest
import co.nilin.opex.payment.service.PaymentService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping("/new")
    suspend fun createPayment(@RequestBody request: CreateInvoiceRequest) {
        paymentService.createNewInvoice(request)
    }

}