package io.lab10.vallet.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import io.lab10.vallet.ValletApp
import io.objectbox.annotation.Entity

/**
 * Created by mtfk on 11.02.18.
 */
object Products {

    /**
     * An array of product items.
     * TODO: add tests
     */

    private val ITEMS: MutableList<Product> = ArrayList()
    private val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)

    fun getProducts(): MutableList<Product> {
        return ITEMS
    }

    fun refresh(token: Token) {
        ITEMS.clear()
        ITEMS.addAll(token.products)
    }

   /* fun toJson(): String {
        val gson = Gson()
        return gson.toJson(getProducts())
    }

    fun fromJson(json: String, tokenAddress: String, clean: Boolean = false) {
        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        val token = tokenBox.query().equal(Token_.tokenAddress, tokenAddress).build().findFirst()
        val gson = Gson()
        val products = gson.fromJson(json, Array<Product>::class.java)
        if (clean)
            token!!.products.clear()
        products.forEach { v ->
            v.id = 0
            token!!.products.add(v)
        }
    }*/
}