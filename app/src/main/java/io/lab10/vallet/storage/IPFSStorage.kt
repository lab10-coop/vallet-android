package io.lab10.vallet.storage

import android.app.Application
import android.content.Context
import android.util.Log
import io.lab10.vallet.TokenStorageBase
import io.lab10.vallet.events.ProductListPublishedEvent
import io.lab10.vallet.models.Token
import org.greenrobot.eventbus.EventBus

class IPFSStorage(val token: Token) : TokenStorageBase {
    private val TAG: String = IPFSStorage::class.java.name

    override fun create() {
        Thread(Runnable {
            var addressName = IPFSManager.INSTANCE.publishProductList(token);
            if (addressName != null) {
                EventBus.getDefault().post(ProductListPublishedEvent(token!!.id, addressName))
            }
        }).start()
    }

    override fun store() {
        Thread(Runnable {
            var addressName = IPFSManager.INSTANCE.publishProductList(token)
            if (addressName != null && addressName.isNotBlank())
                EventBus.getDefault().post(ProductListPublishedEvent(token!!.id, addressName))
        }).start()
    }

    override fun fetch(address: String?) {
        Thread(Runnable {
            if (address != null)
                IPFSManager.INSTANCE.fetchProductList(address, false)
        }).start()
    }
}