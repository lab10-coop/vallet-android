package io.lab10.vallet.admin.models

import android.os.Parcel
import android.os.Parcelable

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

    class Product(val id: String, val name: String, val price: Int, val imagePath: String) : Parcelable {

        override fun toString(): String {
            return name
        }


        private constructor(p: Parcel) : this(
                id = p.readString(),
                name = p.readString(),
                price = p.readInt(),
                imagePath = p.readString())

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(id)
            dest.writeString(name)
            dest.writeInt(price)
            dest.writeString(imagePath)
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