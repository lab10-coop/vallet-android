package io.lab10.vallet

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import io.lab10.vallet.connectivity.BTUtils

import kotlinx.android.synthetic.client.activity_client.*
import org.web3j.tx.Contract
import java.io.File
import java.util.*


class ClientActivity : AppCompatActivity() {

    var voucherWalletAddress : String = "";
    val TAG = ClientActivity::class.java.simpleName

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
            editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_file), walletFile)
            editor.commit()
        }

        walletAddressLabel.text = voucherWalletAddress
        Log.i(TAG, "Wallet address: " + voucherWalletAddress)
        var walletBalance = Web3jManager.INSTANCE.getClientBalance(this, voucherWalletAddress)
        activeVouchersCount.text = walletBalance.toString()
    }

    fun startBroadcastingAddress() {
        // TODO make sure that BT is on if not turn it on
        Toast.makeText(this,"Broadcasting address", Toast.LENGTH_LONG).show()
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        val address = voucherWalletAddress
        // Address is always with 0x which we don't need to transfer
        val part1 = address.substring(2, BTUtils.SERVICE_NAME_SIZE)
        val part2 = address.substring(BTUtils.SERVICE_NAME_SIZE)
        val uuid1 = BTUtils.encodeAddress(part1)
        val uuid2 = BTUtils.encodeAddress(part2)
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            this.startActivityForResult(enableBtIntent, 1)
            // TODO handle turn on off in activity
            // see https://developer.android.com/guide/topics/connectivity/bluetooth.html
        } else {
            mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid1)
            mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid2)
        }
    }

}
