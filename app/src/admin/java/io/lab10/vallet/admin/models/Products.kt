package io.lab10.vallet.admin.models

/**
 * Created by mtfk on 11.02.18.
 */
object Products {

    /**
     * An array of product items.
     * TODO: add tests
     */
    private val ITEMS: MutableList<Product> = ArrayList()

    fun getProducts(): MutableList<Product> {
        return ITEMS;
    }

    fun addItem(item: Product){
        if (!isProductOnList(item)) {
            ITEMS.add(item)
        }
    }

    private fun isProductOnList(item: Product): Boolean {
        for(user in ITEMS) {
            if (user.id.equals(item.id)) {
                return true;
            }
        }
        return false;
    }

    class Product(val id: String, val name: String, val price: Int) {

        override fun toString(): String {
            return name
        }
    }
}