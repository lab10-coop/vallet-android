package io

import android.net.Uri
import io.lab10.vallet.events.*
import io.lab10.vallet.models.Users
import io.lab10.vallet.models.Wallet
import org.greenrobot.eventbus.EventBus

class ValletUriParser {
    companion object {
        fun invoke(uri: Uri) {
            when(uri.scheme) {
                "vallet" -> {
                    invokeForHost(uri)
                }
                "valletadmin" -> {
                    invokeForHost(uri)
                } else  -> {
                throw ValletUriError("Invalid schema")
                }
            }

        }

        // We are using currently few hosts as
        private fun invokeForHost(uri: Uri) {
            when (uri.host) {
                "shop" -> {
                    addShop(uri.pathSegments.last())
                }
                "user" -> {
                    var userName = "User"
                    uri.getQueryParameter("user_name")?.let { userName = it }
                    val userAddress = uri.pathSegments.last()
                    addUser(userAddress, userName)
                }

            }

        }

        private fun addUser(address: String, name: String) {
            if (Wallet.isValidAddress(address)) {
                var user = Users.User(address, address, name)
                EventBus.getDefault().post(DeepLinkUserAddEvent(user))
            } else {
                EventBus.getDefault().post(ErrorEvent("Invalid wallet address"))
            }
        }

        private fun addShop(tokenAddress: String) {
            if (Wallet.isValidAddress(tokenAddress)) {
                EventBus.getDefault().postSticky(AddNewStoreEvent(tokenAddress))
            } else {
                throw ValletUriError("Invalid token address")
            }
        }

    }

}

class ValletUriError(message: String?) : Exception(message)