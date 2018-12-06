package io.lab10.vallet.storage

import io.lab10.vallet.TokenStorageBase
import io.lab10.vallet.manager.PriceListManager
import io.lab10.vallet.models.Token
import io.lab10.vallet.models.TokenBase
import io.lab10.vallet.models.TokenUpdate


// TODO remove
class ApiStorage(val token: Token) : TokenStorageBase{

    override fun create() {
        val tokenBase = TokenBase(token!!.name, token!!.tokenType, token!!.tokenAddress, token!!.productsBase())
        PriceListManager.createPriceList(tokenBase)
    }

    override fun store() {
        val tokenUpdate = TokenUpdate(token!!.secret, token!!.name, token!!.productsBase())
        PriceListManager.updatePriceList(tokenUpdate)
    }

    override fun fetch(notUsed: String?) {
        PriceListManager.fetchPriceList(token!!.tokenAddress)
    }
}