package io.lab10.vallet

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.lab10.vallet.connectivity.BTUtils

import kotlinx.android.synthetic.client.activity_client.*
import java.io.File


class ClientActivity : AppCompatActivity() {

    var voucherWalletAddress : String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        productListBtn.setOnClickListener() { v ->
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        }

        requestTokenBtn.setOnClickListener() { v ->
            startBroadcastingAddress()
        }

        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        voucherWalletAddress = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_wallet_address), "")

        if (voucherWalletAddress.equals("")) {
            val editor = sharedPref.edit()
            val walletFile = Web3jManager.INSTANCE.createWallet(this, "123")
            voucherWalletAddress = Web3jManager.INSTANCE.getWalletAddress(walletFile)
            editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_address), voucherWalletAddress)
            editor.commit()
        }

        walletAddressLabel.text = voucherWalletAddress
    }

    fun startBroadcastingAddress() {
        // TODO make sure that BT is on if not turn it on
        Toast.makeText(this,"Broadcasting address", Toast.LENGTH_LONG).show()
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        val address = voucherWalletAddress
        val uuid = BTUtils.encodeAddress(address)
        val btSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(getString(R.string.app_name), uuid)

    }

}
