package io.lab10.vallet.admin.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import io.lab10.vallet.ValletApp
import io.lab10.vallet.admin.fragments.*
import io.lab10.vallet.admin.models.Users
import io.lab10.vallet.events.*
import io.lab10.vallet.fragments.ProductFragment
import io.lab10.vallet.models.*
import kotlinx.android.synthetic.admin.activity_admin.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AdminActivity : AppCompatActivity(), HomeActivityFragment.OnFragmentInteractionListener,
        DiscoverUsersFragment.OnListFragmentInteractionListener,
        IssueTokenFragment.OnFragmentInteractionListener,
        PriceListFragment.OnFragmentInteractionListener,
        ProductFragment.OnListFragmentInteractionListener {

    override fun onListFragmentInteraction(item: Product) {
        val intent = Intent(this, AddProductActivity::class.java)
        intent.putExtra("PRODUCT_ID", item.id)
        startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
    }

    override fun onListFragmentInteraction(user: Users.User) {
        IssueTokenFragment.newInstance(user).show(supportFragmentManager, "dialog")
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val TAG = AdminActivity::class.java.simpleName
    var voucher: Voucher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        Web3jManager.INSTANCE.getTokenContractAddress(this)

        var voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        voucher = voucherBox.query().build().findFirst()

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
        // TODO move to home fragment
        if (voucher?.tokenAddress != null)
            Web3jManager.INSTANCE.getCirculatingVoucher(this, voucher!!.tokenAddress)

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
        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        var voucher = voucherBox.query().equal(Voucher_.id, event.voucherId).build().findFirst()
        if (voucher != null) {
            voucher.ipnsAdddress = event.ipnsAddress
            voucherBox.put(voucher)
            Toast.makeText(this, "Ipfs address created", Toast.LENGTH_SHORT).show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTokenCreated(event: TokenCreateEvent) {
        var tokenContractaddress = event.address
        var voucherName = event.name
        var voucherType = 0
        if (event.type.equals(Vouchers.Type.VOUCHER.toString()) ) {
            voucherType = 1
        }
        val voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        val voucher = Voucher(0, voucherName!!, tokenContractaddress!!, 0, voucherType, "", true)
        val voucherId = voucherBox.put(voucher)
        // Generate IPFS address
        Thread(Runnable {
            var addressName = IPFSManager.INSTANCE.publishProductList(this);
            if (addressName != null) {
                Log.d(TAG, "Address of products list: " + addressName)
                EventBus.getDefault().post(ProductListPublishedEvent(voucherId, addressName))
            }
        }).start()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }


}
