package co.nilin.opex.payment.model

import org.springframework.data.relational.core.mapping.Table

@Table
data class PaymentGatewayModel(
    val name: String,
    val nameFa: String,
    val gatewayUrl: String,
    val apiUrl: String,
    var isEnabled: Boolean,
    var id: Long? = null
)