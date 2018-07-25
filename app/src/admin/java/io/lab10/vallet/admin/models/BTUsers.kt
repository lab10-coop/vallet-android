package io.lab10.vallet.admin.models

import java.util.*

/**
 * Created by mtfk on 10.01.18.
 */
object BTUsers {

    /**
     * An array of users items.
     */
    private val ITEMS: MutableList<User> = ArrayList()

    fun getUsers(): MutableList<User> {
       return ITEMS;
    }

    fun addItem(item: User){
        if (!isUserOnList(item)) {
            ITEMS.add(item)
        }
    }

    private fun isUserOnList(item: User): Boolean {
        for(user in ITEMS) {
            if (user.id.equals(item.id)) {
                return true;
            }
        }
        return false;
    }

    class User(val id: String, val address: String, val name: String) {

        override fun toString(): String {
            return address
        }
    }
}
