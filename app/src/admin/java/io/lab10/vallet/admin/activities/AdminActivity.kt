package io.lab10.vallet.admin.activities

import android.app.Activity
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
import com.google.zxing.integration.android.IntentIntegrator
import io.ValletUriParser
import io.lab10.vallet.ValletApp
import io.lab10.vallet.admin.fragments.*
import io.lab10.vallet.models.BTUsers
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
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class AdminActivity : AppCompatActivity(), HomeActivityFragment.OnFragmentInteractionListener,
        DiscoverUsersFragment.OnListFragmentInteractionListener,
        IssueDialogFragment.OnFragmentInteractionListener,
        PriceListFragment.OnFragmentInteractionListener,
        ProductListFragment.OnListFragmentInteractionListener {

    override fun onProductClickListner(item: Product) {
        val intent = Intent(this, AddProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", item.id)
        startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
    }

    override fun onListFragmentInteraction(user: BTUsers.User) {
        IssueDialogFragment.newInstance(user).show(supportFragmentManager, "dialog")
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
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_admin)

        // When activity is tirggered from on boarding screen we create token with given name
        val tokenName = intent.getStringExtra("TOKEN_NAME")
        if (tokenName != null) {
            createToken(tokenName)
        }

        // Fetch created token and set it as a active one if non is set yet. This should be triggered
        // only once when app is run first time
        if (ValletApp.activeToken == null) {
            Web3jManager.INSTANCE.getTokenContractAddress(this)
        }

        val myPagerAdapter = MainPagerAdapter(supportFragmentManager)
        myPagerAdapter.addFragment(HomeActivityFragment(), resources.getString(R.string.tab_activity))
        myPagerAdapter.addFragment(PriceListFragment(), resources.getString(R.string.tab_price_list))
        main_pager.adapter = myPagerAdapter
        setFabIssue()
        main_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when(position) {
                    0 -> {
                        setFabIssue()
                        deleteProductVisible(false)
                    }
                    1 -> {
                        setFabAddProduct()
                        deleteProductVisible(true)
                    }
                }
            }
        })

        tab_layout.setupWithViewPager(main_pager)
        prepareHeader()
        EventBus.getDefault().register(this)

    }

    private fun deleteProductVisible(visible: Boolean) {
        hideDeleteProducts = visible
        invalidateOptionsMenu()
    }

    private fun setFabAddProduct() {
        fab_button.setImageResource(R.drawable.add_product_button)
        fab_button.setOnClickListener() { v ->
            val intent = Intent(this@AdminActivity, AddProductActivity::class.java)
            startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
        }
    }

    private fun setFabIssue() {
        fab_button.setImageResource(R.drawable.fab_qr_button)
        fab_button.setOnClickListener() { v ->
            val integrator = IntentIntegrator(this@AdminActivity)
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan()
        }
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
        Toast.makeText(this, "Info: " + event.message, Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductListPublishedEvent(event: ProductListPublishedEvent) {
        val voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        var voucher = voucherBox.query().equal(Token_.id, event.voucherId).build().findFirst()
        if (voucher != null) {
            // TODO if we are using ValletApi we store here uuid if ipfs ipnsAddress
            voucher.ipnsAdddress = event.secret
            voucherBox.put(voucher)
            Toast.makeText(this, "Ipfs address created", Toast.LENGTH_SHORT).show()
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

            progress_overlay.setVisibility(View.GONE)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // prepare remote storage
            Thread(Runnable {
                voucher.storage().create()
            }).start()
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
            Web3jManager.INSTANCE.issueTokensTo(this, address, amount, ValletApp.activeToken!!.tokenAddress)
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
                    circulating_vouchers_value.text = Wallet.convertATS2EUR(event.value).toString() + "€"
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);
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
                else -> return super.onOptionsItemSelected(item)
            }
        } else {
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null && result.contents != null) {
                try {
                    val uri = Uri.parse(result.contents)
                    ValletUriParser.invoke(uri)

                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid uri: " + e.message, Toast.LENGTH_SHORT).show()
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
        // TODO: Manage password for the key
        val walletFile = Web3jManager.INSTANCE.createWallet(this, "123")
        val walletAddress = Web3jManager.INSTANCE.getWalletAddressFromFile(walletFile)
        ValletApp.wallet = Wallet(0, "Main", walletAddress, walletFile)

        // TODO trigger that only if balance is lower then needed amount for creating transaction.
        //

        if (true) { // TOOD Check for balance if 0 request funds and create new token if balance is positive generate only new token
            FaucetManager.INSTANCE.getFoundsAndGenerateNewToken(this, walletAddress, tokenName, voucherType, voucherDecimal)
        } else {
            Web3jManager.INSTANCE.generateNewToken(this, tokenName, voucherType, voucherDecimal)
        }
    }

    private fun prepareHeader() {
        setSupportActionBar(toolbar);

        if (ValletApp.activeToken != null)
            supportActionBar!!.title = ValletApp.activeToken!!.name
    }


}
