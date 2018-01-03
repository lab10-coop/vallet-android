package io.lab10.vallet.admin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.content.Intent
import io.lab10.vallet.R


class WelcomeActivity : AppCompatActivity() {

    val FIRST_RUN = "FIRST_RUN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val firstRun = sharedPreferences.getBoolean("FIRST_RUN", true)
        if (firstRun) {
            // TODO Start VoucherActivity
            val intent = Intent(this, VoucherActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
    }
}
