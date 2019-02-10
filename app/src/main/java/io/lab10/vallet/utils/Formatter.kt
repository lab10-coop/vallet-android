package io.lab10.vallet.utils

import java.text.DecimalFormat

class Formatter {

    companion object {
        fun currency(value: Float) : String {
            val formatter = DecimalFormat("###,###.00")
            return "¤" + formatter.format(value)
        }
    }
}