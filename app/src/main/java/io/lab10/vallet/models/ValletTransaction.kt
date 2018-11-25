package io.lab10.vallet.models

import io.lab10.vallet.ValletApp
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ValletTransaction(@Id var id: Long, var name: String, val value: Long, var blockNumber: Long, var transactionId: String, val to: String) {

    fun description(): String {
        if(ValletApp.isAdmin) {
            val userBox = ValletApp.getBoxStore().boxFor(User::class.java)
            var user = userBox.query().equal(User_.address, this.to).build().findFirst()
            var what = "Sent to "
            if (this.value < 0)
                what = "Spend by "
            var text = what + this.to.subSequence(0, 5)

            if (user != null) {
                text = what + user.name
            }
            return text
        } else {
            return name
        }
    }
}
