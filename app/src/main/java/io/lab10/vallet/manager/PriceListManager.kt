package io.lab10.vallet.manager

import io.lab10.vallet.ValletApp
import io.lab10.vallet.interfaces.ValletApiService
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.ProductChangedEvent
import io.lab10.vallet.models.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import retrofit2.HttpException

class PriceListManager {

    companion object {
        fun createPriceList(token: TokenBase) {
            // TODO extract that to settings
            val apiService = ValletApiService.create("http://192.168.1.103:3000")

            apiService.createPriceList(token).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe ({
                        result ->
                        val response = (result as TokenCreated)
                        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
                        // TODO support multiple tokens
                        val token = tokenBox.query().build().findFirst()
                        if (token != null) {
                            token.secret = response.secret
                            tokenBox.put(token)
                        }
                    }, { error ->
                        EventBus.getDefault().post(ErrorEvent(error.message.toString()))
                    })
        }

        fun fetchPriceList(tokenContractAddress: String) {
            val apiService = ValletApiService.create("http://192.168.1.103:3000")
            apiService.fetchPriceList(tokenContractAddress).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe ({
                        result ->
                        val response = (result as TokenBase)
                        updateLocalDb(response.products)
                    }, { error ->
                        EventBus.getDefault().post(ErrorEvent(error.message.toString()))
                    })
        }

        fun updatePriceList(token: TokenUpdate) {
            val apiService = ValletApiService.create("http://192.168.1.103:3000")
            apiService.updatePriceList(token).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe ({
                        result ->
                        val response = (result as TokenBase)
                        updateLocalDb(response.products)
                    }, { error ->
                        EventBus.getDefault().post(ErrorEvent(error.message.toString()))
                    })
        }

        private fun updateLocalDb(products: List<ProductBase>) {
            val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
            val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            val token = ValletApp.activeToken
            if (token != null) {
                products.forEach { item ->
                    var product = productBox.query().equal(Product_.name, item.name).build().findFirst()

                    if (product != null) {
                        product.nfcTagId = item.nfcTagId
                        product.price = item.price
                        product.imagePath = item.imagePath
                    } else {
                        product = Product(0, item.name, item.price, item.imagePath, item.nfcTagId)
                    }
                    token.products.add(product)
                }
                tokenBox.put(token)
                EventBus.getDefault().post(ProductChangedEvent())
            } else {
                EventBus.getDefault().post(ErrorEvent("No active token available"))
            }

        }
    }
}