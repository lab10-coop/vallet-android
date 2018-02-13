package io.lab10.vallet.admin.activities

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import io.lab10.vallet.admin.fragments.DiscoverUsersFragment
import io.lab10.vallet.admin.models.Users
import kotlinx.android.synthetic.admin.activity_issue_voucher.*
import io.lab10.vallet.admin.fragments.IssueTokenFragment

class IssueVoucherActivity : AppCompatActivity(), DiscoverUsersFragment.OnListFragmentInteractionListener, IssueTokenFragment.OnFragmentInteractionListener {
    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val TAG = IssueVoucherActivity::class.java.simpleName
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

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: IssueVoucherStepsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_voucher)

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = IssueVoucherStepsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        issueVoucherViewPager.adapter = mSectionsPagerAdapter
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the steps.
     */
    inner class IssueVoucherStepsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    return DiscoverUsersFragment.newInstance()
                }
                1 -> {
                    return IssueTokenFragment.newInstance()
                }
                else -> {
                    // THIS should never happen
                    return DiscoverUsersFragment.newInstance()
                }
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }
}