package io.lab10.vallet.admin.models

import android.content.Context
import io.lab10.vallet.R

class Wallet {
    companion object Wallet {
        fun isEuroType(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val voucher_type = sharedPref.getString(context.resources.getString(R.string.shared_pref_voucher_type), Voucher.Type.EUR.toString())
            return voucher_type.equals(Voucher.Type.EUR.toString())
        }
        fun convertEUR2ATS(price: String): Int {
            // 1 ATS = 1 cent
            val value = price.toFloat()
            return (value * 100).toInt()
        }
    }
}