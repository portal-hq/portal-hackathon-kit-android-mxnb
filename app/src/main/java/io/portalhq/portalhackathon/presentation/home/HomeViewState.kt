package io.portalhq.portalhackathon.presentation.home

import io.portalhq.portalhackathon.core.viewstate.ViewState

data class HomeViewState(
    val isDataLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val walletAddress: String? = null,
    val nativeBalance: String? = null,
    val mxnbBalance: String? = null,
    val mostRecentTransactionHash: String? = null,
    val selectedToken: String? = null
) : ViewState {
    val areActionsAllowed = !isDataLoading && !isRefreshing
}
