package io.lab10.vallet.admin.models

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by mtfk on 10.01.18.
 */
object Users {

    /**
     * An array of users items.
     */
    val ITEMS: MutableList<User> = ArrayList()

    private fun addItem(item: User) {
        ITEMS.add(item)
    }

    class User(val id: String, val address: String, val name: String) {

        override fun toString(): String {
            return address
        }
    }
}
