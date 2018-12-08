[![pipeline status](https://code.lab10.io/graz/06-Voucher-Wallet/vallet/android/vallet/badges/master/pipeline.svg)](https://code.lab10.io/graz/06-Voucher-Wallet/vallet/android/vallet/commits/master)

# Vallet

Client and Admin app for Vallet platform

Vallet is a project designed to handle vouchers given out for certain purposes to avoid tedious cash management and create a more efficient and fraud proof alternative. In the first version it is used to handle drink vouchers and self-serving payments.

Modern blockchain systems such as Ethereum provide the ability to safely, quickly, and unchangingly trade a value (token) on the blockchain. In addition, Ethereum has created the possibility to generate tokens via a standard protocol, which are then also easily tradable. These self-generated tokens can be used as coupons, e.g. in exchange for Euro then as a means of payment for drinks, tickets, tickets, etc.

In this project, we want to simplify the creation, distribution and redemption of vouchers (implemented for drinks) on a blockchain so that it is feasible for every publisher without any problems and every user finds this type of payment one of the most convenient options. We use technologies such as Bluetooth and NFC in combination with current smartphones and tablets to make the user experience as optimal as possible. Data will be distributed to users via IPFS so that servers can be avoided.

# Dev setup

For development we are using latest version of Android Studio 3.0.1 equipped
with Firebase (only crash reports for now).

To avoid additional overhead we are using product Flavors to distinguish between admin app and client app.
This way we will be able to speed up the development process by sharing at the beginning some code base.

For more details check build `productFlavors` in `app/build.gradle`

# Build

To compile project choose desired build variant to build and compile it.

# Upgrading smart contract

Everything is handle by TokenFactory Smart contract. If something will change in that contract ABI needs to be updated within the project.
Get latest version of ABI json, should be found in the contract repository.

Use web3j to generate new java class wrapper:

    web3j solidity generate a.bin TokenFactoryJSONABI.json --package io.lab10.vallet.admin -o TokenFactoryNew.java

Where a.bin - is empty file (requred by command line tool)
TOkenFactoryJSONABI.json - is latest json ABI of the contract

Since we are using Kotlin as a main language java file needs to be converted to kotlin, can be don via Android Studio.

Same procedure is for Token smart contract.

# Debug mode

The Admin app has debug mode screen. It is a special screen which allow
you to configure specific attributes like node ip, contract address, api server
ip, ipfs address, generate new token, request funds or get some verbose
notification for the app.

Most of those settings in the future would be moved to settings for regular
users but for time being is hidden as by default none of the user needs access
to it.

To enable it on just tap 8 times on circulating value (home screen)
