package io.lab10.vallet

import android.support.test.runner.AndroidJUnit4
import io.lab10.vallet.models.Products
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductsTest {

    @Before
    fun createProduct() {
        val id = "Piwo"
        val name = "Piwo"
        val price = 123456
        val image = "imagePath"
        var product = Products.Product(id, name, price, image)
        Products.addItem(product)
        Assert.assertEquals(Products.getProducts().first().id, product.id)

    }
    @Test
    fun product_asJson() {
        val json = "{\"id\":\"Piwo\",\"imagePath\":\"imagePath\",\"name\":\"Piwo\",\"price\":123456}"
        val product = Products.getProducts().first()
        Assert.assertEquals(product.toJson(), json)
    }

    @Test
    fun products_asJson() {

        Products.addItem(Products.Product("Chips", "Chipsy", 56434, "imagePath2" ))
        val json = "[{\"id\":\"Piwo\",\"imagePath\":\"imagePath\",\"name\":\"Piwo\",\"price\":123456},{\"id\":\"Chips\",\"imagePath\":\"imagePath2\",\"name\":\"Chipsy\",\"price\":56434}]"
        Assert.assertEquals(Products.toJson(), json)
    }

    @Test
    fun products_fromJson() {
        val json = "[{\"id\":\"Piwo\",\"imagePath\":\"imagePath\",\"name\":\"Piwo\",\"price\":123456},{\"id\":\"Chips\",\"imagePath\":\"imagePath2\",\"name\":\"Chipsy\",\"price\":56434}]"
        Products.fromJson(json)
        Assert.assertEquals(Products.getProducts().size, 2)
    }
}