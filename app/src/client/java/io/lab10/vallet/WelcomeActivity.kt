package io.lab10.vallet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
