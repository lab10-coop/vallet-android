package io.lab10.vallet.connectivity

import java.math.BigInteger
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by mtfk on 01.01.18.
 */

object BTUtils {


    internal fun decodeAddress(uuid: String): String? {
        val regex = "([a-f0-9]{8})-([a-f0-9]{4})-4([a-f0-9]{3})-8([a-f0-9]{3})-([a-f0-9]{8})c0de"
        val pattern = Pattern.compile(regex)
        val m = pattern.matcher(uuid.toLowerCase())
        if (m.find()) {
            val hexData = m.group(1) + m.group(2) + m.group(3) + m.group(4) +
                    m.group(5)
            return BigInteger(hexData, 16).toString(2)
        } else {
            return null
        }
    }

    // TODO: to fit whole eth address we need to create at least 2 services
    // until we will implement etheruem interface we just using short fake address.
    internal fun encodeAddress(data: String): UUID {

        val paddedData = String.format("%-14s", data).replace(' ', '\u0000')
        val hexData = String.format("%028x", BigInteger(1,
                paddedData.toByteArray()))

        return UUID.fromString( hexData.substring(0, 8) + "-" +
                hexData.substring(8, 12) + "-4" +
                hexData.substring(12, 15) + "-8" +
                hexData.substring(15, 18) + "-" +
                hexData.substring(18) + "eth1")

    }
}

