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
import io.lab10.vallet.events.*
import io.lab10.vallet.models.Voucher
import io.lab10.vallet.models.Voucher_
import io.lab10.vallet.models.Wallet
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import org.greenrobot.eventbus.ThreadMode

class ClientActivity : AppCompatActivity() {

    var voucherWalletAddress : String = "";
    val TAG = ClientActivity::class.java.simpleName

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var subscription: DataSubscription
    val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        viewManager = LinearLayoutManager(this)

        Vouchers.refresh()
        // Trigger balance check for each token
        Vouchers.getVouchers().forEach { e ->
            Web3jManager.INSTANCE.getVoucherBalanceFrom(this, e.tokenAddress, e.lastBlockNumber)
        }
        viewAdapter = VoucherAdapter(Vouchers.getVouchers())
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

        generateWalletBarcode(voucherWalletAddress + ";" + getPhoneName())
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

    override fun onDestroy() {
        super.onDestroy()
        subscription.cancel()
    }

    @Subscribe
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, event.address).build().findFirst()
        if (voucher != null && voucher.lastBlockNumber < event.blockNumber.toLong()) {
            voucher.balance = voucher.balance + event.value.toInt()
            voucher.lastBlockNumber = event.blockNumber.toLong()
            voucherBox.put(voucher)
        }
        Vouchers.refresh()
        viewAdapter.notifyDataSetChanged()
    }
    @Subscribe
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, event.address).build().findFirst()
        if (voucher != null && voucher.lastBlockNumber < event.blockNumber.toLong()) {
            voucher.balance = voucher.balance - event.value.toInt()
            voucher.lastBlockNumber = event.blockNumber.toLong()
            voucherBox.put(voucher)
        }
        Vouchers.refresh()
        viewAdapter.notifyDataSetChanged()
    }

    @Subscribe
    fun onTokenNameEvent(event: TokenNameEvent) {
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, event.address).build().findFirst()
        if (voucher != null) {
            voucher.name = event.name
            voucherBox.put(voucher)
        }
        Vouchers.refresh()
        viewAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onError(event: ErrorEvent) {
        Toast.makeText(this, "Error: " + event.message, Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDebug(event: DebugEvent) {
        Toast.makeText(this, "Debug: " + event.message, Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessage(event: MessageEvent) {
        Toast.makeText(this, "Message: " + event.message, Toast.LENGTH_LONG).show()
    }

    @Subscribe
    fun onTokenTypeEvent(event: TokenTypeEvent) {
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
        voucherBox.query().orderDesc(Voucher_.name).build().subscribe().on(AndroidScheduler.mainThread()).observer { vouchers ->
            Vouchers.refresh()
            viewAdapter.notifyDataSetChanged()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val data = result.contents
            val splitData = data.split(";")
            val voucherName = splitData[0]
            val voucherType = splitData[1]
            val tokenAddress = splitData[2]
            val ipnsAddress = splitData[3]
            if (Wallet.isValidAddress(tokenAddress)) {
                storeTokenAddress(voucherName, voucherType.toInt(), tokenAddress, ipnsAddress)
           }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun storeTokenAddress(voucherName: String, voucherType: Int, address: String, ipnsAddress: String) {
        Web3jManager.INSTANCE.getVoucherBalance(this, address)
        var voucher = voucherBox.query().equal(Voucher_.tokenAddress, address).build().findFirst()
        if (voucher == null) {
            voucher = Voucher(0, voucherName, address, 0, voucherType, ipnsAddress, false, 0.toLong())
            voucherBox.put(voucher)
        } else {
            voucher.ipnsAdddress = ipnsAddress
            voucher.name = voucherName
            voucher.type = voucherType
            voucherBox.put(voucher)
        }
        Vouchers.refresh()
        viewAdapter.notifyDataSetChanged()
    }

    fun getPhoneName(): String {
        val myDevice = BluetoothAdapter.getDefaultAdapter()
        return myDevice.name
    }

}
