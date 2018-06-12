package io.lab10.vallet.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Voucher(@Id var id: Long, val name: String, val tokenAddress: String, val balance: Int)
