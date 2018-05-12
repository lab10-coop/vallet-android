package io.lab10.vallet.admin.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lab10.vallet.R
import io.lab10.vallet.admin.models.Users
import kotlinx.android.synthetic.admin.fragment_issue_voucher.*
import kotlinx.android.synthetic.admin.fragment_issue_voucher.view.*

class IssueVoucherFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private var mSectionsPagerAdapter: IssueVoucherStepsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val inflater = inflater.inflate(R.layout.fragment_issue_voucher, container, false)

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = IssueVoucherStepsPagerAdapter(childFragmentManager)

        // Set up the ViewPager with the sections adapter.
        inflater.issueVoucherViewPager.adapter = mSectionsPagerAdapter

        return inflater
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                IssueVoucherFragment()
    }
}
