package io.lab10.vallet.manager

import io.lab10.vallet.ValletApp
import io.lab10.vallet.interfaces.ValletApiService
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.NewTokenEvent
import io.lab10.vallet.events.ProductChangedEvent
import io.lab10.vallet.models.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class PriceListManager {

    companion object {
        fun createPriceList(token: TokenBase) {
            // TODO extract that to settings
            val apiService = ValletApiService.create("https://vallet.mars.lab10.io")

            apiService.createPriceList(token).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn {
                        EventBus.getDefault().post(ErrorEvent(it.message.toString()))
                        TokenCreated()
                    }
                    .subscribe ({
                        result ->
                        val response = (result as TokenCreated)
                        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
                        // TODO support multiple tokens
                        val token = tokenBox.query().build().findFirst()
                        if (token != null && response.secret != null) {
                            token.secret = response.secret
                            tokenBox.put(token)
                        }
                    }, { error ->
                        EventBus.getDefault().post(ErrorEvent("Create Price List:" + error.message.toString()))
                    })
        }

        fun fetchPriceList(tokenContractAddress: String) {
            val apiService = ValletApiService.create("https://vallet.mars.lab10.io")
            apiService.fetchPriceList(tokenContractAddress).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn {
                        EventBus.getDefault().post(ErrorEvent(it.message.toString()))
                        TokenBase()
                    }
                    .subscribe ({
                        result ->
                        val response = (result as TokenBase)
                        if (response.token_name != null && response.token_contract_address != null && response.products != null && response.token_type != null)
                            updateLocalDb(response.token_name, response.token_type, response.token_contract_address, response.products)
                    }, { error ->
                        EventBus.getDefault().post(ErrorEvent(error.message.toString()))
                    })
        }

        fun updatePriceList(token: TokenUpdate) {
            val apiService = ValletApiService.create("https://vallet.mars.lab10.io")
            apiService.updatePriceList(token).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn {
                        EventBus.getDefault().post(ErrorEvent(it.message.toString()))
                        TokenBase()
                    }
                    .subscribe ({
                        result ->
                        val response = (result as TokenBase)
                        if (response.token_name != null && response.token_contract_address != null && response.products != null && response.token_type != null)
                            updateLocalDb(response.token_name, response.token_type, response.token_contract_address, response.products)
                    }, { error ->
                        EventBus.getDefault().post(ErrorEvent(error.message.toString()))
                    })
        }

        private fun updateLocalDb(tokenName: String, tokenType: String, tokenAddres: String, products: List<ProductBase>) {
            val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            var token = tokenBox.query().equal(Token_.tokenAddress, tokenAddres).build().findFirst()
            var isNewToken = false
            if (token == null) {
                token = Token(0, tokenName, tokenAddres, 0, tokenType, "", false, 0, "")
                isNewToken = true
            }

            token.products.clear()
            products.forEach { item ->
                val product = Product(0, item.name, item.price, item.imagePath, "", item.nfcTagId)
                token.products.add(product)
            }
            tokenBox.put(token)
            EventBus.getDefault().post(ProductChangedEvent())
            if(isNewToken)
                EventBus.getDefault().post(NewTokenEvent())

        }
    }
}