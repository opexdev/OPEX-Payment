package co.nilin.opex.payment.service

import co.nilin.opex.payment.model.Invoice

interface PaymentService {

    suspend fun create(invoice: Invoice)

    suspend fun verify(invoice: Invoice)

}