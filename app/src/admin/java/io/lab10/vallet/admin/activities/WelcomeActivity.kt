package io.lab10.vallet.admin.activities

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import io.lab10.vallet.R


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val sharedPreferences = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val firstRun = sharedPreferences.getBoolean("FIRST_RUN", true)
        if (firstRun) {
            val intent = Intent(this, VoucherActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
    }
}
