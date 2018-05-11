package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import io.lab10.vallet.R
import io.lab10.vallet.admin.DebugActivity
import kotlinx.android.synthetic.admin.activity_admin.*
import org.web3j.protocol.core.methods.response.EthGetBalance
import java.io.File


class AdminActivity : AppCompatActivity() {

    val TAG = AdminActivity::class.java.simpleName

    private var debugCount: Int = 0
    private var debugOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        voucherTypeIcon.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    debugCount += 1
                    if (debugCount > 5) {
                        debugOn = true
                    }
                }
                MotionEvent.ACTION_UP -> {

                    if (debugOn) {
                        val intent = Intent(this, DebugActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            return@OnTouchListener true
        })



        issueVouchersBtn.setOnClickListener() { v ->
            val intent = Intent(this, IssueVoucherActivity::class.java)
            startActivity(intent)
        }



        priceListBtn.setOnClickListener() { v ->
            val intent = Intent(this, PriceListActivity::class.java)
            startActivity(intent)
        }

        historyBtn.setOnClickListener() { v ->
            Web3jManager.INSTANCE.poolTokenCreateEvent(this)
        }

        createToken.setOnClickListener() { v ->
            val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val walletFile = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_wallet_file), "")
            if (walletFile != "") {
                val walletPath = File(this.filesDir, walletFile)
                var credentials = Web3jManager.INSTANCE.loadCredential("123", walletPath.absolutePath)
                Web3jManager.INSTANCE.generateNewToken(this, credentials, 2)
            }
        }

    }
}
