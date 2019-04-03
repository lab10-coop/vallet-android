package io.lab10.vallet.activites

import Web3jManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.widget.Toast
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.TokenCreateEvent
import io.lab10.vallet.models.Wallet
import io.lab10.vallet.utils.ValletCryptUtil
import io.objectbox.BoxStore
import kotlinx.android.synthetic.main.activity_restore.*
import org.greenrobot.eventbus.EventBus
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

open class BaseRestoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore)

        btn_restore_now.setOnClickListener {
            performRestore()
        }
    }

    open fun performRestore() {
        try {
            if (edit_restore_secret.text.length < 10) {
                Toast.makeText(this, "Seems like the restore secret is empty, please paste it in the text area", Toast.LENGTH_LONG).show()
            } else {
                val decryptedSecrets = decrypt(edit_restore_secret.text.toString(), edit_password.text.toString())
                val fileName = String(Base64.decode(decryptedSecrets[0], Base64.NO_WRAP))
                val fileContent = String(Base64.decode(decryptedSecrets[1], Base64.NO_WRAP))
                val password = String(Base64.decode(decryptedSecrets[2], Base64.NO_WRAP))
                val name = String(Base64.decode(decryptedSecrets[3], Base64.NO_WRAP))
                val smartContractAddress = String(Base64.decode(decryptedSecrets[4], Base64.NO_WRAP))

                try {
                    ValletApp.getBoxStore().close()
                } catch (e: Exception) {
                    // If crash we don't care as there is no instance probably
                }
                // Try to clean db and restore new key
                BoxStore.deleteAllFiles(this, null)
                ValletApp.initBox(this)
                restoreWallet(password, fileName, fileContent, name, smartContractAddress)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Seems like the content is not valid backup", Toast.LENGTH_LONG).show()
        }
    }

    private fun decrypt(ciphertext: String, password: String): List<String> {
        val fields = ciphertext.split("]")

        val cipherBytes = Base64.decode(fields[0], Base64.NO_WRAP)
        val iv = Base64.decode(fields[1], Base64.NO_WRAP)
        val salt = Base64.decode(fields[2], Base64.NO_WRAP)
        val keyLength = 256

        val key = ValletCryptUtil.deriveKeyPbkdf2(salt, password, keyLength)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParams = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams)
        return try {
            val plaintext = cipher.doFinal(cipherBytes)
            String(plaintext, Charsets.UTF_8).split("]")
        } catch (e: Exception) {
            Toast.makeText(this, "Probably incorrect password or pasted secret is corrupted", Toast.LENGTH_LONG).show()
            arrayListOf()
        }
    }


    private fun restoreWallet(password: String, fileName: String, fileContent: String, name: String, smartContractAddress: String) {
        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)

        // Store password for the wallet file in shared pref in case if the db wil be corrupted that we can restore
        val editor = sharedPref.edit()
        editor.putString(resources.getString(R.string.shared_pref_wallet_password), password)
        editor.commit()

        val walletFile = Web3jManager.INSTANCE.restoreWallet(this, fileName, fileContent)
        val walletAddress = Web3jManager.INSTANCE.getWalletAddressFromFile(walletFile)
        ValletApp.wallet = Wallet(0, name, walletAddress, walletFile)

        val valletDecimal = 12
        EventBus.getDefault().post(TokenCreateEvent(smartContractAddress, name, name, valletDecimal.toBigInteger()))
    }

}
