package io.lab10.vallet.models

import com.google.gson.Gson
import io.lab10.vallet.ValletApp

object History {
    private val ITEMS: MutableList<ValletTransaction> = ArrayList()

    fun getTransactions(): MutableList<ValletTransaction> {
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        ITEMS.clear()
        ITEMS.addAll(valletTransactionBox.query().orderDesc(ValletTransaction_.blockNumber).build().find())
        return ITEMS
    }

    fun getRecent(): MutableList<ValletTransaction> {
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        return valletTransactionBox.query().orderDesc(ValletTransaction_.blockNumber).build().find(0,2)
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(ITEMS)
    }

    fun fromJson(json: String) {
        val gson = Gson()
        ITEMS.clear()
        var tmp: MutableList<ValletTransaction> = ArrayList()
        val products = gson.fromJson(json, Array<ValletTransaction>::class.java)
        products.forEach { v ->
            // TODO add check if voucher is valid?
            if (!isTranscationOnList(v)) {
                ITEMS.add(v)
            }
        }
    }

    fun addTransaction(item: ValletTransaction){
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        val transaction = valletTransactionBox.query().equal(ValletTransaction_.transactionId, item.transactionId).build().findFirst()
        if (transaction == null) {
            valletTransactionBox.put(item)
        }
    }

    fun reloadTransactions() {
        ITEMS.clear()
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        val transactions = valletTransactionBox.query().orderDesc(ValletTransaction_.blockNumber).build().find()
        ITEMS.addAll(transactions)
    }

    fun clear() {
        ITEMS.clear()
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        valletTransactionBox.removeAll()
    }
    private fun isTranscationOnList(item: ValletTransaction): Boolean {
        for(transcation in ITEMS) {
            if (transcation.id.equals(item.id)) {
                return true;
            }
        }
        return false;
    }


}