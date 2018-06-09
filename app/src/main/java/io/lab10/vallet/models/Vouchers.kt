package io.lab10.vallet.models

import com.google.gson.Gson

object Vouchers {

    private val ITEMS: MutableList<Voucher> = ArrayList()

    enum class Type {
        EUR, VOUCHER
    }

    fun getProducts(): MutableList<Voucher> {
        return ITEMS;
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(ITEMS)
    }

    fun fromJson(json: String) {
        val gson = Gson()
        ITEMS.clear()
        var tmp: MutableList<Voucher> = ArrayList()
        val products = gson.fromJson(json, Array<Voucher>::class.java)
        products.forEach { v ->
            // TODO add check if voucher is valid?
            if (!isVoucherOnList(v)) {
                ITEMS.add(v)
            }
        }
    }

    fun addItem(item: Voucher){
        if (!isVoucherOnList(item)) {
            ITEMS.add(item)
        }
    }

    private fun isVoucherOnList(item: Voucher): Boolean {
        for(voucher in ITEMS) {
            if (voucher.id.equals(item.id)) {
                return true;
            }
        }
        return false;
    }

    class Voucher(val id: String, val name: String, val tokenAddress: String, val balance: Int) {

    }

}