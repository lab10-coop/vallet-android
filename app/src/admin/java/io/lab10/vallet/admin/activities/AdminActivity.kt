package io.lab10.vallet.admin.activities

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.view.MenuItem
import io.lab10.vallet.admin.fragments.*
import io.lab10.vallet.admin.models.Products
import io.lab10.vallet.admin.models.Users
import kotlinx.android.synthetic.admin.activity_admin.*
import kotlinx.android.synthetic.admin.fragment_issue_voucher.*


class AdminActivity : AppCompatActivity(), HomeActivityFragment.OnFragmentInteractionListener,
        IssueVoucherFragment.OnFragmentInteractionListener,
        DiscoverUsersFragment.OnListFragmentInteractionListener,
        IssueTokenFragment.OnFragmentInteractionListener,
        PriceListFragment.OnFragmentInteractionListener,
        ProductFragment.OnListFragmentInteractionListener {


    override fun onListFragmentInteraction(item: Products.Product) {
        TODO("not implemented")
    }

    override fun onListFragmentInteraction(item: Users.User) {
        val fragmentPagerAdapter = issueVoucherViewPager.getAdapter() as FragmentPagerAdapter
        for (i in 0 until fragmentPagerAdapter.count) {

            val viewPagerFragment = issueVoucherViewPager.getAdapter().instantiateItem(issueVoucherViewPager, i) as Fragment
            if (viewPagerFragment != null && viewPagerFragment.isAdded) {

                if (viewPagerFragment is IssueTokenFragment) {
                    val oneFragment = viewPagerFragment as IssueTokenFragment
                    if (oneFragment != null) {
                        oneFragment!!.updateUser(item)
                    }
                }
            }
        }
        issueVoucherViewPager.currentItem = 2
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
                    R.id.action_item2 -> selectedFragment = IssueVoucherFragment.newInstance()
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
}
