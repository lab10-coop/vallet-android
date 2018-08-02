package io.lab10.vallet.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ValletTransaction(@Id var id: Long, var name: String, val value: Long, var blockNumber: Long, var transactionId: String, val to: String)
