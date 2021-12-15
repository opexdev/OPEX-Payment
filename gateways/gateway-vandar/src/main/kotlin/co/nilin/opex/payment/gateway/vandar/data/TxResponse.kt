package co.nilin.opex.payment.gateway.vandar.data

data class TxResponse(
    val amount: String,
    val realAmount: Long,
    val wage: String,
    val transId: Long,
    val refnumber: String?,
    val trackingCode: String?,
    val factorNumber: String?,
    val description: String?,
    val mobile: String?,
    val cardNumber: String?,
    val paymentStart: String?,
    val cid: String?,
    val message: String?,
) : BaseResponse()