package io.portalhq.portalhackathon.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import android.widget.Toast
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import io.portalhq.portalhackathon.core.commonconstants.BlockChainConstants
import io.portalhq.portalhackathon.presentation.home.HomeViewModel
import io.portalhq.portalhackathon.presentation.home.HomeViewState
import io.portalhq.portalhackathon.ui.components.AppScaffold
import io.portalhq.portalhackathon.ui.components.ScreenNavigation
import io.portalhq.portalhackathon.ui.components.ScreenNotification

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewState.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val pullToRefreshState = rememberPullRefreshState(
        refreshing = viewState.value.isRefreshing,
        onRefresh = { viewModel.refresh() }
    )
    AppScaffold(
        title = "Portal Wallet",
        snackbarHostState = snackbarHostState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullToRefreshState)
                .verticalScroll(rememberScrollState())
        ) {
            HomeScreenUI(viewModel = viewModel, viewState = viewState.value)
            PullRefreshIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                refreshing = viewState.value.isRefreshing,
                state = pullToRefreshState
            )
            if (viewState.value.isDataLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    val navigationCommand = viewModel.navigationCommand.collectAsState(initial = null)
    ScreenNavigation(
        navigationCommand = navigationCommand.value,
        navController = navController
    ) {
    }

    val notificationCommand = viewModel.notificationCommand.collectAsState(initial = null)
    ScreenNotification(
        snackbarHostState = snackbarHostState,
        notificationCommand = notificationCommand.value
    )
}

@Composable
private fun HomeScreenUI(viewModel: HomeViewModel, viewState: HomeViewState) {
    Column(
        modifier = Modifier
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .fillMaxSize(),
    ) {
        if (viewState.walletAddress == null) {
            GenerateWallet(viewModel = viewModel, viewState = viewState)
        }

        if (viewState.walletAddress != null) {
            WalletDetails(viewModel = viewModel,viewState = viewState)
            TransferToken(viewModel = viewModel, viewState = viewState)
        }
        WalletBackupAndRecovery(viewModel = viewModel, viewState = viewState)
    }
}

@Composable
private fun ColumnScope.GenerateWallet(viewModel: HomeViewModel, viewState: HomeViewState) {
    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "No wallet found on device. Let's create one!",
        style = MaterialTheme.typography.body2
    )

    Button(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 10.dp),
        onClick = { viewModel.generateWallet() },
        enabled = viewState.areActionsAllowed
    ) {
        Text(text = "Generate Wallet")
    }
}

@Composable
private fun ColumnScope.WalletDetails(viewModel: HomeViewModel,viewState: HomeViewState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Wallet Details",
            style = MaterialTheme.typography.h6.copy(fontSize = 22.sp)
        )

        Button(
            onClick = { viewModel.fundWalletWithTestnetAssets() },
            enabled = viewState.areActionsAllowed
        ) {
            Text(text = "Fund Wallet")
        }
    }

    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "Wallet Address",
        style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
    )

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionContainer(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = viewState.walletAddress.orEmpty(),
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(viewState.walletAddress.orEmpty()))
                Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy address",
                tint = MaterialTheme.colors.primary
            )
        }
    }

    Divider(modifier = Modifier.padding(vertical = 10.dp))

    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "Balances",
        style = MaterialTheme.typography.subtitle1.copy(fontSize = 18.sp)
    )

    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "Native Balance",
        style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
    )
    Text(
        text = "${viewState.nativeBalance ?: "0"} ETH",
        style = MaterialTheme.typography.body2
    )

    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "MXNB Balance",
        style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
    )
    Text(
        text = "${viewState.mxnbBalance ?: "0"} MXNB",
        style = MaterialTheme.typography.body2
    )
}

@Composable
private fun ColumnScope.TransferToken(viewModel: HomeViewModel, viewState: HomeViewState) {
    var recipientAddress by remember {
        mutableStateOf(BlockChainConstants.EIP_155_TEST_ADDRESS)
    }

    var amount by remember {
        mutableStateOf("0.001")
    }

    var selectedToken by remember {
        mutableStateOf(BlockChainConstants.NATIVE_TOKEN_SYMBOL)
    }

    var expandedDropdown by remember { mutableStateOf(false) }

    Text(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 20.dp),
        text = "Transfer Tokens",
        style = MaterialTheme.typography.h6.copy(fontSize = 20.sp)
    )

    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "Recipient Address",
        style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
    )
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = recipientAddress,
        singleLine = true,
        onValueChange = { value -> recipientAddress = value },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
    )

    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "Amount",
        style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
    )
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = amount,
        singleLine = true,
        placeholder = { Text(text = "0.0") },
        onValueChange = { value -> amount = value },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
        colors = TextFieldDefaults.textFieldColors()
    )

    Text(
        modifier = Modifier.padding(top = 10.dp),
        text = "Token",
        style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = selectedToken,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expandedDropdown = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Token"
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expandedDropdown,
            onDismissRequest = { expandedDropdown = false }
        ) {
            DropdownMenuItem(onClick = {
                selectedToken = BlockChainConstants.MXNB_TOKEN_SYMBOL
                expandedDropdown = false
            }) {
                Text(BlockChainConstants.MXNB_TOKEN_SYMBOL)
            }
        }
    }

    if (viewState.mostRecentTransactionHash != null) {
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = "Recent Transaction",
            style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
        )

        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val url = "https://sepolia.arbiscan.io/tx/${viewState.mostRecentTransactionHash}"
                        uriHandler.openUri(url)
                    },
                text = viewState.mostRecentTransactionHash,
                style = MaterialTheme.typography.body2.copy(
                    color = MaterialTheme.colors.primary,
                    textDecoration = TextDecoration.Underline
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(viewState.mostRecentTransactionHash))
                    Toast.makeText(context, "Transaction hash copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy transaction hash",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }

    Button(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 10.dp),
        onClick = {
            viewModel.sendToken(
                amount = amount,
                recipientAddress = recipientAddress,
                tokenSymbol = selectedToken
            )
        },
        enabled = viewState.areActionsAllowed
    ) {
        Text(text = "Transfer $selectedToken")
    }
}

@Composable
private fun ColumnScope.WalletBackupAndRecovery(viewModel: HomeViewModel, viewState: HomeViewState) {
    var password by remember {
        mutableStateOf("0000")
    }
    var isPasswordVisible by remember {
        mutableStateOf(false)
    }

    Text(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 20.dp, bottom = 10.dp),
        text = "Backup & Recovery",
        style = MaterialTheme.typography.h6.copy(fontSize = 20.sp)
    )

    Text(
        modifier = Modifier.padding(bottom = 10.dp),
        text = "Password",
        style = MaterialTheme.typography.subtitle2.copy(fontSize = 16.sp)
    )
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        value = password,
        singleLine = true,
        onValueChange = { value -> password = value },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
            val description = if (isPasswordVisible) "Hide Password" else "Show Password"
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(imageVector = icon, contentDescription = description)
            }
        }
    )

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (viewState.walletAddress != null) {
            Button(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f),
                onClick = { viewModel.backupWallet(password) },
                enabled = viewState.areActionsAllowed
            ) {
                Text(text = "Backup Wallet")
            }
        }

        Button(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            onClick = { viewModel.recoverWallet(password) },
            enabled = viewState.areActionsAllowed
        ) {
            Text(text = "Recover Wallet")
        }
    }
}
