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
import io.lab10.vallet.utils.ValletCryptUtil
import kotlinx.android.synthetic.main.activity_backup.*
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class BackupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        btn_backup_now.setOnClickListener {
            performBackup()
        }

    }

    private fun performBackup() {
        if (edit_backup_password.text.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show()
        } else {
            val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val keyPass = sharedPref.getString(resources.getString(R.string.shared_pref_wallet_password), "")
            val fileName = ValletApp.wallet!!.filePath
            // In case of Admin app it is store name, for user app it is user name
            val name = ValletApp.wallet!!.name
            var smartContractAddress = ""
            if (ValletApp.activeToken != null) {
                smartContractAddress = ValletApp.activeToken!!.tokenAddress
            }
            val walletFileContent = File(filesDir, fileName).inputStream().readBytes().toString(Charsets.UTF_8)
            var inputForEncryption = String(Base64.encode(fileName.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)) + "]"
            inputForEncryption += String(Base64.encode(walletFileContent.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)) + "]"
            inputForEncryption += String(Base64.encode(keyPass.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)) + "]"
            inputForEncryption += String(Base64.encode(name.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)) + "]"
            inputForEncryption += String(Base64.encode(smartContractAddress.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))

            val secureKey = encrypt(inputForEncryption, edit_backup_password.text.toString())

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Vallet", secureKey)
            clipboard!!.setPrimaryClip(clip)
            Toast.makeText(this, "Encrypted key was copied to your clipboard", Toast.LENGTH_LONG).show()
        }
    }

    private fun encrypt(secret: String, password: String): String {
        val keyLength = 256
        val saltLength = keyLength / 8 // same size as key output

        val random = SecureRandom()
        val salt = ByteArray(saltLength)
        random.nextBytes(salt)
        val key = ValletCryptUtil.deriveKeyPbkdf2(salt, password, keyLength)
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

}
