package io.lab10.vallet.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Wallet(@Id var id: Long = 0,
                  var name: String = "",
                  val address: String = "",
                  val filePath: String = "") {
    companion object Wallet {
        fun convertEUR2ATS(price: String): Int {
            // 1 ATS = 1 cent
            val value = price.toFloat()
            return (value * 100).toInt()
        }

        fun convertATS2EUR(ats: Long): Float {
            // 1 ATS = 1 cent
            return (ats / 100.0).toFloat()
        }
        fun isValidAddress(address: String): Boolean {
            if (address.length == 42) {
                return true
            }
            return false
        }

        fun formatAddress(userAddress: String?): String {
            if(userAddress != null) {
                if (userAddress.substring(0..1).equals("0x")) {
                    return userAddress
                } else {
                    return userAddress.drop(0).drop(0)
                }
            }
            throw IllegalArgumentException("Address required")
        }
    }
}