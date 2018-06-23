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

    fun refresh(tokenAddress: String) {
        ITEMS.clear()
        ITEMS.addAll(productBox.query().equal(Product_.token, tokenAddress).build().find())
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(getProducts())
    }

    fun fromJson(json: String, tokenAddress: String, clean: Boolean = false) {
        val gson = Gson()
        val products = gson.fromJson(json, Array<Product>::class.java)
        if (clean)
            productBox.query().equal(Product_.token, tokenAddress).build().remove()
        products.forEach { v ->
            // TODO add check if product is valid?
            v.token = tokenAddress
            v.id = 0
            if (!isProductOnList(v)) {
                val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
                productBox.put(v)
            }
        }
    }

    private fun isProductOnList(item: Product): Boolean {
        val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
        val product = productBox.query().equal(Product_.name, item.name).equal(Product_.token, item.token).build().findFirst()
        return product != null
    }
}