package io.lab10.vallet.admin.activities

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import android.widget.Toast
import io.lab10.vallet.events.DebugEvent
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.admin.fragments.*
import io.lab10.vallet.models.Products
import io.lab10.vallet.admin.models.Users
import kotlinx.android.synthetic.admin.activity_admin.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AdminActivity : AppCompatActivity(), HomeActivityFragment.OnFragmentInteractionListener,
        DiscoverUsersFragment.OnListFragmentInteractionListener,
        IssueTokenFragment.OnFragmentInteractionListener,
        PriceListFragment.OnFragmentInteractionListener,
        ProductFragment.OnListFragmentInteractionListener {


    override fun onListFragmentInteraction(item: Products.Product) {
        // TODO add interaction for product
    }

    override fun onListFragmentInteraction(user: Users.User) {
        IssueTokenFragment.newInstance(user).show(supportFragmentManager, "dialog")
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val TAG = AdminActivity::class.java.simpleName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        navigation.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                var selectedFragment: Fragment? = null
                when (item.getItemId()) {
                    R.id.action_item1 -> selectedFragment = HomeActivityFragment.newInstance()
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
        Web3jManager.INSTANCE.getCirculatingVoucher(this)

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onError(event: ErrorEvent) {
        Toast.makeText(this, "Error: " + event.message, Toast.LENGTH_LONG).show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDebug(event: DebugEvent) {
        Toast.makeText(this, "Debug: " + event.message, Toast.LENGTH_LONG).show()
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
