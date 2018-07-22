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
    // TODO fix that and pass context to ipfs storage from above or move config to db
    private val context = Application()

    override fun create() {
        var addressName = IPFSManager.INSTANCE.publishProductList(context);
        if (addressName != null) {
            Log.d(TAG, "Address of products list: " + addressName)
            EventBus.getDefault().post(ProductListPublishedEvent(token!!.id, addressName))
        }
    }

    override fun store() {
        var addressName = IPFSManager.INSTANCE.publishProductList(context)
        if (addressName != null && addressName.isNotBlank())
            EventBus.getDefault().post(ProductListPublishedEvent(token!!.id, addressName))
    }

    override fun fetch() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}