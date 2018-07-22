package io.lab10.vallet.admin.activities.manager

import android.util.Log
import io.lab10.vallet.ValletApp
import io.lab10.vallet.admin.interfaces.ValletApiService
import io.lab10.vallet.events.ErrorEvent
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
                        if ((error as HttpException).code() == 404) {
                            // For some reason the secret token does not exist on server we need to recreate it
                            val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
                            // TODO add support for multiple tokens
                            val token = tokenBox.query().build().findFirst()
                            token!!.storage().create()

                        }
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

            products.forEach { item ->
                var product = productBox.query().equal(Product_.name, item.name).build().findFirst()
                val token = getActiveToken()
                if (product != null) {
                    product.nfcTagId = item.nfcTagId
                    product.price = item.price
                    product.imagePath = item.imagePath
                } else {
                    product = Product(0, item.name, item.price, item.imagePath, item.nfcTagId)
                }
                token.products.add(product)
            }
        }

        private fun getActiveToken(): Token {
            val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            // TODO add support for multiple tokens
            return tokenBox.query().build().findFirst()!!
        }
    }
}