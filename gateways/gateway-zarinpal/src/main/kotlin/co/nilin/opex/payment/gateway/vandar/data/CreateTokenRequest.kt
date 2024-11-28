package co.nilin.opex.payment.gateway.vandar.data

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateTokenRequest(
    @JsonProperty("api_key")
    val apiKey: String,
    val amount: Long,
    @JsonProperty("callback_url")
    val callbackUrl: String,
    @JsonProperty("mobile_number")
    val mobile: String?,
    val factorNumber: String?,
    val description: String?,
    @JsonProperty("valid_card_number")
    val card: String?,
    @JsonProperty("national_code")
    val nationalCode: String?
)