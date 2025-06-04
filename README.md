# Portal Hackathon Kit - Android

This repository shows you how you can easily integrate Arbitrum Sepolia's blockchain and stablecoins into your Android app using the [Portal Android SDK](https://docs.portalhq.io/guides/android). It covers the following features:

1. Generate an EVM Wallet
2. Fetch and display your wallet's balances
3. Transfer any of these tokens to a different address
4. Request testnet funds directly from the app
5. Backup the wallet with password as the key to Portal servers
6. Recover the wallet with password as the key from Portal servers

Portal SDK also covers the following backup methods which were not covered in this example app but you can learn how to implement them through our [docs](https://docs.portalhq.io/guides/android/back-up-a-wallet):

1. Backup with iCloud
2. Backup with GDrive
3. Backup with Passkey

## How to Run This Example App

1. Clone the repo to your local system
2. Open the project in your Android Studio
3. Go to your Portal Dashboard [settings page](https://app.portalhq.io/settings#client-api-keys) and create a client test API key (screenshots are attached below for your convenience)
4. Update the **PORTAL_CLIENT_API_KEY** field in `app/src/main/java/io/portalhq/portalhackathon/core/commonconstants/PortalConstants.kt` with your Portal client id
5. Run the app and it should work without any issues

## Understanding the Example App

Most of the files in the app are for skeleton of the app and not directly related to Portal SDK. Below are the important files which have Portal SDK code in Action:

1. `PortalModule.kt`: This is where **Portal SDK** is being initialized. The app is configured to work with both Arbitrum and Arbitrum Sepolia.
2. `PortalRepository.kt`: This repository file contains all the features that are performed in this example app using the Portal Android SDK like createWallet, backupWallet, sendToken, etc.
3. `MyPortalApi.kt`: This file contains code to interact with **Portal Assets API** which is used to build transactions and also fetch token balances.
4. `BlockChainConstants.kt`: This file contains common blockchain related constants.
5. `PortalConstants.kt`: This file contains common Portal related constants (like Portal client API key).
6. `HomeScreen.kt`: This is the main screen UI of the app which contains all UI elements of this app (like generate wallet, transfer tokens, fund wallet, etc.)
7. `HomeViewModel.kt`: This ViewModel controls the presentation and events coming from **HomeScreen.kt** and also holds the view state for this screen.

You can ignore all other files as those files are all helper classes.

## Features Highlighted

### Token Transfers

The app allows you to transfer tokens to any EVM address with a simple interface. Each transaction is properly formatted for the Arbitrum Sepolia blockchain.

### In-App Faucet

A unique feature of this app is the integrated faucet functionality that allows you to request testnet funds with a single button press, without having to leave the application.

### Transaction Tracking

All transactions include clickable transaction hashes that lead directly to the Arbitrum Sepolia's block explorer, allowing you to track your transaction status.

## Portal Documentation

### Portal SDK Reference

Portal's SDKs have several pieces of core functionality:

- [Generating a Wallet](https://docs.portalhq.io/guides/web/create-a-wallet): This function creates MPC key shares on your local device and the Portal servers. These key shares support all EVM chains, as well as other chains.
- [Signing a Transaction](https://docs.portalhq.io/guides/web/sign-a-transaction): This function signs a provided transaction, and can broadcast that transaction to a chain when an RPC gateway URL is provided.
- [Signature Hooks](https://docs.portalhq.io/guides/web/add-custom-signature-hooks): By default this repo will submit a transaction without prompting a user, but you can use signature hooks to build a prompt for users before submitting a transaction for signing.

### Portal APIs

Portal supplies several APIs for simplifying your development:

- [Get Assets](https://docs.portalhq.io/reference/client-api/v3-endpoints#get-assets-by-chain): This endpoint returns a list of fungible assets (native and ERC-20 tokens) associated with your client for a given chain.
- [Get NFTs](https://docs.portalhq.io/reference/client-api/v3-endpoints#get-nft-assets-by-chain): This endpoint returns a list of the NFTs associated with your client for a given chain.
- [Get Transactions](https://docs.portalhq.io/reference/client-api/v3-endpoints#get-transactions-by-chain): This endpoint returns a list of the historic transactions associated with your client for a given chain.
- [Build a Transaction - Send Asset](https://docs.portalhq.io/reference/client-api/v3-endpoints#build-a-send-asset-transaction): This endpoint builds a formatted transaction to send a fungible asset (native or ERC-20 tokens) for a given chain.
- [Evaluate a Transaction](https://docs.portalhq.io/reference/client-api/v3-endpoints#evaluate-a-transaction): This endpoint can simulate a transaction and/or scan a transaction for security concerns.

### Other Helpful Resources

- [What is Portal MPC?](https://docs.portalhq.io/resources/portals-mpc-architecture)

## Help

Need help or want to request a feature? Reach out to us on the [official Portal Community Slack](https://portalcommunity.slack.com/archives/C07EZFF9N78).
