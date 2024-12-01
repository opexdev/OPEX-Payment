package com.opex.payment.core.model

data class ErrorResponse(val errors: Error)

data class Error(val message:String? , val code:Int?)