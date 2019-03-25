package io.lab10.vallet.activites

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.models.Configuration
import io.lab10.vallet.models.Configuration_
import kotlinx.android.synthetic.main.activity_backup.*
import java.io.File
import java.lang.Exception
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        btn_backup_now.setOnClickListener {
            performBackup()
        }

        btn_restore_now.setOnClickListener {
            performRestore()
        }
    }

    private fun performBackup() {
        if (edit_backup_password.text.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show()
        } else {
            val configBox = ValletApp.getBoxStore().boxFor(Configuration::class.java)
            val keyPass = configBox.query().equal(Configuration_.name, "walletPassword").build().findFirst()!!.value
            val fileName = ValletApp.wallet!!.filePath
            val secret = File(filesDir, fileName).inputStream().readBytes().toString(Charsets.UTF_8)
            var inputForEncryption = String(Base64.encode(fileName.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)) + "]"
            inputForEncryption += String(Base64.encode(secret.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)) + "]"
            inputForEncryption += String(Base64.encode(keyPass.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))

            val secureKey = encrypt(inputForEncryption, edit_backup_password.text.toString())

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Vallet", secureKey)
            clipboard!!.setPrimaryClip(clip)
            Toast.makeText(this, "Encrypted key was copied to your clipboard", Toast.LENGTH_LONG).show()
        }
    }

    private fun performRestore() {
        if (edit_restore_secret.text.length < 10 ) {
            Toast.makeText(this, "Seems like the restore secret is empty, please paste it in the text area", Toast.LENGTH_LONG).show()
        } else {
            val decryptedSecrets = decrypt(edit_restore_secret.text.toString(), edit_backup_password.text.toString())
            Toast.makeText(this, String(Base64.decode(decryptedSecrets[0], Base64.NO_WRAP)), Toast.LENGTH_LONG).show()
        }
    }
    private fun encrypt(secret: String, password: String): String {
        val keyLength = 256
        val saltLength = keyLength / 8 // same size as key output

        val random = SecureRandom()
        val salt = ByteArray(saltLength)
        random.nextBytes(salt)
        val key = deriveKeyPbkdf2(salt, password, keyLength)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        val iv = ByteArray(cipher.getBlockSize())
        random.nextBytes(iv);
        val ivParams = IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        val ciphertext = cipher.doFinal(secret.toByteArray(Charsets.UTF_8))
        var encoded_secret = String(Base64.encode(ciphertext, Base64.NO_WRAP))
        val encoded_iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encoded_salt = Base64.encodeToString(salt, Base64.NO_WRAP)
        val result = encoded_secret + "]" + encoded_iv + "]" + encoded_salt
        return result

    }

    private fun decrypt(ciphertext: String, password: String): List<String> {
        val fields = ciphertext.split("]")

        val cipherBytes = Base64.decode(fields[0], Base64.NO_WRAP)
        val iv = Base64.decode(fields[1], Base64.NO_WRAP)
        val salt = Base64.decode(fields[2], Base64.NO_WRAP)
        val keyLength = 256

        val key = deriveKeyPbkdf2(salt, password, keyLength)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams)
        return try {
            val plaintext = cipher.doFinal(cipherBytes)
            String(plaintext, Charsets.UTF_8).split("]")
        } catch(e: Exception) {
            Toast.makeText(this, "Probably incorrect password or pasted secret is corrupted", Toast.LENGTH_LONG).show()
            arrayListOf()
        }
    }

    private fun deriveKeyPbkdf2(salt: ByteArray, password: String, keyLength: Int): SecretKeySpec {
        val iterationCount = 1000

        val keySpec = PBEKeySpec(password.toCharArray(), salt,
                iterationCount, keyLength)
        val keyFactory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = keyFactory.generateSecret(keySpec).getEncoded()
        return SecretKeySpec(keyBytes, "AES")
    }
}
