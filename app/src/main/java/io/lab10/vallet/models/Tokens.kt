package io.lab10.vallet.models

import io.lab10.vallet.ValletApp

object Tokens {

    enum class Type(val type: String) {
        EUR("V-EUR"), VOUCHER("Voucher")
    }

    fun getVouchers(): MutableList<Token> {
        return  ValletApp.getBoxStore().boxFor(Token::class.java).query().build().find()
    }

}