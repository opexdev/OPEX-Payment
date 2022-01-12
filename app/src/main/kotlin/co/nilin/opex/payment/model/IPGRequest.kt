package co.nilin.opex.payment.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("ipg_request")
data class IPGRequest(
    val invoiceId: Long,
    val requestId: String,
    var isExpired: Boolean = false,
    var isPaid: Boolean = false,
    val createDate: LocalDateTime = LocalDateTime.now(),
    @Id
    val id: Long? = null,
)