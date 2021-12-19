package co.nilin.opex.payment.service

import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.proxy.OpexProxy
import org.springframework.stereotype.Service

@Service
class OpexBridgeService(private val opexProxy: OpexProxy) {

    suspend fun notifyDeposit(invoice: Invoice): Boolean {
        val response = try {
            opexProxy.deposit(invoice).success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        return response
    }

}