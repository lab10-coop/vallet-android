package io.lab10.vallet.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id


// Voucher type 0 = EUR, 1 = VOUCHER
// TODO use enum
@Entity
data class Voucher(@Id var id: Long = 0,
                   var name: String = "",
                   val tokenAddress: String = "",
                   var balance: Int = 0,
                   var type: Int = 0,
                   var ipnsAdddress: String = "",
                   var default: Boolean = true,
                   var lastBlockNumber: Long = 0)