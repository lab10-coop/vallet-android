package io.lab10.vallet.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ValletTransaction(@Id var id: Long, val name: String, val value: Long, val blockNumber: Long, val transactionId: String)