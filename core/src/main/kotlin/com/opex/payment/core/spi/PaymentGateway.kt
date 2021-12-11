package com.opex.payment.core.spi

import com.opex.payment.core.model.CreateInvoiceResponse
import com.opex.payment.core.model.Invoice
import com.opex.payment.core.model.VerifyInvoiceResponse

interface PaymentGateway {

    suspend fun create(invoice: Invoice): CreateInvoiceResponse

    suspend fun verify(invoice: Invoice): VerifyInvoiceResponse

}