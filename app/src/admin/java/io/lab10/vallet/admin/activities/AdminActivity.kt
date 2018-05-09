package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.lab10.vallet.R
import kotlinx.android.synthetic.admin.activity_admin.*
import org.web3j.protocol.core.methods.response.EthGetBalance
import java.io.File


class AdminActivity : AppCompatActivity() {

    val TAG = AdminActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val voucherName = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_name), "")
        val voucherWalletAddress = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_wallet_address), "0x0")
        voucherNameLabel.text = voucherName.toString()
        voucherWalletAddresLabel.text = voucherWalletAddress.toString()
        try {
            var walletBalance = Web3jManager.INSTANCE.getBalance(this, voucherWalletAddress)
            voucherWalletBalanceLabel.text = walletBalance.balance.toString()
        } catch (e: Exception) {
            // TODO inform user about fail balance sync
            voucherWalletBalanceLabel.text = "0e"
        }
        voucherContractAddresLabel.text = sharedPref.getString(resources.getString(R.string.shared_pref_token_contract_address), "0x0")
        voucherContractAddresLabel.text = sharedPref.getString(resources.getString(R.string.shared_pref_token_contract_address), "0x0")


        Log.i(TAG, "Voucher contract address: " + voucherContractAddresLabel.text)
        Log.i(TAG, "Wallet address: " + voucherWalletAddress )

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
