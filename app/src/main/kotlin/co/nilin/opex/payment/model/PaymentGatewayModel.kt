package co.nilin.opex.payment.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("payment_gateway")
data class PaymentGatewayModel(
    val name: String,
    var isEnabled: Boolean,
    @Id var id: Long? = null
)