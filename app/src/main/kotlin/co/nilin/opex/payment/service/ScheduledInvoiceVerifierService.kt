package co.nilin.opex.payment.service

import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.dao.PaymentGatewayRepository
import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.utils.Interval
import co.nilin.opex.payment.utils.toInvoiceDTO
import com.opex.payment.core.spi.PaymentGateway
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class ScheduledInvoiceVerifierService(
    private val beanFactory: BeanFactory,
    private val opexBridgeService: OpexBridgeService,
    private val invoiceRepository: InvoiceRepository,
    private val gatewayRepository: PaymentGatewayRepository
) {

    private val executor = Executors.newSingleThreadExecutor()

    @Scheduled(fixedDelay = 60000)
    fun run() {
        executor.execute { runBlocking(Dispatchers.IO) { verifyInvoices() } }
    }

    suspend fun verifyInvoices() {
        val time = Interval(5, TimeUnit.MINUTES).getLocalDateTime()
        invoiceRepository.findAllUnverifiedOlderThan(time)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .take(10)
            .forEach { verify(it) }
    }

    @Transactional
    suspend fun verify(invoice: Invoice) {
        delay(1000)
        val gatewayModel = gatewayRepository.findById(invoice.paymentGatewayId).awaitFirst()
        val service = getGatewayService(gatewayModel.name)
        val response = service.verify(invoice.toInvoiceDTO())
        invoice.status = response.status

        val isNotified = opexBridgeService.notifyDeposit(invoice)
        invoice.isNotified = isNotified

        invoiceRepository.save(invoice).awaitFirst()
    }

    private fun getGatewayService(name: String): PaymentGateway {
        return beanFactory.getBean<PaymentGateway>(name)
    }

}