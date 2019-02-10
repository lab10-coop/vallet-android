package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.models.Token
import kotlinx.android.synthetic.main.activity_show_qr_code.*
import org.greenrobot.eventbus.EventBus

class ShowQrCodeActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    var voucher: Token? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr_code)

        sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        var voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        voucher = voucherBox.query().build().findFirst()

        if (voucher == null) {
            finish()
        }

        closeButton.setOnClickListener() { _ ->
            finish()
        }

        hint_text.text = "This is QR code for shop: ${voucher!!.name} \n\nShow this QR code to your user for them to add your shop to their app."

        try {
            val barcodeEncoder = BarcodeEncoder()
            val uri = "vallet://shop/" + voucher!!.tokenAddress
            val bitmap = barcodeEncoder.encodeBitmap(uri, BarcodeFormat.QR_CODE, 400, 400)
            voucherQrcode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            EventBus.getDefault().post(ErrorEvent(e.message.toString()))
        }
    }
}
