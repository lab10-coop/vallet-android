package io.lab10.vallet.utils

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class ValletCryptUtil {
    companion object {

        fun deriveKeyPbkdf2(salt: ByteArray, password: String, keyLength: Int): SecretKeySpec {
            val iterationCount = 1000

            val keySpec = PBEKeySpec(password.toCharArray(), salt,
                    iterationCount, keyLength)
            val keyFactory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = keyFactory.generateSecret(keySpec).getEncoded()
            return SecretKeySpec(keyBytes, "AES")
        }
    }
}