package co.nilin.opex.payment.model

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("invoice")
data class Invoice(
    val userId: String,
    val reference: String,
    val amount: Double,
    val callbackUrl: String,
    val currency: Currency,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val updateDate: LocalDateTime = LocalDateTime.now(),
    val status: InvoiceStatus = InvoiceStatus.New,
    val remoteStatus: String? = null,
    val description: String? = null,
    val cardNumber: String? = null,
    val nationalCode: String? = null,
    var id: Long? = null
)