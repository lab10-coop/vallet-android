[![pipeline status](https://code.lab10.io/graz/06-Voucher-Wallet/vallet/android/vallet/badges/master/pipeline.svg)](https://code.lab10.io/graz/06-Voucher-Wallet/vallet/android/vallet/commits/master)


# Vallet

Client and Admin app for Vallet platform

# Dev setup

For development we are using latest version of Android Studio 3.0.1 equipped
with Firebase (only crash reports for now).

To avoid additional overhead we are using product Flavors to distinguish between admin app and client app.
This way we will be able to speed up the development process by sharing at the beginning some code base.

For more details check build `productFlavors` in `app/build.gradle`

# Build

To compile project choose desired build variant to build and compile it.

# Keystore

Keystore is part of the git repo is encrypted and password is keept in secret. Ask @rmi for details.

# CI/CD

We are using `gradle-play-publisher` for deployment.
Gitlab is handling our builds (tests in the future) and deployment to google play see `.gitlabci.yml` for details.
Required secret variables for keystore password and keypassword. See `app/build.gradle` for details.
