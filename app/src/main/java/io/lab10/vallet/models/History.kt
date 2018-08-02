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
        val builder = valletTransactionBox.query();
        builder.equal(ValletTransaction_.transactionId, item.transactionId)
        var transaction = builder.build().findUnique()
        if (transaction == null) {
            // Find if the pending transaction is already in place, should always be the case
            val builder = valletTransactionBox.query();
            builder.equal(ValletTransaction_.to, item.to).equal(ValletTransaction_.value, item.value.toLong())
            var transaction = builder.build().findFirst()
            if (transaction != null) {
                transaction.name = item.name
                transaction.blockNumber = item.blockNumber.toLong()
                transaction.transactionId = item.transactionId

            } else {
                transaction = item
            }
            valletTransactionBox.put(transaction)
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