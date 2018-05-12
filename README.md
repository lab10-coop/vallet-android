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

# CI/CD

We are using `gradle-play-publisher` for deployment.
Gitlab is handling our builds (tests in the future) and deployment to google play see `.gitlabci.yml` for details.
Required secret variables for keystore password and keypassword. See `app/build.gradle` for details.
