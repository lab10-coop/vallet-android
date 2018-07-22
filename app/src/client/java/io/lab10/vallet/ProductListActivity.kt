package io.lab10.vallet

import android.os.Bundle
import android.nfc.NfcAdapter
import android.widget.Toast
import android.os.Parcelable
import android.content.Intent
import android.provider.Settings
import android.app.PendingIntent
import android.nfc.Tag
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.ProductsListEvent
import io.lab10.vallet.fragments.ProductFragment
import io.lab10.vallet.models.Token
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import io.lab10.vallet.events.ProductChangedEvent
import io.lab10.vallet.events.ProductRefreshEvent
import io.lab10.vallet.models.Product
import io.lab10.vallet.models.Products
import io.lab10.vallet.models.Token_
import kotlinx.android.synthetic.client.activity_product_list.*

class ProductListActivity : AppCompatActivity(), ProductFragment.OnListFragmentInteractionListener {
    override fun onProductLongClickListner(holder: ProductRecyclerViewAdapter.ProductViewHolder) {
        // Do nothing client should not be able to remove item
    }

    override fun onProductCancelRemoveListner(holder: ProductRecyclerViewAdapter.ProductViewHolder) {
        // Do nothing client should not be able to remove item
    }

    override fun onProductClickListner(item: Product) {
        if (item.price > token!!.balance) {
            Toast.makeText(this, "Sorry not enough funds", Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(this)
                    .setTitle("Pay")
                    .setMessage("Are you confirm to pay for that product?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, whichButton ->
                        Web3jManager.INSTANCE.redeemToken(this, item.price.toBigInteger(), tokenAddress!!)
                        Toast.makeText(this, "Paid", Toast.LENGTH_SHORT).show()
                    })
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var tokenAddress: String? = null
    private var tokenBalance: String? = null
    private var tokenType: Int? = null
    private var token: io.lab10.vallet.models.Token? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener() {
            finish()
        }
        val extras = intent.extras
        if (extras != null) {
            tokenAddress = extras.getString("EXTRA_TOKEN_ADDRESS")
            tokenBalance = extras.getString("EXTRA_TOKEN_BALANCE")
            tokenType = extras.getInt("EXTRA_TOKEN_TYPE")
            if (tokenType != 0) {
                toolbarVoucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon)
            } else {
                toolbarVoucherTypeIcon.setBackgroundResource(R.drawable.euro_icon_black)
            }
            toolbarBalance.text = tokenBalance
            val vouchersBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            token = vouchersBox.query().equal(Token_.tokenAddress, tokenAddress).build().findFirst()
            if (token != null && token!!.remoteReadStoragePresent()) {
                token!!.storage().fetch()
            } else {
                EventBus.getDefault().post(ErrorEvent("Token does not have remote storage defined. Contact Admin"))
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
        }else if(!nfcAdapter!!.isEnabled()){
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_LONG).show();
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductchangedEvent(event: ProductChangedEvent) {
        refreshProductList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductRefreshevent(event: ProductRefreshEvent) {
        refreshProductList()
        reloadProductList()
    }

    private fun reloadProductList() {

        if (token != null) {
            // Load first from local storage
            Products.refresh(token!!)
            var productFragment = supportFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
            productFragment.notifyAboutchange()
            productFragment.swiperefresh.isRefreshing = false
            if (token!!.remoteReadStoragePresent())
                token!!.storage().fetch()
        }
    }

    override fun onResume() {
        super.onResume()

        val intent = intent
        val action = intent.action

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            if (nfcAdapter!!.isEnabled())
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

    private fun refreshProductList() {
        var productFragment = supportFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        Products.refresh(token!!)
        productFragment.notifyAboutchange()
        productFragment.swiperefresh.isRefreshing = false
    }

}
