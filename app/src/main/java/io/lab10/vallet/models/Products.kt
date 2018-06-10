package io.lab10.vallet.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson

/**
 * Created by mtfk on 11.02.18.
 */
object Products {

    /**
     * An array of product items.
     * TODO: add tests
     */
    private val ITEMS: MutableList<Product> = ArrayList()

    fun getProducts(): MutableList<Product> {
        return ITEMS;
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(ITEMS)
    }

    fun fromJson(json: String) {
        val gson = Gson()
        ITEMS.clear()
        var tmp: MutableList<Product> = ArrayList()
        val products = gson.fromJson(json, Array<Product>::class.java)
        products.forEach { v ->
            // TODO add check if product is valid?
            if (!isProductOnList(v)) {
                ITEMS.add(v)
            }
        }
    }

    fun addItem(item: Product){
        if (!isProductOnList(item)) {
            ITEMS.add(item)
        }
    }

    private fun isProductOnList(item: Product): Boolean {
        for(user in ITEMS) {
            if (user.id.equals(item.id)) {
                return true;
            }
        }
        return false;
    }

    class Product(val id: String, val name: String, val price: Int, val imagePath: String, val nfcTagId: String) : Parcelable {

        override fun toString(): String {
            return name
        }

        fun toJson(): String {
            val gson = Gson()
            return gson.toJson(this)
        }


        private constructor(p: Parcel) : this(
                id = p.readString(),
                name = p.readString(),
                price = p.readInt(),
                imagePath = p.readString(),
                nfcTagId = p.readString()
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(id)
            dest.writeString(name)
            dest.writeInt(price)
            dest.writeString(imagePath)
            dest.writeString(nfcTagId)
        }

        override fun describeContents() = 0

        fun isValid(): Boolean {
            if (name != null && price > 0) {
                return true
            } else {
                return false
            }
        }
        companion object {
            @JvmField val CREATOR = object : Parcelable.Creator<Product> {
                override fun createFromParcel(parcel: Parcel) = Product(parcel)

                override fun newArray(size: Int) = arrayOfNulls<Product>(size)
            }
        }
    }
}