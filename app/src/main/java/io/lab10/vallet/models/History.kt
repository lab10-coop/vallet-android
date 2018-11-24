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
        return valletTransactionBox.query().orderDesc(ValletTransaction_.blockNumber).build().find(0,10)
    }

    fun addTransaction(item: ValletTransaction){
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        val builder = valletTransactionBox.query();
        builder.equal(ValletTransaction_.transactionId, item.transactionId)
        var transaction = builder.build().findUnique()
        if (transaction == null || item.transactionId.isBlank()) {
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

    fun clear() {
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        valletTransactionBox.removeAll()
    }

}