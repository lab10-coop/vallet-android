package io.lab10.vallet.admin.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import io.lab10.vallet.admin.fragments.DiscoverUsersFragment
import io.lab10.vallet.admin.models.Users
import kotlinx.android.synthetic.admin.activity_issue_voucher.*
import org.greenrobot.eventbus.EventBus
import android.widget.Toast
import io.lab10.vallet.admin.events.NewAddressEvent
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe



class IssueVoucherActivity : AppCompatActivity(), DiscoverUsersFragment.OnListFragmentInteractionListener {

    val TAG = IssueVoucherActivity::class.java.simpleName
    override fun onListFragmentInteraction(item: Users.User) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);

    }

    override fun onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewAddressEvent) {
        Toast.makeText(this, event.deviceName + " " + event.address, Toast.LENGTH_SHORT).show()
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
                    return DiscoverUsersFragment.newInstance()
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