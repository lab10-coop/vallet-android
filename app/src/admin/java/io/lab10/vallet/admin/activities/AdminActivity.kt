package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import kotlinx.android.synthetic.admin.activity_admin.*


class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val voucherName = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_name), "")
        voucherNameLabel.text = voucherName.toString()

        issueVouchersBtn.setOnClickListener() { v ->
            val intent = Intent(this, IssueVoucherActivity::class.java)
            startActivity(intent)
        }

    }
}
