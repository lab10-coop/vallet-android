package io.lab10.vallet.models

import io.lab10.vallet.ValletApp
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ValletTransaction(@Id var id: Long, var name: String, val value: Long, var blockNumber: Long, var transactionId: String, val to: String, val tokenAddress: String) {

    fun description(): String {
        if(ValletApp.isAdmin) {
            val userBox = ValletApp.getBoxStore().boxFor(User::class.java)
            var user = userBox.query().equal(User_.address, this.to).build().findFirst()
            var what = "Sent to "
            if (this.value < 0)
                what = "Spent by "
            var text = what + this.to.subSequence(0, 5)

            if (user != null) {
                text = what + user.name
            }
            if (this.blockNumber == Long.MAX_VALUE) {
                text += " ..."
            }
            return text
        } else {
            var what = "Received"
            if (this.value < 0) {
                if (name.isNotEmpty()) {
                    if (name.length > 15) {
                        what = "Spent on " + name.slice(IntRange(0, 15)) + "..."
                    } else {
                        what = "Spent on $name"
                    }
                } else {
                    what = "Spent"
                }
            }
            return what
        }
    }
}
