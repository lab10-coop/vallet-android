package io

import android.net.Uri
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.NewShopAddEvent
import io.lab10.vallet.events.ProductChangedEvent
import io.lab10.vallet.events.ProductRemoveEvent
import io.lab10.vallet.models.Token
import io.lab10.vallet.models.Wallet
import org.greenrobot.eventbus.EventBus

class ValletUriParser {
    companion object {
        fun invoke(uri: Uri) {
            if (uri.scheme == "vallet") {
                invokeForHost(uri)
            } else {
                throw ValletUriError("Invalid schema")
            }
        }

        // We are using currently few hosts as
        private fun invokeForHost(uri: Uri) {
            when (uri.host) {
                "shop" -> {
                    addShop(uri.pathSegments.last())
                }
            }

        }

        private fun addShop(tokenAddress: String) {
            if (Wallet.isValidAddress(tokenAddress)) {
                val token = Token(0, "", tokenAddress, 0, 0, "", true, 0 )
                token.storage().fetch()
            } else {
                throw ValletUriError("Invalid token address")
            }
        }

    }

}

class ValletUriError(message: String?) : Exception(message)