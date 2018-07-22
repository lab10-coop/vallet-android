package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.models.Token
import kotlinx.android.synthetic.admin.activity_show_qr_code.*

class ShowQrCodeActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    var voucher: Token? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_qr_code)

        sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        var voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        voucher = voucherBox.query().build().findFirst()

        closeButton.setOnClickListener() { _ ->
            finish()
        }

        try {
            val barcodeEncoder = BarcodeEncoder()
            val address = voucher!!.tokenAddress
            val priceListIPNSAddress = voucher!!.ipnsAdddress
            val data = voucher!!.name + ";" + voucher!!.tokenType + ";" + address + ";" + priceListIPNSAddress
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400)
            voucherQrcode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // TODO handle exception
        }
    }
}
