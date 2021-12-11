package co.nilin.opex.payment.service

import co.nilin.opex.payment.config.Providers
import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.dao.ServiceProviderRepository
import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.model.ServiceProvider
import co.nilin.opex.payment.proxy.JibitPPGProxy
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
@Qualifier(Providers.Jibit)
class JibitPaymentService(
    private val proxy: JibitPPGProxy,
    private val invoiceRepository: InvoiceRepository,
    private val serviceProviderRepository: ServiceProviderRepository
) : PaymentService {

    private var service: ServiceProvider? = null
    private val apiKey = ""
    private val secretKey = ""

    override suspend fun create(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun verify(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    private suspend fun checkTokens() {
        val service = getService()
        if (service.accessToken == null) {
            val response = proxy.generateToken(apiKey, secretKey)
            service.apply {
                accessToken = response.accessToken
                refreshToken = response.refreshToken
                updateService(service)
            }
        } else {

        }
    }

    private suspend fun refreshToken() {

    }

    private suspend fun getService(): ServiceProvider {
        if (service == null) {
            service = serviceProviderRepository.findByName(Providers.Jibit).awaitSingleOrNull()
                ?: throw IllegalStateException("No service found - ${Providers.Jibit}")
        }
        return service!!
    }

    private suspend fun updateService(service: ServiceProvider) {
        this.service = serviceProviderRepository.save(service).awaitSingleOrNull()
    }

}