package io.lab10.vallet

import android.app.Activity
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
import io.lab10.vallet.models.Token
import io.lab10.vallet.models.Token_
import io.lab10.vallet.models.Tokens
import io.lab10.vallet.models.Wallet
import io.objectbox.android.AndroidScheduler
import org.greenrobot.eventbus.ThreadMode

class ClientActivity : AppCompatActivity() {

    var voucherWalletAddress : String = "";
    val TAG = ClientActivity::class.java.simpleName

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var scanningInProgress: Boolean = false
    val voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
    val REQUEST_BT_ENABLE = 100
    var debugMode: Boolean = false
    var debugModeCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        viewManager = LinearLayoutManager(this)

        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        voucherWalletAddress = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_wallet_address), "")


        Tokens.refresh()

        logo.setOnClickListener() {
            if (debugModeCount < 7)
                debugModeCount +1
            else
                debugMode = true
                Toast.makeText(this, "Debug mode on", Toast.LENGTH_SHORT).show()
        }

        viewAdapter = VoucherAdapter(Tokens.getVouchers())
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

        loader.setOnClickListener() {
            startBroadcastingAddress()
        }

        scanningLabel.setOnClickListener() {
            startBroadcastingAddress()
        }

        observeVouchers()

        if (voucherWalletAddress.equals("")) {
            val editor = sharedPref.edit()
            val walletFile = Web3jManager.INSTANCE.createWallet(this, "123")
            voucherWalletAddress = Web3jManager.INSTANCE.getWalletAddress(walletFile)
            editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_address), voucherWalletAddress)
            editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_file), walletFile)
            editor.commit()
        }

        if (debugMode)
            Log.i(TAG, "Wallet address: " + voucherWalletAddress)

        generateWalletBarcode(voucherWalletAddress + ";" + getPhoneName())
    }

    private fun refreshBalance() {
        // Trigger balance check for each token
        Tokens.getVouchers().forEach { e ->
            Web3jManager.INSTANCE.getClientBalance(this, e.tokenAddress,  voucherWalletAddress)
        }
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
        if (!scanningInProgress) {
            // TODO make sure that BT is on if not turn it on

            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
               Toast.makeText(this, "This devices does not support BT please use QR code instead", Toast.LENGTH_SHORT).show()
            } else {
                val address = voucherWalletAddress
                // Address is always with 0x which we don't need to transfer
                val part1 = address.substring(2, BTUtils.SERVICE_NAME_SIZE + 2)
                val part2 = address.substring(BTUtils.SERVICE_NAME_SIZE + 2)
                val uuid1 = BTUtils.encodeAddress(part1)
                val uuid2 = BTUtils.encodeAddress(part2)
                if (!mBluetoothAdapter.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    this.startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE)
                } else {
                    scanningInProgress = true
                    if (debugMode)
                        Toast.makeText(this, "Broadcasting address", Toast.LENGTH_LONG).show()
                    startLoaderAnimation()
                    loader.setBackgroundResource(R.drawable.loader)
                    scanningLabel.text = resources.getString(R.string.broadcasting_wallet_address)
                    mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid1)
                    mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid2)
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        refreshBalance()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNewBalanceAvailabelEvent(event: TokenBalanceEvent) {
        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        val token = tokenBox.query().equal(Token_.tokenAddress, event.address).build().findFirst()
        if (token != null) {
            token.balance = event.balance.toInt()
            tokenBox.put(token)
        }
        Tokens.refresh()
        viewAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        Tokens.refresh()
        viewAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        Tokens.refresh()
        viewAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTokenNameEvent(event: TokenNameEvent) {
        var voucher = voucherBox.query().equal(Token_.tokenAddress, event.address).build().findFirst()
        if (voucher != null) {
            voucher.name = event.name
            voucherBox.put(voucher)
        }
        Tokens.refresh()
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
        var voucher = voucherBox.query().equal(Token_.tokenAddress, event.address).build().findFirst()
        if (voucher != null) {
            if (event.name.equals("EUR")) {
                voucher.tokenType = 0
            } else {
                voucher.tokenType = 1
            }
            voucherBox.put(voucher)
        }
    }

    private fun observeVouchers() {
        voucherBox.query().orderDesc(Token_.name).build().subscribe().on(AndroidScheduler.mainThread()).observer { vouchers ->
            Tokens.refresh()
            viewAdapter.notifyDataSetChanged()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Return from QR code scanning
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null && result.contents != null) {
                val data = result.contents
                val splitData = data.split(";")
                if (splitData.size == 4) {
                    val voucherName = splitData[0]
                    val voucherType = splitData[1]
                    val tokenAddress = splitData[2]
                    val ipnsAddress = splitData[3]
                    if (Wallet.isValidAddress(tokenAddress)) {
                        storeTokenAddress(voucherName, voucherType.toInt(), tokenAddress, ipnsAddress)
                    }
                } else {
                    Toast.makeText(this, "Invalid qr code, try different", Toast.LENGTH_SHORT).show()
                }

            }
        }

        if (requestCode == REQUEST_BT_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                startBroadcastingAddress()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun storeTokenAddress(voucherName: String, voucherType: Int, address: String, ipnsAddress: String) {
        Web3jManager.INSTANCE.getClientBalance(this, address, voucherWalletAddress)
        var voucher = voucherBox.query().equal(Token_.tokenAddress, address).build().findFirst()
        if (voucher == null) {
            voucher = Token(0, voucherName, address, 0, voucherType, ipnsAddress, false, 0.toLong())
            voucherBox.put(voucher)
        } else {
            voucher.ipnsAdddress = ipnsAddress
            voucher.name = voucherName
            voucher.tokenType = voucherType
            voucherBox.put(voucher)
        }
        Tokens.refresh()
        viewAdapter.notifyDataSetChanged()
    }

    fun getPhoneName(): String {
        val myDevice = BluetoothAdapter.getDefaultAdapter()
        return myDevice.name
    }

}
