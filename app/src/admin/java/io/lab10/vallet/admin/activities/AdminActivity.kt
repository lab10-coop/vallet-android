package io.lab10.vallet.admin.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import io.lab10.vallet.R
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.zxing.integration.android.IntentIntegrator
import io.ValletUriParser
import io.lab10.vallet.ValletApp
import io.lab10.vallet.activites.BackupActivity
import io.lab10.vallet.admin.fragments.*
import io.lab10.vallet.models.User
import io.lab10.vallet.events.*
import io.lab10.vallet.fragments.ProductListFragment
import io.lab10.vallet.models.*
import kotlinx.android.synthetic.admin.activity_admin.*
import kotlinx.android.synthetic.admin.fragment_home_activity.*
import kotlinx.android.synthetic.main.progressbar_overlay.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigInteger
import io.lab10.vallet.admin.adapters.MainPagerAdapter
import io.lab10.vallet.connectivity.BTUtils
import java.lang.Exception
import io.lab10.vallet.utils.Formatter
import io.lab10.vallet.utils.NetworkUtils
import it.lamba.random.nextAlphanumericString
import java.io.File
import kotlin.random.Random

class AdminActivity : AppCompatActivity(), HomeActivityFragment.OnFragmentInteractionListener,
        IssueDialogFragment.OnFragmentInteractionListener,
        PriceListFragment.OnFragmentInteractionListener,
        ProductListFragment.OnListFragmentInteractionListener {

    override fun onProductClickListner(item: Product) {
        val intent = Intent(this, AddProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", item.id)
        startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val TAG = AdminActivity::class.java.simpleName
    var voucher: Token? = null
    private val REQUEST_BT_SCAN = 100
    private var hideDeleteProducts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Initialization of ObjectBox should happen always when the app starts
        // But for some reason during the lifecycle of the app happens that the onCreate()
        // is not triggered and the box is null. To avoid that issue we are triggering initialization
        // manually while the screens pops up.
        ValletApp.initBox(this)

        setDefaultConfiguration()

        if (ValletApp.wallet == null) {
            createWallet()
        }

        // When activity is triggered from on boarding screen we create token with given name
        val tokenName = intent.getStringExtra("TOKEN_NAME")
        if (tokenName != null) {
            Thread {
                createToken(tokenName)
            }.start()
        }

        // Fetch created token and set it as a active one if non is set yet. This should be triggered
        // only once when app is run first time
        if (ValletApp.activeToken == null) {
            Thread {
                Web3jManager.INSTANCE.getTokenContractAddress(this)
            }.start()
        }

        val myPagerAdapter = MainPagerAdapter(supportFragmentManager)
        myPagerAdapter.addFragment(HomeActivityFragment(), resources.getString(R.string.tab_activity))
        myPagerAdapter.addFragment(PriceListFragment(), resources.getString(R.string.tab_price_list))
        main_pager.adapter = myPagerAdapter
        main_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        setSendMoneyButton()
                        deleteProductVisible(false)
                    }
                    1 -> {
                        setFabAddProduct()
                        deleteProductVisible(true)
                    }
                }
            }
        })
        setSendMoneyButton()
        tab_layout.setupWithViewPager(main_pager)
        prepareHeader()
        EventBus.getDefault().register(this)

    }

    private fun setDefaultConfiguration() {
        val configBox = ValletApp.getBoxStore().boxFor(Configuration::class.java)
        // TODO do not override if will be already set
        configBox.put(Configuration(0, "ipfsAddress", resources.getString(R.string.ipfs_server)))
    }

    private fun deleteProductVisible(visible: Boolean) {
        hideDeleteProducts = visible
        invalidateOptionsMenu()
    }

    private fun setFabAddProduct() {
        fab_button.visibility = View.VISIBLE
        sendMoneyButton.visibility = View.GONE
        fab_button.setImageResource(R.drawable.add_product_button)
        fab_button.setOnClickListener() { v ->
            val intent = Intent(this@AdminActivity, AddProductActivity::class.java)
            startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
        }
    }

    private fun setSendMoneyButton() {
        // We do not want to display add product fab button same time when send money so we hid it
        fab_button.visibility = View.GONE
        sendMoneyButton.visibility = View.VISIBLE
        sendMoneyButton.setOnClickListener() { v ->
            val integrator = IntentIntegrator(this@AdminActivity)
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNoInternetEvent(event: NoInternetEvent) {
        Toast.makeText(this, "No internet connection please connect to the internet to continue", Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onError(event: ErrorEvent) {
        Crashlytics.log(event.message)
        Toast.makeText(this, "Error: " + event.message, Toast.LENGTH_LONG).show()
        if (progress_overlay.visibility == View.VISIBLE) {
            progress_overlay.visibility = View.GONE
            finish()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDebug(event: DebugEvent) {
        Toast.makeText(this, "Debug: " + event.message, Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessage(event: MessageEvent) {
        Toast.makeText(this, "Info: " + event.message, Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductListPublishedEvent(event: ProductListPublishedEvent) {
        val voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        var voucher = voucherBox.query().equal(Token_.id, event.voucherId).build().findFirst()
        if (voucher != null) {
            // TODO if we are using ValletApi we store here uuid instead of ipfs/ipnsAddress
            voucher.ipnsAddress = event.ipfsAddress
            voucherBox.put(voucher)
            Web3jManager.INSTANCE.storePriceList(this, voucher.id, voucher.tokenAddress, voucher.ipnsAddress)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTokenCreated(event: TokenCreateEvent) {
        var tokenContractaddress = event.address
        var tokenName = event.name
        var tokenType = Tokens.Type.EUR.type
        if (event.type.equals(Tokens.Type.VOUCHER.type)) {
            tokenType = Tokens.Type.VOUCHER.type
        }

        if (ValletApp.activeToken == null || ValletApp.activeToken!!.tokenAddress != tokenContractaddress) {
            supportActionBar!!.title = tokenName
            val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            val voucher = Token(0, tokenName!!, tokenContractaddress!!, 0, tokenType, "", true, 0, "")
            tokenBox.put(voucher)

            progress_overlay.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    @Subscribe
    fun onIssueTokenEvent(event: IssueTokenEvent) {
        var address = Wallet.formatAddress(event.userAddress)
        var amount = BigInteger.ZERO
        var userName = event.userName
        if (userName.isNullOrEmpty()) {
            userName = ""
        }
        addUserToAddressBook(event.userAddress, userName!!)
        if (ValletApp.activeToken?.tokenType.equals(Tokens.Type.EUR.type)) {
            amount = BigInteger.valueOf(Wallet.convertEUR2ATS(event.amount).toLong())
        } else {
            amount = BigInteger(event.amount)
        }
        if (amount > BigInteger.ZERO) {
            Web3jManager.INSTANCE.issueTokensTo(this, address, amount, ValletApp.activeToken!!.tokenAddress, userName)
            // Request funds for user to be able to consume tokens
            // TODO calculate how much we should request base on the amount
            // TODO check balance before
            FaucetManager.INSTANCE.getFounds(this, address)
        } else {
            EventBus.getDefault().post(ErrorEvent("Value must be positive"))
        }
    }

    @Subscribe
    fun onTotalSupplyEvent(event: TokenTotalSupplyEvent) {
        runOnUiThread {
            if (ValletApp.activeToken!!.tokenAddress.equals(event.address)) {
                if (ValletApp.activeToken!!.tokenType.equals(Tokens.Type.EUR.type)) {
                    circulating_vouchers_value.text = Formatter.currency(Wallet.convertATS2EUR(event.value))
                } else {
                    circulating_vouchers_value.text = event.value.toString()
                }
            }
        }
    }

    @Subscribe
    fun onDeepLinkUserAddEvent(event: DeepLinkUserAddEvent) {
        IssueDialogFragment.newInstance(event.user).show(supportFragmentManager, event.user.name)
    }

    @Subscribe
    fun onPriceListEvent(event: PriceListAddressEvent) {
        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        val token = tokenBox.query().equal(Token_.tokenAddress, event.tokenAddress).build().findFirst()
        token!!.ipnsAddress = event.ipfsAddress
        tokenBox.put(token)
        if (token!!.remoteWriteStoragePresent()) {
            token.storage().fetch(token.ipnsAddress)
        } else {
            token.storage().create()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);
    }

    override fun onResume() {
        super.onResume()
        NetworkUtils.isInternetAvailable()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        if (hideDeleteProducts) {
            menu.findItem(R.id.menu_delete).setVisible(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.getItemId()) {
                R.id.menu_qr_code -> {
                    val intent = Intent(this, ShowQrCodeActivity::class.java)
                    startActivity(intent)
                    return true
                }
                R.id.menu_backup -> {
                    val intent = Intent(this, BackupActivity::class.java)
                    startActivity(intent)
                    return true
                }
                else -> return super.onOptionsItemSelected(item)
            }
        } else {
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null && result.contents != null) {
                try {
                    val uri = Uri.parse(result.contents)
                    ValletUriParser.invoke(uri)

                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid uri: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        if (requestCode == REQUEST_BT_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                BTUtils.startScanningForAddresses(this@AdminActivity)
            }
        }
    }

    private fun addUserToAddressBook(address: String, name: String) {
        val userBox = ValletApp.getBoxStore().boxFor(User::class.java)
        val user = User(0, name, address)
        userBox.put(user)
    }

    private fun createToken(tokenName: String) {
        val voucherDecimal = 12;
        var voucherType = Tokens.Type.EUR.type

        // TODO trigger that only if balance is lower then needed amount for creating transaction.
        // For now this is triggered anyway just once a start of the app
        if (true) { // TOOD Check for balance if 0 request funds and create new token if balance is positive generate only new token
            FaucetManager.INSTANCE.getFoundsAndGenerateNewToken(this, ValletApp.wallet!!.address, tokenName, voucherType, voucherDecimal)
        } else {
            Web3jManager.INSTANCE.generateNewToken(this, tokenName, voucherType, voucherDecimal)
        }
    }

    private fun createWallet() {
        val password: String = Random.nextAlphanumericString()
        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)

        // Store password for the wallet file in shared pref in case if the db wil be corrupted that we can restore
        val editor = sharedPref.edit()
        editor.putString(resources.getString(R.string.shared_pref_wallet_password), password)
        editor.commit()

        val walletFile = Web3jManager.INSTANCE.createWallet(this, password)
        val walletAddress = Web3jManager.INSTANCE.getWalletAddressFromFile(walletFile)
        ValletApp.wallet = Wallet(0, "Main", walletAddress, walletFile)
    }


    private fun prepareHeader() {
        setSupportActionBar(toolbar);

        if (ValletApp.activeToken != null)
            supportActionBar!!.title = ValletApp.activeToken!!.name
    }


}
