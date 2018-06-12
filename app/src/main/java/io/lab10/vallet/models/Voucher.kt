package io.lab10.vallet.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id


// Voucher type 0 = EUR 1 = VOUCHER
// TODO use enum
@Entity
data class Voucher(@Id var id: Long, var name: String, val tokenAddress: String, var balance: Int, var type: Int, var ipfsAdddress: String)
