package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import io.lab10.vallet.ProductRecyclerViewAdapter
import io.lab10.vallet.ValletApp
import io.lab10.vallet.admin.fragments.*
import io.lab10.vallet.admin.models.BTUsers
import io.lab10.vallet.admin.models.User
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


class AdminActivity : AppCompatActivity(), HomeActivityFragment.OnFragmentInteractionListener,
        DiscoverUsersFragment.OnListFragmentInteractionListener,
        IssueDialogFragment.OnFragmentInteractionListener,
        PriceListFragment.OnFragmentInteractionListener,
        ProductListFragment.OnListFragmentInteractionListener {

    override fun onProductClickListner(item: Product) {
        val intent = Intent(this, AddProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", item.id)
        startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)    }

    override fun onProductLongClickListner(holder: ProductRecyclerViewAdapter.ProductViewHolder) {
        holder.mBackgroundArea.visibility = View.VISIBLE
    }

    override fun onProductCancelRemoveListner(holder: ProductRecyclerViewAdapter.ProductViewHolder) {
        holder.mBackgroundArea.visibility = View.GONE
    }

    override fun onListFragmentInteraction(user: BTUsers.User) {
        IssueDialogFragment.newInstance(user).show(supportFragmentManager, "dialog")
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val TAG = AdminActivity::class.java.simpleName
    var voucher: Token? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val tokenName = intent.getStringExtra("TOKEN_NAME")
        if (tokenName != null) {
            createToken(tokenName)
        }

        Web3jManager.INSTANCE.getTokenContractAddress(this)

        navigation.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                var selectedFragment: Fragment? = null
                when (item.getItemId()) {
                    R.id.action_item1 -> selectedFragment = HomeActivityFragment.newInstance()
                    // TODO Disabled if none voucher is present
                    R.id.action_item2 -> selectedFragment = DiscoverUsersFragment.newInstance()
                    R.id.action_item3 -> selectedFragment = PriceListFragment.newInstance()
                }
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, selectedFragment)
                transaction.commit()
                return true
            }
        })

        //Manually displaying the first fragment - one time only
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, HomeActivityFragment.newInstance())
        transaction.commit()

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
        tokenNameLabel.text = tokenName
        var tokenType = 0
        if (event.type.equals(Tokens.Type.VOUCHER.toString()) ) {
            tokenType = 1
        }
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


    @Subscribe
    fun onPendingTransaction(event: PendingTransactionEvent) {
        val transaction = ValletTransaction(0, "Pending", event.amount.toLong(), 0, "", event.to)
        val transactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        transactionBox.put(transaction)
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
        if (ValletApp.activeToken?.tokenType == 0) {
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    private fun addUserToAddressBook(address: String, name: String) {
        val userBox = ValletApp.getBoxStore().boxFor(User::class.java)
        val user = User(0,name, address)
        userBox.put(user)
    }

    private fun createToken(tokenName: String) {
        val voucherDecimal = 12;
        var voucherType = Tokens.Type.EUR.toString()
        // TODO: Manage password for the key
        val walletFile = Web3jManager.INSTANCE.createWallet(this, "123")
        val walletAddress = Web3jManager.INSTANCE.getWalletAddress(walletFile)
        ValletApp.wallet = Wallet(0, "Main", walletAddress, walletFile)

        // TODO trigger that only if balance is lower then needed amount for creating transaction.
        //

        if (true) { // TOOD Check for balance if 0 request funds and create new token if balance is positive generate only new token
            FaucetManager.INSTANCE.getFoundsAndGenerateNewToken(this, walletAddress, tokenName, voucherType, voucherDecimal)
        } else {
            Web3jManager.INSTANCE.generateNewToken(this, tokenName, voucherType, voucherDecimal)
        }
    }


}
