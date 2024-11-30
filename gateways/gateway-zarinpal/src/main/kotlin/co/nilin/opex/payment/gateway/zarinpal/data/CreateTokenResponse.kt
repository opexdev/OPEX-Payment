package co.nilin.opex.payment.gateway.zarinpal.data


data class CreateTokenResponse(val data: Data?):BaseResponse()

data class Data(val code:Int, val message:String?, val authority:String)