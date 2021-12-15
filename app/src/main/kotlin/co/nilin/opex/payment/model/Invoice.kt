package co.nilin.opex.payment.model

import com.opex.payment.core.model.Currency
import com.opex.payment.core.model.InvoiceStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("invoice")
data class Invoice(
    val userId: String,
    val amount: Double,
    val callbackUrl: String,
    val currency: Currency,
    val paymentGatewayId: Long,
    val reference: String = UUID.randomUUID().toString(),
    var gatewayRequestId: String? = null,
    var gatewayStatus: String? = null,
    var status: InvoiceStatus = InvoiceStatus.New,
    var description: String? = null,
    var cardNumber: String? = null,
    var mobile: String? = null,
    var nationalCode: String? = null,
    var isNotified: Boolean = false,
    val createDate: LocalDateTime = LocalDateTime.now(),
    var updateDate: LocalDateTime = LocalDateTime.now(),
    @Id var id: Long? = null
)