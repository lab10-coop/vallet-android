package io.lab10.vallet.admin.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.models.Token


class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        var voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        var voucher = voucherBox.query().build().find()

        // TODO try to fetch from network
        if (voucher.size > 0) {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, CreateTokenActivity::class.java)
            startActivity(intent)
        }
    }
}
