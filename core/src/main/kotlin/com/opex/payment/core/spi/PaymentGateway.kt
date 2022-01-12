package com.opex.payment.core.spi

import com.opex.payment.core.model.CreateInvoiceResponse
import com.opex.payment.core.model.IPGRequestDTO
import com.opex.payment.core.model.InvoiceDTO
import com.opex.payment.core.model.VerifyInvoiceResponse

interface PaymentGateway {

    suspend fun create(invoice: InvoiceDTO): CreateInvoiceResponse

    suspend fun verify(invoice: InvoiceDTO,request: IPGRequestDTO): VerifyInvoiceResponse

    suspend fun createRedirectUrl(request: IPGRequestDTO): String

}