package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.models.Voucher
import kotlinx.android.synthetic.admin.activity_debug.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DebugActivity : AppCompatActivity() {
    val TAG = DebugActivity::class.java.simpleName

    lateinit var sharedPref: SharedPreferences
    var voucher: Voucher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        var voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        voucher = voucherBox.query().build().findFirst()

        refreshAll()

        saveButton.setOnClickListener() { v ->
            val editor = sharedPref.edit()
            if (apiServerAddressInput.text.toString().length > 0)
                editor.putString(resources.getString(R.string.shared_pref_vallet_api_server_address), apiServerAddressInput.text.toString())
            if (serverIp.text.toString().length > 0)
                editor.putString(resources.getString(R.string.shared_pref_artis_node_address), serverIp.text.toString())
            if (contractAddress.text.toString().length > 0)
                editor.putString(resources.getString(R.string.shared_pref_factory_contract_address), contractAddress.text.toString())
            if (ipfsAddressInput.text.toString().length > 0)
                editor.putString(resources.getString(R.string.shared_pref_ipfs_address), ipfsAddressInput.text.toString())
            editor.commit()
            contractAddressValue.text = Web3jManager.INSTANCE.getContractAddress(this)
            refreshAll()
        }

        resetButton.setOnClickListener() { v ->
            val editor = sharedPref.edit()
            editor.remove(resources.getString(R.string.shared_pref_artis_node_address))
            editor.remove(resources.getString(R.string.shared_pref_factory_contract_address))
            editor.remove(resources.getString(R.string.shared_pref_vallet_api_server_address))
            editor.commit()
            refreshAll()
        }

        generateTokenButton.setOnClickListener() { v ->
            val intent = Intent(this, CreateTokenActivity::class.java)
            startActivity(intent)
        }

        getFundsButton.setOnClickListener() { v ->
            val voucherWalletAddress = sharedPref!!.getString(resources.getString(R.string.shared_pref_voucher_wallet_address), "0x0")

            FaucetManager.INSTANCE.getFounds(this, voucherWalletAddress)
        }

        enabledDebugMode.setOnClickListener() { v ->
            val debugMode = sharedPref!!.getBoolean(resources.getString(R.string.shared_pref_debug_mode), false)
            val editor = sharedPref.edit()
            editor.putBoolean(resources.getString(R.string.shared_pref_debug_mode), !debugMode)
            editor.commit()
            refreshDebugMode()
        }

        try {
            val barcodeEncoder = BarcodeEncoder()
            val address = voucher!!.tokenAddress
            val priceListIPNSAddress = voucher!!.ipnsAdddress
            val data = voucher!!.name + ";" + voucher!!.type + ";" + address + ";" + priceListIPNSAddress
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400)
            voucherQrcode.setImageBitmap(bitmap)
        } catch (e: Exception) {

        }

    }


    private fun refreshApiServerAddress() {
        apiServerAddressLabel.text = sharedPref!!.getString(resources.getString(R.string.shared_pref_vallet_api_server_address), resources.getString(R.string.default_vallet_api_server_address))

    }
    private fun refreshDebugMode() {
        val debugMode = sharedPref!!.getBoolean(resources.getString(R.string.shared_pref_debug_mode), false)
        if (debugMode) {
            enabledDebugMode.text = "Disable debug Mode"
        } else {
            enabledDebugMode.text = "Enable debug Mode"
        }
    }

    private fun refreshBalance() {
        val voucherWalletAddress = sharedPref!!.getString(resources.getString(R.string.shared_pref_voucher_wallet_address), "0x0")
        voucherWalletAddresLabel.text = voucherWalletAddress.toString()
        Log.i(TAG, "Wallet address: " + voucherWalletAddress)
        try {
            var walletBalance = Web3jManager.INSTANCE.getBalance(this, voucherWalletAddress)
            voucherWalletBalanceLabel.text = walletBalance.balance.toString()
        } catch (e: Exception) {
            // TODO inform user about fail balance sync
            voucherWalletBalanceLabel.text = "0e"
        }

    }

    private fun refreshIPNSAddress() {
        if (voucher?.ipnsAdddress != null)
            ipnsAddresLabel.text = voucher!!.ipnsAdddress
    }

    private fun refreshName() {
        if (voucher?.name != null)
            voucherNameLabel.text = voucher!!.name
    }

    private fun refreshNodeAddress() {
        serverIpValue.text = Web3jManager.INSTANCE.getNodeAddress(this)
    }

    private fun refreshIpfsAddress() {
        ipfsAddressValue.text = sharedPref!!.getString(resources.getString(R.string.shared_pref_ipfs_address), resources.getString(R.string.ipfs_server))

    }

    private fun refreshFactoryContractAddress() {
        contractAddressValue.text = Web3jManager.INSTANCE.getContractAddress(this)
    }

    private fun refreshTokenContract() {
        if (voucher?.tokenAddress != null) {
            voucherContractAddresLabel.text = voucher!!.tokenAddress
            Log.i(TAG, "Vouchers contract address: " + voucherContractAddresLabel.text)
        }
    }

    private fun refreshAll() {
        refreshBalance()
        refreshName()
        refreshNodeAddress()
        refreshFactoryContractAddress()
        refreshTokenContract()
        refreshIpfsAddress()
        refreshDebugMode()
        refreshIPNSAddress()
        refreshApiServerAddress()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onError(event: ErrorEvent) {
        Toast.makeText(this, "Error: " + event.message, Toast.LENGTH_LONG).show()
    };
}
