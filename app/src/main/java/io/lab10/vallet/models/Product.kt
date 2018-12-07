package io.lab10.vallet.models

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
data class Product(@Expose @Id var id: Long = 0,
                   @Expose var name: String = "",
                   @Expose var price: Long = 0,
                   @Expose var imagePath: String = "",
                   @Expose var nfcTagId: String? = "")  {

    lateinit var token: ToOne<Token>

    override fun toString(): String {
        return name
    }

    fun toJson(): String {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        return gson.toJson(this)
    }

    fun isValid(): Boolean {
        return (name.isNotEmpty() && price > 0)
    }
}

// TODO could we replace that with Expose annotaion or other exclusion strategy?
// data structure for retrofit to avoid loops with gson as it has problems with handling ToOne/ToMany
data class ProductBase(
        val name: String,
        val price: Long,
        val imagePath: String,
        val nfcTagId: String?
)