package io.lab10.vallet.admin.activities

import android.content.Intent
import io.lab10.vallet.activites.BaseRestoreActivity

class RestoreActivity : BaseRestoreActivity() {

    override fun performRestore() {
        super.performRestore()
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
    }
}