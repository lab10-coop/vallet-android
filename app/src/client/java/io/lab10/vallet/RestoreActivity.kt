package io.lab10.vallet

import android.content.Intent
import android.widget.Toast
import io.lab10.vallet.activites.BaseRestoreActivity

class RestoreActivity : BaseRestoreActivity() {

    override fun performRestore() {
        try {
            super.performRestore()
            val intent = Intent(this, ClientHomeActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Seems like the content is not valid backup", Toast.LENGTH_LONG).show()
        }
    }
}