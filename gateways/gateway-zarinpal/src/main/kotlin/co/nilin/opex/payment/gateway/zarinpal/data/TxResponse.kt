package co.nilin.opex.payment.gateway.zarinpal.data

data class TxResponse(
    val data: InquiryTx
) : BaseResponse()


data class InquiryTx(val status:String, val code:Int, val message:String)
