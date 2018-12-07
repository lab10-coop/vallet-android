package io.lab10.vallet.models

import com.google.gson.Gson
import io.lab10.vallet.ValletApp

object History {

    fun getTransactions(): MutableList<ValletTransaction> {
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        return valletTransactionBox.query().orderDesc(ValletTransaction_.blockNumber).build().find()
    }

    fun getRecentOutgoing(): MutableList<ValletTransaction> {
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        return valletTransactionBox.query().greater(ValletTransaction_.value, 0).orderDesc(ValletTransaction_.blockNumber).build().find(0,10)
    }

    fun getRecent(): MutableList<ValletTransaction> {
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        return valletTransactionBox.query().orderDesc(ValletTransaction_.blockNumber).build().find(0,6)
    }

    fun addTransaction(item: ValletTransaction){
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        val builder = valletTransactionBox.query();
        builder.equal(ValletTransaction_.transactionId, item.transactionId)
        var transaction = builder.build().findFirst()
        if (transaction == null || item.transactionId.isBlank()) {
            transaction = item
        } else {
            transaction.blockNumber = item.blockNumber
            transaction.transactionId = item.transactionId
            if (item.name.isNotEmpty())
                transaction.name = item.name
        }
        valletTransactionBox.put(transaction)
    }

    fun clear() {
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        valletTransactionBox.removeAll()
    }

}