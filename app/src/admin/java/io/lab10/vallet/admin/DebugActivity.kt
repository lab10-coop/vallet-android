package io.lab10.vallet.admin

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.lab10.vallet.R
import kotlinx.android.synthetic.admin.activity_debug.*

class DebugActivity : AppCompatActivity() {
    val TAG = DebugActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

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
    }
}
