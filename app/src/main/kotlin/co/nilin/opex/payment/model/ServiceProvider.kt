package co.nilin.opex.payment.model

import org.springframework.data.relational.core.mapping.Table

@Table("service_provider")
data class ServiceProvider(
    val name: String,
    val url: String,
    var accessToken: String?,
    var refreshToken: String?,
    var isActive: Boolean,
    var isEnabled: Boolean,
    var id: Long? = null
)