package io.lab10.vallet.utils

import android.view.Window.FEATURE_NO_TITLE
import android.os.Bundle
import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.MessageEvent
import io.lab10.vallet.events.ProductPaidEvent
import io.lab10.vallet.models.Product
import io.lab10.vallet.models.Tokens
import io.lab10.vallet.models.Wallet
import kotlinx.android.synthetic.client.pay_dialog.*
import org.greenrobot.eventbus.EventBus


class PayDialog(var activity: Activity, val product: Product) : Dialog(activity), android.view.View.OnClickListener {
    var d: Dialog? = null
    var yes: Button? = null
    var no: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(FEATURE_NO_TITLE)
        setContentView(R.layout.pay_dialog)
        yes = findViewById(R.id.pay_button) as Button
        no = findViewById(R.id.close_button) as ImageButton
        (yes as Button).setOnClickListener(this)
        (no as ImageButton).setOnClickListener(this)
        if (ValletApp.activeToken!!.tokenType.equals(Tokens.Type.EUR.type)) {
            price_label.text = Wallet.convertATS2EUR(product.price).toString()
        } else {
            voucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon)
            price_label.text = product.price.toString()
        }
        product_name.text = product.name

    }

    override fun onClick(v: View) {
        when (v.getId()) {
            // TODO chanage the action here
            R.id.pay_button -> {
                Web3jManager.INSTANCE.redeemToken(activity, product.price.toBigInteger(), ValletApp.activeToken!!.tokenAddress, product.name)
                EventBus.getDefault().post(MessageEvent("You paid for " + product.name))
                EventBus.getDefault().post(ProductPaidEvent(product.price))
            }
            R.id.close_button -> dismiss()
            else -> {
            }
        }
        dismiss()
    }
}