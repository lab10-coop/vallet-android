package io.lab10.vallet.models

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.NewTokenEvent
import io.lab10.vallet.events.ProductChangedEvent
import org.greenrobot.eventbus.EventBus

class PriceList(@Expose val token_name: String, @Expose val products: MutableList<Product>, @Expose val token_type: String, @Expose val token_contract_address: String) {

    fun toJson(): String {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        return gson.toJson(this)
    }


    companion object {
        fun fromJson(json: String, ipfsAddress: String) {
            val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            try {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val priceList = gson.fromJson(json, PriceList::class.java)
                var token = tokenBox.query().equal(Token_.tokenAddress, (priceList as PriceList).token_contract_address).build().findFirst()
                if (token != null) {
                    token.tokenType = priceList.token_type
                    token.name = priceList.token_name
                    token.products.clear()
                    priceList.products.forEach {
                        token!!.products.add(Product(0, it.name, it.price, it.imagePath, it.localImagePath, it.nfcTagId))
                    }
                    tokenBox.put(token)
                    EventBus.getDefault().post(ProductChangedEvent())
                } else {
                    token = Token(0, priceList.token_name, priceList.token_contract_address, 0, priceList.token_type, ipfsAddress, true, 0, "")
                    tokenBox.put(token)
                    EventBus.getDefault().post(NewTokenEvent())
                }

            } catch (err: Exception) {
                EventBus.getDefault().post(ErrorEvent("Can't parse json price list object for "+ ipfsAddress +". Contact Administrator"))
            }
        }
    }
}