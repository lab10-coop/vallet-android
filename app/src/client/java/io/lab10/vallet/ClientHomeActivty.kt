package io.lab10.vallet

import android.app.Activity
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import io.lab10.vallet.connectivity.BTUtils
import io.lab10.vallet.events.*
import io.lab10.vallet.fragments.NoStoreFragment
import io.lab10.vallet.fragments.ProductListFragment
import io.lab10.vallet.models.*
import io.lab10.vallet.models.Token
import kotlinx.android.synthetic.client.activity_client_home_activty.*
import kotlinx.android.synthetic.client.app_bar_client_home_activty.*
import kotlinx.android.synthetic.client.voucher_item.view.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ClientHomeActivty : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ProductListFragment.OnListFragmentInteractionListener {

    private var voucherWalletAddress : String = "";
    // Used onActivityResult to refresh fragment since not always the state of activity is restored on time
    private var reloadOnResume = false

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


        if (voucherWalletAddress.equals("")) {
            val editor = sharedPref.edit()
            val walletFile = Web3jManager.INSTANCE.createWallet(this, "123")
            voucherWalletAddress = Web3jManager.INSTANCE.getWalletAddress(walletFile)
            editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_address), voucherWalletAddress)
            editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_file), walletFile)
            editor.commit()
        }

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

    private fun showPlaceHolderFragment() {
        val noStorePlaceHolder = NoStoreFragment.newInstance()
        toolbarVoucherTypeIcon.visibility = View.GONE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.price_list_fragment_container, noStorePlaceHolder)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun showPriceListFragment() {
        if (ValletApp.activeToken!!.tokenType != 0) {
            toolbarVoucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon)
        } else {
            toolbarVoucherTypeIcon.setBackgroundResource(R.drawable.euro_icon_black)
        }
        setPriceListHeader()
        if (ValletApp.activeToken!!.remoteReadStoragePresent()) {
            ValletApp.activeToken!!.storage().fetch()
        } else {
            EventBus.getDefault().post(ErrorEvent("Token does not have remote storage defined. Contact Admin"))
        }
        val productListFragment = ProductListFragment.newInstance()
        toolbarVoucherTypeIcon.visibility = View.VISIBLE
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.price_list_fragment_container, productListFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setPriceListHeader() {
        if (ValletApp.activeToken != null) {
            toolbarBalance.text = ValletApp.activeToken!!.balance.toString()
        }
    }

    private fun reloadNavigation() {
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        val menu = navigationView.menu
        menu.clear()
        if(ValletApp.activeToken == null){
            val i = menu.add(resources.getString(R.string.no_token_available))
            val txt = resources.getString(R.string.no_token_available)
            val s = SpannableString(txt)
            s.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.gray)), 0, txt.length, 0);
            i.setTitle(s)
        }
        Tokens.getVouchers().forEach { token ->
            val item = menu.add(token.name)
            item.isCheckable = true
            item.isChecked = token.active
            item.setActionView(R.layout.token_balance )
            val view = item.actionView
            view.voucherBalance.text = Wallet.convertATS2EUR(token.balance).toString()
            item.setOnMenuItemClickListener { _ ->
                ValletApp.activeToken = token
                reloadProductList()
                drawer_layout.closeDrawer(GravityCompat.START)
                true
            }
        }
        menu.addSubMenu("Settings")
        val scanMenu = menu.add(resources.getString(R.string.tap_to_search_for_admin))
        scanMenu.setOnMenuItemClickListener { _ ->
            val integrator = IntentIntegrator(this)
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan()
            true
        }
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

    override fun onPostResume() {
        super.onPostResume()
        if(ValletApp.activeToken != null) {
            showPriceListFragment()
        } else {
            showPlaceHolderFragment()
        }
        if (reloadOnResume) {
            reloadOnResume = false
            reloadProductList()
        }
    }
    private val REQUEST_BT_ENABLE = 100

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
            token.balance = event.balance
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
            var productFragment = supportFragmentManager.primaryNavigationFragment
            if (productFragment is ProductListFragment) {
                productFragment.notifyAboutchange()
                productFragment.swiperefresh.isRefreshing = false
                if (ValletApp.activeToken!!.remoteReadStoragePresent())
                    ValletApp.activeToken!!.storage().fetch()
            }
        }
    }


    private fun refreshProductListView() {
        var productFragment = supportFragmentManager.findFragmentById(R.id.price_list_fragment_container)
        if (productFragment is ProductListFragment) {
            Products.refresh(ValletApp.activeToken!!)
            productFragment.notifyAboutchange()
            productFragment.swiperefresh.isRefreshing = false
        }
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

    private fun storeTokenAddress(voucherName: String, voucherType: Int, address: String, ipnsAddress: String) {
        Web3jManager.INSTANCE.getClientBalance(this, address, voucherWalletAddress)
        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        var token = tokenBox.query().equal(Token_.tokenAddress, address).build().findFirst()
        if (token == null) {
            token = Token(0, voucherName, address, 0, voucherType, ipnsAddress, false, 0.toLong())
            tokenBox.put(token)
        } else {
            token.ipnsAdddress = ipnsAddress
            token.name = voucherName
            token.tokenType = voucherType
            tokenBox.put(token)
        }
        ValletApp.activeToken = token
        Tokens.refresh()
        reloadNavigation()
        reloadOnResume = true
    }

    private var scanningInProgress = false

    private fun startBroadcastingAddress() {
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
                    //if (debugMode)
                    //    Toast.makeText(this, "Broadcasting address", Toast.LENGTH_LONG).show()
                    //startLoaderAnimation()
                    //loader.setBackgroundResource(R.drawable.loader)
                    //scanningLabel.text = resources.getString(R.string.broadcasting_wallet_address)
                    mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid1)
                    mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), uuid2)
                }
            }
        }
    }

}
