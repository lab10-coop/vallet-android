package io.lab10.vallet

import android.content.Intent
import io.lab10.vallet.activites.BaseRestoreActivity

class RestoreActivity : BaseRestoreActivity() {

    override fun performRestore() {
        super.performRestore()
        val intent = Intent(this, ClientHomeActivity::class.java)
        startActivity(intent)
    }
}