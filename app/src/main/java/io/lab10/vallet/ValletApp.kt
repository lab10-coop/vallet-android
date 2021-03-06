package io.lab10.vallet

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import io.lab10.vallet.activites.BackupActivity
import io.lab10.vallet.activites.BaseRestoreActivity
import io.lab10.vallet.models.MyObjectBox
import io.lab10.vallet.models.Token
import io.lab10.vallet.models.Token_
import io.lab10.vallet.models.Wallet
import io.objectbox.BoxStore

open class ValletApp : Application() {
    companion object {
        var box: BoxStore? = null
        var isAdmin: Boolean = false
        var activeToken: Token?
            get() {
                val tokenBox = getBoxStore().boxFor(Token::class.java)
                var at = tokenBox.query().equal(Token_.active, true).build().findFirst()
                // In case if there is no active token (at) we pick first and set it as active token
                if (at == null) {
                    var nat = tokenBox.query().build().findFirst()
                    if (nat != null) {
                        nat!!.active = true
                        tokenBox.put(nat)
                    }
                    return nat
                } else {
                    return at
                }
            }
            set(value) {
                val tokenBox = getBoxStore().boxFor(Token::class.java)
                val allTokens = tokenBox.query().build().find()
                // Reset current active tokens (there should be one but just in case we set for all)
                allTokens.forEach { p ->
                    p.active = false
                }
                tokenBox.put(allTokens)
                val activeToken = tokenBox.query().equal(Token_.id, (value as Token).id).build().findFirst()
                activeToken!!.active = true
                tokenBox.put(activeToken)
            }
        var wallet: Wallet?
            get() {
                val walletBox = getBoxStore().boxFor(Wallet::class.java)
                return walletBox.query().build().findFirst()
            }
            set(value) {
                val walletBox = getBoxStore().boxFor(Wallet::class.java)
                // TODO add support for multiple wallets
                walletBox.put((value as Wallet))
            }

        fun getBoxStore(): BoxStore {
            return box as BoxStore
        }

        fun initBox(context: Context) {
            try {
                if (box == null || box!!.isClosed) {
                    box = MyObjectBox.builder().androidContext(context).build()
                }
            } catch (e: java.lang.Exception) {
                Toast.makeText(context, "The database is corrupted can't start the app. Error: " + e.message, Toast.LENGTH_LONG).show()
                // Show dialog to reset the app?
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        initBox()
        isAdmin()
    }

    fun initBox() {
        try {
            if (box == null) {
                box = MyObjectBox.builder().androidContext(this).build()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "The database is corrupted can't start the app. Error: " + e.message, Toast.LENGTH_LONG).show()
            val intent = Intent(this, BackupActivity::class.java)
            startActivity(intent)
        }
    }

    fun isAdmin() {
        isAdmin = packageName.equals("io.lab10.vallet.admin")
    }
}