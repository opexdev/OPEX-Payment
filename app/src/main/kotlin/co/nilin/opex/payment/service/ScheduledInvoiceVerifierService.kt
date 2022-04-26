package co.nilin.opex.payment.service

import co.nilin.opex.payment.dao.IPGRequestRepository
import co.nilin.opex.payment.dao.InvoiceRepository
import co.nilin.opex.payment.dao.PaymentGatewayRepository
import co.nilin.opex.payment.model.Invoice
import co.nilin.opex.payment.utils.Interval
import co.nilin.opex.payment.utils.asIPGRequestDTO
import co.nilin.opex.payment.utils.asInvoiceDTO
import com.opex.payment.core.model.InvoiceStatus
import com.opex.payment.core.spi.PaymentGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.getBean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Service
class ScheduledInvoiceVerifierService(
    private val beanFactory: BeanFactory,
    private val opexBridgeService: OpexBridgeService,
    private val invoiceRepository: InvoiceRepository,
    private val gatewayRepository: PaymentGatewayRepository,
    private val ipgRequestRepository: IPGRequestRepository
) {

    private val executor = Executors.newFixedThreadPool(2)
    private val logger = LoggerFactory.getLogger(ScheduledInvoiceVerifierService::class.java)

    @Scheduled(fixedDelay = 30000)
    fun runVerifier() {
        executor.execute { runBlocking(Dispatchers.IO) { verifyInvoices() } }
    }

    suspend fun verifyInvoices() {
        val verifyTime = Interval(2, TimeUnit.MINUTES).getLocalDateTime()
        val notifyTime = Interval(1, TimeUnit.MINUTES).getLocalDateTime()
        invoiceRepository.findAllOpenOlderThan(verifyTime)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .take(10)
            .also { if (it.isNotEmpty()) logger.info("verifying ${it.size} invoices") }
            .forEach {
                verify(it)
                checkExpiry(it)
            }

        invoiceRepository.findAllDoneButNotNotifiedOlderThan(notifyTime)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .take(10)
            .also { if (it.isNotEmpty()) logger.info("notifying ${it.size} invoices") }
            .forEach { notify(it) }
    }

    @Transactional
    suspend fun verify(invoice: Invoice) {
        logger.info("Verifying invoice ${invoice.reference}")
        val ipgRequest = ipgRequestRepository.findOpenRequest(invoice.id!!).awaitFirstOrNull() ?: return
        val gatewayModel = gatewayRepository.findById(invoice.paymentGatewayId).awaitFirst()
        val service = getGatewayService(gatewayModel.name)

        delay(2000)
        val response = service.verify(invoice.asInvoiceDTO(), ipgRequest.asIPGRequestDTO())
        ipgRequest.isPaid = response.status == InvoiceStatus.Done
        invoice.status = response.status
        invoice.updateDate = LocalDateTime.now()
        invoiceRepository.save(invoice).awaitFirst()

        val isNotified = opexBridgeService.notifyDeposit(invoice)
        invoice.isNotified = isNotified
        invoice.updateDate = LocalDateTime.now()

        invoiceRepository.save(invoice).awaitFirst()
        ipgRequestRepository.save(ipgRequest).awaitFirst()
    }

    suspend fun notify(invoice: Invoice) {
        delay(2000)
        logger.info("Notifying invoice ${invoice.reference}")
        invoice.isNotified = try {
            opexBridgeService.notifyDeposit(invoice)
        } catch (e: Exception) {
            logger.error("Failed to notify the core system for invoice ${invoice.reference}", e)
            false
        }
        invoiceRepository.save(invoice).awaitFirst()
    }

    suspend fun checkExpiry(invoice: Invoice) {
        val expiryTime = Interval(20, TimeUnit.MINUTES).getLocalDateTime()
        if (invoice.createDate > expiryTime) return

        logger.info("Invoice ${invoice.reference} is expired")
        with(invoice) {
            status = InvoiceStatus.Expired
            updateDate = LocalDateTime.now()
            invoiceRepository.save(this).awaitFirst()
        }

        ipgRequestRepository.findOpenRequest(invoice.id!!)
            .awaitFirstOrNull()
            ?.let {
                it.isExpired = true
                ipgRequestRepository.save(it).awaitFirst()
            }
    }

    private fun getGatewayService(name: String): PaymentGateway {
        return beanFactory.getBean<PaymentGateway>(name)
    }

}