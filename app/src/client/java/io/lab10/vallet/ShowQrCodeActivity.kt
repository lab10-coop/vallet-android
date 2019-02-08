package io.lab10.vallet

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_show_qr_code.*
import java.net.URLEncoder

class ShowQrCodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr_code)

        val voucherWalletAddress = ValletApp.wallet!!.address

        activity_title.text = resources.getString(R.string.activity_show_qr_code_title)
        hint_text.text = resources.getString(R.string.activity_show_qr_code_hint)


        closeButton.setOnClickListener() { _ ->
            finish()
        }
        // TODO: add user name from first step of the registration
        var uri = "valletadmin://user/" + voucherWalletAddress + "?user_name=" + URLEncoder.encode(ValletApp.wallet!!.name, "UTF-8") + ""

        generateWalletBarcode(uri)
    }


    private fun generateWalletBarcode(address: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(address, BarcodeFormat.QR_CODE, 400, 400)
            voucherQrcode.setImageBitmap(bitmap)
        } catch (e: Exception) {

        }
    }

}