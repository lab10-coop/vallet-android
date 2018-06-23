package io.lab10.vallet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.nfc.NfcAdapter
import android.widget.Toast
import android.os.Parcelable
import android.provider.Settings.ACTION_WIRELESS_SETTINGS
import android.content.Intent
import android.provider.Settings
import android.app.PendingIntent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.support.v4.app.FragmentActivity
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.ProductsListEvent
import io.lab10.vallet.fragments.ProductFragment
import io.lab10.vallet.models.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigInteger
import kotlin.experimental.and


class ProductListActivity : FragmentActivity(), ProductFragment.OnListFragmentInteractionListener {
    override fun onListFragmentInteraction(item: Product) {
        // TODO add confirmation
        Web3jManager.INSTANCE.redeemToken(this, item.price.toBigInteger(), tokenAddress!!)
    }

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var tokenAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        val extras = intent.extras
        if (extras != null) {
            tokenAddress = extras.getString("EXTRA_TOKEN_ADDRESS")
            val vouchersBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
            var voucher = vouchersBox.query().equal(Voucher_.tokenAddress, tokenAddress).build().findFirst()
            if (voucher != null && voucher.ipnsAdddress.length > 0) {
                fetchProducts(voucher.ipnsAdddress)
            } else {
                EventBus.getDefault().post(ErrorEvent("Missing ipns address"))
                finish()
            }

        } else {
            EventBus.getDefault().post(ErrorEvent("Missing token address"))
            finish()
        }


        pendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, this.javaClass)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
            finish();
        }else if(!nfcAdapter!!.isEnabled()){
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();
            finish();
        }


    }


    private fun fetchProducts(priceListIPNSAddress: String) {
        // TODO we should use IntentService for all network activities
        // to avoid potential memory leaks. In this case we also should check
        // response and handle case where response will fail and inform user.
        var productFragment = supportFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.swiperefresh.isRefreshing = true;
        Thread(Runnable {
            try {
                IPFSManager.INSTANCE.fetchProductList(this, priceListIPNSAddress)
                EventBus.getDefault().post(ProductsListEvent())
            } catch (e: Exception) {
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))
                productFragment.swiperefresh.isRefreshing = false;
            }
        }).start()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductsListEvent(event: ProductsListEvent) {
        var productFragment = supportFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.notifyAboutchange()
        productFragment.swiperefresh.isRefreshing = false
    };

    override fun onResume() {
        super.onResume()

        val intent = intent
        val action = intent.action

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled())
                showWirelessSettings();

            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null);
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        resolveIntent(intent)
    }

    private fun showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }

    private fun resolveIntent(intent: Intent) {
        val action = intent.action

        if (NfcAdapter.ACTION_TAG_DISCOVERED == action
                || NfcAdapter.ACTION_TECH_DISCOVERED == action
                || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            val tag = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as Tag
            var result = ""
            for (b in tag.id) {
                val st = String.format("%02X", b)
                result += st
            }
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }
    }

}
