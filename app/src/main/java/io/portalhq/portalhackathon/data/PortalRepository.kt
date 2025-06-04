package io.portalhq.portalhackathon.data

import io.portalhq.android.Portal
import io.portalhq.android.mpc.data.BackupConfigs
import io.portalhq.android.mpc.data.BackupMethods
import io.portalhq.android.mpc.data.PasswordStorageConfig
import io.portalhq.android.provider.data.PortalRequestMethod
import io.portalhq.android.storage.mobile.PortalNamespace
import io.portalhq.portalhackathon.core.commonconstants.BlockChainConstants
import io.portalhq.portalhackathon.data.apimodels.BuildTransactionApiRequest
import io.portalhq.portalhackathon.data.datamodels.UserBalance
import io.portalhq.android.api.data.FundParams
import javax.inject.Inject

class PortalRepository @Inject constructor(
    private val portal: Portal,
    private val portalApi: MyPortalApi,
) {
    suspend fun createWallet() {
        if (!portal.isWalletOnDevice()) {
            portal.createWallet()
        }
    }

    suspend fun isWalletCreated() = portal.isWalletOnDevice()

    suspend fun getWalletAddress(): String? {
        return if (isWalletCreated()) {
            portal.getAddress(PortalNamespace.EIP155)
        } else {
            null
        }
    }

    suspend fun getWalletBalance(): UserBalance {
        val assets = portalApi.getAssets()
        return UserBalance(
            nativeBalance = assets.nativeBalance.balance,
            mxnbBalance = assets.tokenBalances.firstOrNull {
                it.symbol == BlockChainConstants.MXNB_TOKEN_SYMBOL
            }?.balance ?: "0",
        )
    }

    suspend fun sendToken(amount: String, recipientAddress: String, tokenSymbol: String): String {
        val transaction = portalApi.buildTransaction(
            BuildTransactionApiRequest(
                amount = amount,
                to = recipientAddress,
                token = tokenSymbol
            )
        )

        // Convert the transaction JsonObject to a Map for Portal SDK
        val transactionMap = mutableMapOf<String, Any>()
        for (entry in transaction.transaction.entrySet()) {
            transactionMap[entry.key] = entry.value.asString
        }

        return portal.request(
            chainId = BlockChainConstants.ARBITRUM_SEPOLIA, // Using testnet by default
            method = PortalRequestMethod.eth_sendTransaction, // Changed to EVM compatible method
            params = listOf(transactionMap)
        ).result as String
    }

    suspend fun backupWalletWithPassword(password: String = "0000") {
        portal.backupWallet(
            backupMethod = BackupMethods.Password,
            backupConfigs = BackupConfigs(PasswordStorageConfig(password))
        )
    }

    suspend fun recoverWalletWithPassword(password: String = "0000"): String {
        return portal.recoverWallet(
            backupMethod = BackupMethods.Password,
            backupConfigs = BackupConfigs(PasswordStorageConfig(password))
        )
    }

    suspend fun fundWalletWithTestnetAssets(): String? {
        val chainId = BlockChainConstants.ARBITRUM_SEPOLIA
        val params = FundParams(
            amount = "0.01", // Request 0.01
            token = "NATIVE"  // Native token
        )

        // Fund the Portal wallet
        val result = portal.receiveTestnetAsset(chainId, params)

        return if (result.isSuccess) {
            val response = result.getOrNull()
            response?.data?.txHash
        } else {
            val error = result.exceptionOrNull()?.message ?: "Unknown error"
            throw Exception("Failed to fund wallet: $error")
        }
    }
}
