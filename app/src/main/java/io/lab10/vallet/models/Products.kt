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
    fun getProducts(): MutableList<Product> {
        val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
        return productBox.query().build().find()
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(getProducts())
    }

    fun fromJson(json: String) {
        val gson = Gson()
        var tmp: MutableList<Product> = ArrayList()
        val products = gson.fromJson(json, Array<Product>::class.java)
        products.forEach { v ->
            // TODO add check if product is valid?
            if (!isProductOnList(v)) {
                val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
                productBox.put(v)
            }
        }
    }

    private fun isProductOnList(item: Product): Boolean {
        val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
        val product = productBox.query().equal(Product_.id, item.id).build().findFirst()
        return product != null
    }
}