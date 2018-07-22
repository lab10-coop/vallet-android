package io.lab10.vallet.models

import io.lab10.vallet.TokenStorageBase
import io.lab10.vallet.storage.ApiStorage
import io.lab10.vallet.storage.IPFSStorage
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany


// Voucher type 0 = EUR, 1 = VOUCHER
// TODO use enum
@Entity
data class Token(@Id var id: Long = 0,
                 var name: String = "",
                 val tokenAddress: String = "",
                 var balance: Int = 0,
                 var tokenType: Int = 0,
                 var ipnsAdddress: String = "",
                 var default: Boolean = true,
                 var lastBlockNumber: Long = 0,
                 var secret: String = "") {

    @Backlink
    lateinit var products: ToMany<Product>

    fun storage() : TokenStorageBase {
        // TODO Allow to configure storage type
        if (true) {
            return ApiStorage(this)
        } else {
            return IPFSStorage(this)
        }
    }

    // Hack to work around GSON problems with converting relationships between objects
    fun productsBase() : List<ProductBase> {
        val productsBase: MutableList<ProductBase> = ArrayList()
        products.forEach { product ->
            val pb = ProductBase(product.name, product.price, product.imagePath, product.nfcTagId)
            productsBase.add(pb)
        }
        return productsBase
    }

    // Check if one any remote storage is already created
    fun remoteStoragePresent(): Boolean {
        return !secret.isNullOrEmpty() or !ipnsAdddress.isNullOrEmpty()
    }

}

// Data structures for retrofit to communicate with ValletAPI
// TODO we can not use token data class since gson is not handling ToMany relation ship so we need
// to deserialize it using other object and fill relations manually

// NOTICe keep in mind that those data structure should have attribute names of the API calls
data class TokenCreated(
        val secret: String,
        val token_name: String,
        val products: List<ProductBase>
)

data class TokenBase(
        val token_name: String,
        val token_type: Int,
        val products: List<ProductBase>
)

data class TokenUpdate(
        val secret: String,
        val token_name: String,
        val products: List<ProductBase>
)