package io.portalhq.portalhackathon.presentation.home

import dagger.hilt.android.lifecycle.HiltViewModel
import io.portalhq.portalhackathon.core.viewmodel.ScreenBaseViewModel
import io.portalhq.portalhackathon.data.PortalRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val portalRepository: PortalRepository
) : ScreenBaseViewModel<HomeViewState>() {
    override fun defaultViewState() = HomeViewState()

    init {
        fetchWalletDetails()
    }

    fun refresh() {
        updateState { it.copy(isRefreshing = true) }
        fetchWalletDetails()
    }

    private fun fetchWalletDetails() {
        launchOperation {
            val walletAddress = portalRepository.getWalletAddress()
            updateState { it.copy(walletAddress = walletAddress) }
            if (walletAddress != null) {
                fetchWalletBalance()
            }
        }
    }

    private suspend fun fetchWalletBalance() {
        val balance = portalRepository.getWalletBalance()
        updateState { it.copy(
            nativeBalance = balance.nativeBalance,
            mxnbBalance = balance.mxnbBalance,
        )}
    }

    fun generateWallet() {
        launchOperation {
            portalRepository.createWallet()
            fetchWalletDetails()
        }
    }

    fun sendToken(amount: String, recipientAddress: String, tokenSymbol: String) {
        if (recipientAddress.isBlank()) {
            notify("Please enter a valid recipient address")
            return
        }

        if (amount.isBlank() || amount.toDoubleOrNull() == null){
            notify("Please enter a valid amount")
            return
        }

        launchOperation {
            val transactionHash = portalRepository.sendToken(amount, recipientAddress, tokenSymbol)
            updateState { it.copy(mostRecentTransactionHash = transactionHash) }
            notify("Transaction sent successfully")

            delay(15000)
            fetchWalletBalance()
        }
    }

    fun backupWallet(password: String) {
        if (password.length < 4) {
            notify("Password must be at least 4 characters long")
            return
        }

        launchOperation {
            portalRepository.backupWalletWithPassword(password)
            notify("Wallet backed up successfully")
        }
    }

    fun recoverWallet(password: String) {
        if (password.length < 4) {
            notify("Password must be at least 4 characters long")
            return
        }
        launchOperation {
            portalRepository.recoverWalletWithPassword(password)
            notify("Wallet recovered successfully")
            fetchWalletDetails()
        }
    }

    fun fundWalletWithTestnetAssets() {
        if (viewState.value.walletAddress == null) {
            notify("Please create a wallet first")
            return
        }

        launchOperation {
            val txHash = portalRepository.fundWalletWithTestnetAssets()
            if (txHash != null) {
                updateState { it.copy(mostRecentTransactionHash = txHash) }
                notify("Testnet funding transaction sent successfully")

                // Wait a bit before refreshing balances to allow transaction to be processed
                delay(15000)
                fetchWalletBalance()
            } else {
                notify("Funding request processed, but no transaction hash was returned")
                // Refresh balances anyway in case it worked
                delay(15000)
                fetchWalletBalance()
            }
        }
    }

    private fun launchOperation(operation: suspend () -> Unit) {
        launchWithDefaultErrorHandling(onEndWithError = {
            updateState { it.copy(isDataLoading = false, isRefreshing = false) }
        }) {
            updateState { it.copy(isDataLoading = !it.isRefreshing) }
            operation()
            updateState { it.copy(isDataLoading = false, isRefreshing = false) }
        }
    }
}
