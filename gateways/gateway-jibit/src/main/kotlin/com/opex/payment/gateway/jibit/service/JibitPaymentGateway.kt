package com.opex.payment.gateway.jibit.service

import com.opex.payment.core.Gateways
import com.opex.payment.core.model.CreateInvoiceResponse
import com.opex.payment.core.model.Invoice
import com.opex.payment.core.model.VerifyInvoiceResponse
import com.opex.payment.core.spi.PaymentGateway
import com.opex.payment.gateway.jibit.proxy.JibitPPGProxy
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
@Qualifier(Gateways.Jibit)
class JibitPaymentGateway(private val proxy: JibitPPGProxy) : PaymentGateway {

    private val apiKey = ""
    private val secretKey = ""

    override suspend fun create(invoice: Invoice): CreateInvoiceResponse {
        TODO("Not yet implemented")
    }

    override suspend fun verify(invoice: Invoice): VerifyInvoiceResponse {
        TODO("Not yet implemented")
    }

    private suspend fun checkTokens() {

    }

    private suspend fun refreshToken() {

    }

}