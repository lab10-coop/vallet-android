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
import java.math.BigInteger
import com.google.zxing.BarcodeFormat
import android.graphics.Bitmap
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation






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
        var myDataset: MutableList<Vouchers.Voucher> = ArrayList()
        // TODO fetch from local DB
        val voucher = Vouchers.Voucher("Lab10", "Lab10", "0x123131232", 156)
        val voucher2 = Vouchers.Voucher("Lab10", "Lab10", "0x123131232", 156)
        myDataset.add(voucher)
        myDataset.add(voucher2)
        viewAdapter = VoucherAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.voucherList).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
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
        Web3jManager.INSTANCE.getVoucherBalance(this, voucherWalletAddress)

        generateWalletBarcode(voucherWalletAddress)
        // TODO update proper voucher on the list
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
/*        val voucherCount = activeVouchersCount.text as String
        var currentValue = BigInteger.ZERO
        if (voucherCount.length > 0) {
            currentValue = voucherCount.toBigInteger()
        }
        currentValue += event.value
        activeVouchersCount.text = currentValue.toString()*/
    }
    @Subscribe
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
/*        val voucherCount = activeVouchersCount.text as String
        var currentValue = BigInteger.ZERO
        if (voucherCount.length > 0) {
            currentValue = voucherCount.toBigInteger()
        }
        currentValue -= event.value
        activeVouchersCount.text = currentValue.toString()*/
    }

}
