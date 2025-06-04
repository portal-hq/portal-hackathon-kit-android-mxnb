package io.portalhq.portalhackathon.data.apimodels

import com.google.gson.JsonObject

data class BuildTransactionApiResponse(
    val transaction: JsonObject,
    val metadata: TransactionMetadata
)

data class TransactionMetadata(
    val amount: String,
    val fromAddress: String,
    val toAddress: String,
    val tokenSymbol: String,
    val tokenDecimals: Int,
    val rawAmount: String
)