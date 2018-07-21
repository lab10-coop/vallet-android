package io.lab10.vallet.models

data class PriceList (
    val token_name: String,
    val secret: String,
    val products: List<Product>
)

data class PriceListCreate(
        val token_name: String,
        val products: List<Product>
)