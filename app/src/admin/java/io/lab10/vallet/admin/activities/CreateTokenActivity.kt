package io.lab10.vallet.admin.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lab10.vallet.R

import kotlinx.android.synthetic.admin.activity_voucher.*
import android.widget.Toast
import kotlinx.android.synthetic.admin.fragment_voucher_name.*
import kotlinx.android.synthetic.admin.fragment_voucher_name.view.*
import android.net.ConnectivityManager
import io.lab10.vallet.admin.events.CreateTokenEvent
import org.greenrobot.eventbus.EventBus


class CreateTokenActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mStepsPagerAdapter: StepsPagerAdapter? = null
    var voucherName: String = "ATS"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)

        mStepsPagerAdapter = StepsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the step adapter.
        voucherSettingsViewPager.adapter = mStepsPagerAdapter
        voucherSettingsViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                // TODO nothing to do here since we have just one page at the moment
            }

        })
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the steps.
     */
    inner class StepsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    return VoucherNameFragment.newInstance(voucherSettingsViewPager)
                }
                else -> {
                    return VoucherNameFragment.newInstance(voucherSettingsViewPager)
                }
            }
        }

        override fun getCount(): Int {
            return 1
        }
    }

    class VoucherNameFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {

            val rootView = inflater.inflate(R.layout.fragment_voucher_name, viewGroup, false)
            val sharedPref = activity.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val voucherName = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_name), "")
            rootView.inputVoucherName.setText(voucherName)

            val finishButton = rootView.getStarterd
            finishButton.setOnClickListener() { v ->

                // TODO replace that with token.valid?
                if (inputVoucherName.text.length < 3) {
                    Toast.makeText(activity, "Please fill the name of the store at least 3 characters", Toast.LENGTH_SHORT).show()
                } else {
                    if (haveNetworkConnection()) {
                        // TODO add voucher.valid? before submitting

                        val name = inputVoucherName.text.toString()
                        EventBus.getDefault().post(CreateTokenEvent(name))


                        val intent = Intent(view?.context, AdminActivity::class.java)
                        intent.putExtra("TOKEN_NAME", name)
                        startActivity(intent)


                    } else {
                        Toast.makeText(activity, "Pleaes connect to the internet to continue", Toast.LENGTH_LONG).show()
                    }
                }
            }

            return rootView
        }

        private fun haveNetworkConnection(): Boolean {
            var haveConnectedWifi = false
            var haveConnectedMobile = false

            val cm = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.allNetworkInfo
            for (ni in netInfo) {
                if (ni.typeName.equals("WIFI", ignoreCase = true))
                    if (ni.isConnected)
                        haveConnectedWifi = true
                if (ni.typeName.equals("MOBILE", ignoreCase = true))
                    if (ni.isConnected)
                        haveConnectedMobile = true
            }
            return haveConnectedWifi || haveConnectedMobile
        }

        companion object {
            var voucherViewPager: ViewPager? = null

            fun newInstance(viewPager: ViewPager): VoucherNameFragment {
                val fragment = VoucherNameFragment()
                voucherViewPager = viewPager
                return fragment
            }
        }
    }
}
