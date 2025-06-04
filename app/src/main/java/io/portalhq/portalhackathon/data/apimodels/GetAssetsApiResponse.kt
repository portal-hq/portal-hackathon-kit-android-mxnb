package io.portalhq.portalhackathon.data.apimodels

import io.portalhq.portalhackathon.core.commonconstants.BlockChainConstants

data class GetAssetsApiResponse(
    val nativeBalance: NativeBalance,
    val tokenBalances: List<TokenBalance>
) {

    val mxnbBalance: String
        get() {
            return tokenBalances.firstOrNull {
                it.symbol == BlockChainConstants.MXNB_TOKEN_SYMBOL
            }?.balance ?: return "0"
        }
}

data class NativeBalance(
    val balance: String
)

data class TokenBalance(
    val balance: String,
    val symbol: String
)
