package io.lab10.vallet.models

import com.google.gson.Gson
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
data class Product(@Id var id: Long = 0,
                   var name: String = "",
                   var price: Long = 0,
                   var imagePath: String = "",
                   var localImagePath: String = "",
                   var nfcTagId: String? = "")  {

    lateinit var token: ToOne<Token>

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

// data structure for retrofit to avoid loops with gson as it has problems with handling ToOne/ToMany
data class ProductBase(
        val name: String,
        val price: Long,
        val imagePath: String,
        val nfcTagId: String?
)