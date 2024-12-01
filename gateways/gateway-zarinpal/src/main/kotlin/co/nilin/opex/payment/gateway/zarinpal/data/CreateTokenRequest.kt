package co.nilin.opex.payment.gateway.zarinpal.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.opex.payment.core.model.Currency


data class CreateTokenRequest(
        @JsonProperty("merchant_id")
        val merchantId: String,
        val amount: Long,
        @JsonProperty("callback_url")
        val callbackUrl: String,
        val currency: Currency? = null,
        val description: String? = null,
        val metaData: MedataData? = null
)

data class MedataData(val mobile: String?, val email: String?)