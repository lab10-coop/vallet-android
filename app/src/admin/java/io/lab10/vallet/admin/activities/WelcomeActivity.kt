package io.lab10.vallet.admin.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.models.Token


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())

        setContentView(R.layout.activity_welcome)

        if (ValletApp.activeToken != null) {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, CreateTokenActivity::class.java)
            startActivity(intent)
        }
    }
}
