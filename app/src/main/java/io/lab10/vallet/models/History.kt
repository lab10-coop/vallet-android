package io.lab10.vallet.models

import com.google.gson.Gson
import java.math.BigInteger

object History {
    private val ITEMS: MutableList<Transaction> = ArrayList()

    fun getTransactions(): MutableList<Transaction> {
        return ITEMS;
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(ITEMS)
    }

    fun fromJson(json: String) {
        val gson = Gson()
        ITEMS.clear()
        var tmp: MutableList<Transaction> = ArrayList()
        val products = gson.fromJson(json, Array<Transaction>::class.java)
        products.forEach { v ->
            // TODO add check if voucher is valid?
            if (!isVoucherOnList(v)) {
                ITEMS.add(v)
            }
        }
    }

    fun addItem(item: Transaction){
        if (!isVoucherOnList(item)) {
            ITEMS.add(item)
        }
    }

    private fun isVoucherOnList(item: Transaction): Boolean {
        for(voucher in ITEMS) {
            if (voucher.id.equals(item.id)) {
                return true;
            }
        }
        return false;
    }


    class Transaction(val id: String, val name: String, val value: BigInteger) {

    }
}