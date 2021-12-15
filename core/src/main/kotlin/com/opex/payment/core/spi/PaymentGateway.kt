package com.opex.payment.core.spi

import com.opex.payment.core.model.CreateInvoiceResponse
import com.opex.payment.core.model.InvoiceDTO
import com.opex.payment.core.model.VerifyInvoiceResponse

interface PaymentGateway {

    suspend fun create(invoice: InvoiceDTO): CreateInvoiceResponse

    suspend fun verify(invoice: InvoiceDTO): VerifyInvoiceResponse

    suspend fun createRedirectUrl(invoice: InvoiceDTO): String

}