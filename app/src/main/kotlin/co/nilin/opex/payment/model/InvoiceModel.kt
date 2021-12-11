package co.nilin.opex.payment.model

import com.opex.payment.core.model.Currency
import com.opex.payment.core.model.InvoiceStatus
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table
data class InvoiceModel(
    val userId: String,
    val reference: String,
    val amount: Double,
    val callbackUrl: String,
    val currency: Currency,
    val paymentGatewayId: Long,
    var gatewayId: String? = null,
    var gatewayStatus: String? = null,
    var description: String? = null,
    var cardNumber: String? = null,
    var mobile: String? = null,
    var nationalCode: String? = null,
    var status: InvoiceStatus = InvoiceStatus.New,
    val createDate: LocalDateTime = LocalDateTime.now(),
    var updateDate: LocalDateTime = LocalDateTime.now(),
    var id: Long? = null
)