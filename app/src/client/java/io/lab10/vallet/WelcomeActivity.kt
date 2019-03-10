package io.lab10.vallet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())

        // Initialization of ObjectBox should happen always when the app starts
        // But for some reason during the lifecycle of the app happens that the onCreate()
        // is not triggered and the box is null. To avoid that issue we are triggering initialization
        // manually while the screens pops up.
        ValletApp.initBox(this)

        setContentView(R.layout.activity_welcome)

        if (ValletApp.wallet != null) {
            val intent = Intent(this, ClientHomeActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }
    }
}
