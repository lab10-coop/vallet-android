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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import kotlinx.android.synthetic.admin.activity_voucher.view.*
import kotlinx.android.synthetic.admin.fragment_voucher_name.*
import kotlinx.android.synthetic.admin.fragment_voucher_name.view.*
import kotlinx.android.synthetic.admin.fragment_voucher_settings.view.*
import java.io.File
import java.math.BigInteger


class VoucherActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mStepsPagerAdapter: StepsPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)
        voucherFinishBtn.visibility = View.GONE


        mStepsPagerAdapter = StepsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the step adapter.
        voucherSettingsViewPager.adapter = mStepsPagerAdapter
        voucherSettingsViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    voucherNextBtn.visibility = View.VISIBLE
                    voucherFinishBtn.visibility = View.GONE
                    step1.setImageDrawable(getDrawable(R.drawable.step_circle_active))
                    step2.setImageDrawable(getDrawable(R.drawable.step_circle))
                    step3.setImageDrawable(getDrawable(R.drawable.step_circle))

                }

                if (position == 1) {
                    voucherNextBtn.visibility = View.GONE
                    voucherFinishBtn.visibility = View.VISIBLE
                    step1.setImageDrawable(getDrawable(R.drawable.step_circle))
                    step2.setImageDrawable(getDrawable(R.drawable.step_circle_active))
                    step3.setImageDrawable(getDrawable(R.drawable.step_circle))
                }
            }

        })
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the steps.
     */
    inner class StepsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when(position) {
                0 -> {
                    return VoucherNameFragment.newInstance(voucherSettingsViewPager)
                }
                1 -> {
                    return VoucherSettingsFragment.newInstance()
                } else -> {
                    return VoucherNameFragment.newInstance(voucherSettingsViewPager)
                }
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    class VoucherNameFragment : Fragment(), View.OnClickListener {

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.voucherNextBtn -> {
                    if (inputVoucherName.text.toString().length < 3) {
                        Toast.makeText(activity, resources.getString(R.string.error_voucher_name_too_short), Toast.LENGTH_SHORT).show()
                    } else {
                        val sharedPref = activity.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString(resources.getString(R.string.shared_pref_voucher_name), inputVoucherName.text.toString())
                        editor.commit()
                        voucherViewPager?.currentItem = 1
                    }
                }
            }
        }

        override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {

            val rootView = inflater.inflate(R.layout.fragment_voucher_name, viewGroup, false)
            activity.voucherNextBtn.setOnClickListener(this)
            val sharedPref = activity.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val voucherName = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_name), "")
            rootView.inputVoucherName.setText(voucherName)
            return rootView
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

    class VoucherSettingsFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?, savedInstanceState: Bundle?): View? {

            val rootView = inflater.inflate(R.layout.fragment_voucher_settings, viewGroup, false)
            val adapter = ArrayAdapter.createFromResource(this.context,
                    R.array.voucher_type, android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinner = rootView.findViewById(R.id.voucherTypeSpinner) as Spinner
            spinner.adapter = adapter
            val finishButton = activity.voucherFinishBtn
            finishButton.setOnClickListener() {v ->
                val sharedPref = activity.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putInt(resources.getString(R.string.shared_pref_voucher_decimal), 12)
                editor.putString(resources.getString(R.string.shared_pref_voucher_type), rootView.voucherTypeSpinner.selectedItem.toString())
                editor.putBoolean("FIRST_RUN", false)
                // TODO: Manage password for the key
                val walletFile = Web3jManager.INSTANCE.createWallet(context, "123")
                val walletAddress = Web3jManager.INSTANCE.getWalletAddress(walletFile)
                editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_address), walletAddress)
                editor.putString(resources.getString(R.string.shared_pref_voucher_wallet_file), walletFile)
                editor.commit()

                val intent = Intent(view?.context, AdminActivity::class.java)
                startActivity(intent)
            }
            return rootView
        }

        companion object {
            fun newInstance() : VoucherSettingsFragment {
                val fragment = VoucherSettingsFragment()
                return fragment
            }
        }
    }
}
