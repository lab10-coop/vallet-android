package io.lab10.vallet

import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import io.lab10.vallet.events.*
import io.lab10.vallet.fragments.ProductFragment
import io.lab10.vallet.models.*
import io.lab10.vallet.models.Token
import kotlinx.android.synthetic.client.activity_client_home_activty.*
import kotlinx.android.synthetic.client.app_bar_client_home_activty.*
import kotlinx.android.synthetic.client.voucher_item.view.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ClientHomeActivty : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ProductFragment.OnListFragmentInteractionListener {
    var voucherWalletAddress : String = "";



    override fun onProductLongClickListner(holder: ProductRecyclerViewAdapter.ProductViewHolder) {
        // Do nothing client should not be able to remove item
    }

    override fun onProductCancelRemoveListner(holder: ProductRecyclerViewAdapter.ProductViewHolder) {
        // Do nothing client should not be able to remove item
    }

    override fun onProductClickListner(item: Product) {
        if (item.price > ValletApp.activeToken!!.balance) {
            Toast.makeText(this, "Sorry not enough funds", Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(this)
                    .setTitle("Pay")
                    .setMessage("Are you confirm to pay for that product?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, whichButton ->
                        Web3jManager.INSTANCE.redeemToken(this, item.price.toBigInteger(), ValletApp.activeToken!!.tokenAddress)
                        Toast.makeText(this, "Paid", Toast.LENGTH_SHORT).show()
                    })
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home_activty)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        voucherWalletAddress = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_wallet_address), "")

        if (ValletApp.activeToken!!.tokenType != 0) {
            toolbarVoucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon)
        } else {
            toolbarVoucherTypeIcon.setBackgroundResource(R.drawable.euro_icon_black)
        }
        if (ValletApp.activeToken!!.remoteReadStoragePresent()) {
            ValletApp.activeToken!!.storage().fetch()
        } else {
            EventBus.getDefault().post(ErrorEvent("Token does not have remote storage defined. Contact Admin"))
        }

        setPriceListHeader()
        // TODO move that to settings
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_SHORT).show();
        }else if(!nfcAdapter!!.isEnabled()){
            Toast.makeText(this,
                    "NFC NOT Enabled!",
                    Toast.LENGTH_SHORT).show();
        }


        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        val burger = toggle.drawerArrowDrawable
        burger.color = resources.getColor(R.color.black)
        toggle.syncState()

        Tokens.refresh()

        reloadNavigation()
        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun setPriceListHeader() {
        toolbarBalance.text = ValletApp.activeToken!!.balance.toString()
    }

    private fun reloadNavigation() {
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        val menu = navigationView.menu
        menu.clear()
        Tokens.getVouchers().forEach { token ->
            val item = menu.add(token.name)
            item.isCheckable = true
            item.isChecked = token.active
            item.setActionView(R.layout.token_balance )
            val view = item.actionView
            view.voucherBalance.text = token.balance.toString()
            item.setOnMenuItemClickListener { _ ->
                ValletApp.activeToken = token
                reloadProductList()
                true
            }
        }
        menu.addSubMenu("Settings")
        menu.add(resources.getString(R.string.tap_to_search_for_admin))
        val qrMenu = menu.add(resources.getString(R.string.your_qrcode))
        qrMenu.setOnMenuItemClickListener { _ ->
            val intent = Intent(this, ShowQrCodeActivity::class.java)
            startActivity(intent)
            true
        }
        navigationView.invalidate()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStart() {
        super.onStart()
        refreshBalance()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    override fun onResume() {
        super.onResume()

        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, this.javaClass)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        if (nfcAdapter != null) {
            if (nfcAdapter!!.isEnabled())
                nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null);
        }

    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        resolveIntent(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductchangedEvent(event: ProductChangedEvent) {
        refreshProductListView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductRefreshevent(event: ProductRefreshEvent) {
        refreshProductListView()
        reloadProductList()
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
        setPriceListHeader()
        reloadNavigation()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRedeemTokenEvent(event: TokenRedeemEvent) {
        refreshBalance()
    }

    private fun reloadProductList() {

        if (ValletApp.activeToken != null) {
            // Load first from local storage
            Products.refresh(ValletApp.activeToken!!)
            var productFragment = supportFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
            productFragment.notifyAboutchange()
            productFragment.swiperefresh.isRefreshing = false
            if (ValletApp.activeToken!!.remoteReadStoragePresent())
                ValletApp.activeToken!!.storage().fetch()
        }
    }


    private fun refreshProductListView() {
        var productFragment = supportFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        Products.refresh(ValletApp.activeToken!!)
        productFragment.notifyAboutchange()
        productFragment.swiperefresh.isRefreshing = false
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

    private fun refreshBalance() {
        // Trigger balance check for each token
        Tokens.getVouchers().forEach { e ->
            Web3jManager.INSTANCE.getClientBalance(this, e.tokenAddress,  voucherWalletAddress)
        }
    }

}
