package io.lab10.vallet.utils

import java.lang.Exception
import java.math.BigInteger
import java.util.Arrays
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object Base58Util {
    private val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val BASE = BigInteger.valueOf(58)

    fun encode(input: ByteArray): String {
        // TODO: This could be a lot more efficient.
        var bi = BigInteger(1, input)
        val s = StringBuffer()
        while (bi.compareTo(BASE) >= 0) {
            val mod = bi.mod(BASE)
            s.insert(0, ALPHABET[mod.toInt()])
            bi = bi.subtract(mod).divide(BASE)
        }
        s.insert(0, ALPHABET[bi.toInt()])
        // Convert leading zeros too.
        for (anInput in input) {
            if (anInput.toInt() == 0)
                s.insert(0, ALPHABET[0])
            else
                break
        }
        return s.toString()
    }

    fun decode(input: String): ByteArray {
        val bytes = decodeToBigInteger(input).toByteArray()
        // We may have got one more byte than we wanted, if the high bit of the next-to-last byte was not zero. This
        // is because BigIntegers are represented with twos-compliment notation, thus if the high bit of the last
        // byte happens to be 1 another 8 zero bits will be added to ensure the number parses as positive. Detect
        // that case here and chop it off.
        val stripSignByte = bytes.size > 1 && bytes[0].toInt() == 0 && bytes[1] < 0
        // Count the leading zeros, if any.
        var leadingZeros = 0
        var i = 0
        while (input[i] == ALPHABET[0]) {
            leadingZeros++
            i++
        }
        // Now cut/pad correctly. Java 6 has a convenience for this, but Android can't use it.
        val tmp = ByteArray(bytes.size - (if (stripSignByte) 1 else 0) + leadingZeros)
        System.arraycopy(bytes, if (stripSignByte) 1 else 0, tmp, leadingZeros, tmp.size - leadingZeros)
        return tmp
    }

    fun decodeToBigInteger(input: String): BigInteger {
        var bi = BigInteger.valueOf(0)
        // Work backwards through the string.
        for (i in input.length - 1 downTo 0) {
            val alphaIndex = ALPHABET.indexOf(input[i])
            if (alphaIndex == -1) {
                throw Exception("Illegal character " + input[i] + " at " + i)
            }
            bi = bi.add(BigInteger.valueOf(alphaIndex.toLong()).multiply(BASE.pow(input.length - 1 - i)))
        }
        return bi
    }

    /**
     * Uses the checksum in the last 4 bytes of the decoded data to verify the rest are correct. The checksum is
     * removed from the returned data.
     *
     * @throws AddressFormatException if the input is not base 58 or the checksum does not validate.
     */
    fun decodeChecked(input: String): ByteArray {
        var tmp = decode(input)
        if (tmp.size < 4)
            throw Exception("Input too short")
        val checksum = ByteArray(4)
        System.arraycopy(tmp, tmp.size - 4, checksum, 0, 4)
        val bytes = ByteArray(tmp.size - 4)
        System.arraycopy(tmp, 0, bytes, 0, tmp.size - 4)
        tmp = doubleDigest(bytes)
        val hash = ByteArray(4)
        System.arraycopy(tmp, 0, hash, 0, 4)
        if (!Arrays.equals(hash, checksum))
            throw Exception("Checksum does not validate")
        return bytes
    }

    private fun doubleDigest(input: ByteArray): ByteArray {
        return doubleDigest(input, 0, input.size)
    }

    /**
     * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
     * standard procedure in BitCoin. The resulting hash is in big endian form.
     */
    private fun doubleDigest(input: ByteArray, offset: Int, length: Int): ByteArray {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(input, offset, length)
            val first = digest.digest()
            return digest.digest(first)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)  // Cannot happen.
        }

    }
}