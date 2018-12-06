package io.lab10.vallet.models

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.NewTokenEvent
import io.lab10.vallet.events.ProductChangedEvent
import org.greenrobot.eventbus.EventBus

class PriceList(@Expose val tokenName: String, @Expose val products: MutableList<Product>, @Expose val tokenType: String, @Expose val tokenContractAddress: String) {

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
                var token = tokenBox.query().equal(Token_.tokenAddress, (priceList as PriceList).tokenContractAddress).build().findFirst()
                if (token != null) {
                    token.tokenType = priceList.tokenType
                    token.name = priceList.tokenName
                    token.products.clear()
                    priceList.products.forEach {
                        token!!.products.add(Product(0, it.name, it.price, it.imagePath, it.localImagePath, it.nfcTagId))
                    }
                    tokenBox.put(token)
                    EventBus.getDefault().post(ProductChangedEvent())
                } else {
                    token = Token(0, priceList.tokenName, priceList.tokenContractAddress, 0, priceList.tokenType, ipfsAddress, true, 0, "")
                    tokenBox.put(token)
                    EventBus.getDefault().post(NewTokenEvent())
                }

            } catch (err: Exception) {
                EventBus.getDefault().post(ErrorEvent("Can't parse json price list object for "+ ipfsAddress +". Contact Administrator"))
            }
        }
    }
}