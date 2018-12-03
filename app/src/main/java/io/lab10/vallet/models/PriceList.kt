package io.lab10.vallet.models

import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

class PriceList(@Expose val tokenName: String, @Expose val products: MutableList<Product>, @Expose val tokenType: String, @Expose val tokenContractAddress: String) {

    fun toJson(): String {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        return gson.toJson(this)
    }
}