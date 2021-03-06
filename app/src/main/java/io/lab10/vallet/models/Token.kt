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
                 var balance: Long = 0,
                 var tokenType: String = Tokens.Type.EUR.toString(),
                 var ipnsAddress: String = "",
                 var active: Boolean = true,
                 var lastBlockNumber: Long = 0,
                 var secret: String = "") {

    @Backlink
    lateinit var products: ToMany<Product>

    fun storage() : TokenStorageBase {
        // TODO Allow to configure storage type
        if (false) {
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

    // Check if any remote storage is already created
    // Use by admin to store data
    fun remoteWriteStoragePresent(): Boolean {
        return !secret.isNullOrEmpty() or !ipnsAddress.isNullOrEmpty()
    }

    // Use for reading by client
    fun remoteReadStoragePresent() : Boolean {
        return !tokenAddress.isNullOrEmpty() or !ipnsAddress.isNullOrEmpty()
    }

}

// Data structures for retrofit to communicate with ValletAPI
// TODO we can not use token data class since gson is not handling ToMany relation ship so we need
// to deserialize it using other object and fill relations manually

// NOTICE keep in mind that those data structure should have attribute names of the API calls
data class TokenCreated(
        val secret: String? = null,
        val token_name: String? = null,
        val products: List<ProductBase>? = null,
        val token_type: String? = null
)

data class TokenBase(
        val token_name: String? = null,
        val token_type: String? = null,
        val token_contract_address: String? = null,
        val products: List<ProductBase>? = null
)

data class TokenUpdate(
        val secret: String,
        val token_name: String,
        val products: List<ProductBase>
)