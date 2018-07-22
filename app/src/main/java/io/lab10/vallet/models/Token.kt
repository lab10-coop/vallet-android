package io.lab10.vallet.models

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
                 val secret: String = "") {

    @Backlink
    lateinit var products: ToMany<Product>

}

// Data structures for retrofit to communicate with ValletAPI
data class TokenCreate(
        val tokenName: String,
        val tokenType: Integer,
        val products: List<Product>
)

data class TokenUpdate(
        val secret: String,
        val tokenName: String,
        val products: List<Product>
)