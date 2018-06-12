package io.lab10.vallet

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import io.lab10.vallet.connectivity.BTUtils
import io.lab10.vallet.events.RedeemVoucherEvent
import io.lab10.vallet.events.TransferVoucherEvent
import io.lab10.vallet.models.Vouchers

import kotlinx.android.synthetic.client.activity_client.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import com.google.zxing.BarcodeFormat
import android.view.View
import android.view.animation.Animation
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import com.google.zxing.integration.android.IntentIntegrator
import io.lab10.vallet.events.TokenNameEvent
import io.lab10.vallet.events.TokenTypeEvent
import io.lab10.vallet.models.Voucher
import io.lab10.vallet.models.Voucher_
import io.lab10.vallet.models.Wallet

class ClientActivity : AppCompatActivity() {

    var voucherWalletAddress : String = "";
    val TAG = ClientActivity::class.java.simpleName

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        viewManager = LinearLayoutManager(this)

        var myVouchers = Vouchers.getVouchers()
        viewAdapter = VoucherAdapter(myVouchers)
            scanTokenContract.visibility = View.VISIBLE
            scanTokenContract.setOnClickListener {
                val integrator = IntentIntegrator(this)
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan()
            }

        recyclerView = findViewById<RecyclerView>(R.id.voucherList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        observeVouchers()

        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        voucherBox.query().build().forEach { voucher ->
            if (voucher.name.equals("Fetching ...")) {
                Web3jManager.INSTANCE.getTokenName(this, voucher.tokenAddress)
            }

            // TODO this should not be check like that as both states are very likely
            if (voucher.balance == 0) {
                Web3jManager.INSTANCE.getVoucherBalance(this, voucher.tokenAddress)
            }
            if (voucher.type == 0) {
                Web3jManager.INSTANCE.getTokenType(this, voucher.tokenAddress)
            }
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

        Log.i(TAG, "Wallet address: " + voucherWalletAddress)

        generateWalletBarcode(voucherWalletAddress)
    }

    private fun generateWalletBarcode(address: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(address, BarcodeFormat.QR_CODE, 400, 400)
            voucherQrcode.setImageBitmap(bitmap)
        } catch (e: Exception) {

        }

    }

    private fun startLoaderAnimation() {
        val anim = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 700
        loader.startAnimation(anim)
    }

    // TODO trigger stop when scanning will be over
    private fun stopLoaderAnimation() {
        loader.animation = null
        loader.visibility = View.GONE
    }

    fun startBroadcastingAddress() {
        // TODO make sure that BT is on if not turn it on
        Toast.makeText(this,"Broadcasting address", Toast.LENGTH_LONG).show()
        startLoaderAnimation()
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        val address = voucherWalletAddress
        // Address is always with 0x which we don't need to transfer
        val part1 = address.substring(2, BTUtils.SERVICE_NAME_SIZE+2)
        val part2 = address.substring(BTUtils.SERVICE_NAME_SIZE+2)
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

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        startBroadcastingAddress()
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, event.address).build().findFirst()
        if (voucher != null) {
            voucher.balance = voucher.balance + event.value.toInt()
            voucherBox.put(voucher)
        }
    }
    @Subscribe
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, event.address).build().findFirst()
        if (voucher != null) {
            voucher.balance = voucher.balance - event.value.toInt()
            voucherBox.put(voucher)
        }
    }

    @Subscribe
    fun onTokenNameEvent(event: TokenNameEvent) {
        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, event.address).build().findFirst()
        if (voucher != null) {
            voucher.name = event.name
            voucherBox.put(voucher)
        }
    }

    @Subscribe
    fun onTokenTypeEvent(event: TokenTypeEvent) {
        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, event.address).build().findFirst()
        if (voucher != null) {
            if (event.name.equals("EUR")) {
                voucher.type = 0
            } else {
                voucher.type = 1
            }
            voucherBox.put(voucher)
        }
    }

    private fun observeVouchers() {
        val voucherBox = ValletApp.getBoxStore()
        voucherBox.subscribe(Voucher::class.java).observer {
            viewAdapter.notifyDataSetChanged()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val data = result.contents
            val tokenAddress = data.split(";")[0]
            val ipfsAddress = data.split(";")[1]
            if (Wallet.isValidAddress(tokenAddress)) {
                storeTokenAddress(tokenAddress, ipfsAddress)
           }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun storeTokenAddress(address: String, ipfsAddress: String) {
        Web3jManager.INSTANCE.getTokenName(this, address)
        Web3jManager.INSTANCE.getTokenType(this, address)
        Web3jManager.INSTANCE.getVoucherBalance(this, address)
        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        val voucher = Voucher(0,"Fetching ...", address, 0, 0, ipfsAddress)
        voucherBox.put(voucher)
    }

}
