package co.nilin.opex.payment.gateway.zarinpal.data

import com.fasterxml.jackson.annotation.JsonProperty

data class VerifyResponse(
        val data: Verify?,
) : BaseResponse()

data class Verify(
        val code: Int?,
        val message: String?,
        @JsonProperty("card_hash")
        val cardHash: String?,
        @JsonProperty("card_pan")
        val cardPan: String?,
        @JsonProperty("ref_id")
        val referenceId: String?,
        @JsonProperty("fee_type")
        val feeType: String?,
        val fee: Long?
)