package io.lab10.vallet.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Product(@Id var id: Long, var name: String, var price: Int, var imagePath: String, var localImagePath: String, var nfcTagId: String)  {

    override fun toString(): String {
        return name
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    fun isValid(): Boolean {
        return (name.isNotEmpty() && price > 0)
    }
}