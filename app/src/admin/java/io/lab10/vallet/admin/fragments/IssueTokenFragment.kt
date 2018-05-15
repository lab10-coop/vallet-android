package io.lab10.vallet.admin.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import io.lab10.vallet.R
import io.lab10.vallet.admin.models.Users
import kotlinx.android.synthetic.admin.fragment_issue_token.*
import kotlinx.android.synthetic.admin.fragment_issue_token.view.*
import java.io.File
import java.math.BigInteger

class IssueTokenFragment : DialogFragment() {

    private var userName: String? = null
    private var userAddress: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            userName = arguments.getString(USER_NAME_PARAM)
            userAddress = arguments.getString(USER_ADDRESS_PARAM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_issue_token, container, false)

        view.issueUserName.text = userName

        view.issueButton.setOnClickListener() { v ->
            val amountInput = voucherAmountInput.text.toString()
            val amount = BigInteger(amountInput)
            val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val walletFile = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_wallet_file), "")
            if (walletFile != "" && userAddress != null) {
                val walletPath = File(context.filesDir, walletFile)
                var credentials = Web3jManager.INSTANCE.loadCredential("123", walletPath.absolutePath)
                try {
                    Web3jManager.INSTANCE.issueTokensTo(activity, credentials, userAddress!!, amount)

                } catch (e: Exception) {
                    dialog.dismiss()
                }
            }
        }

        view.closeButton.setOnClickListener() { v ->
            dialog.dismiss()
        }

        // Inflate the layout for this fragment
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    fun updateUser(user: Users.User) {
        userAddress = user.address
        userName = user.name
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(user: Uri)
    }

    companion object {

        val USER_NAME_PARAM = "user_name"
        val USER_ADDRESS_PARAM = "user_address"

        fun newInstance(user: Users.User) =
            IssueTokenFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_NAME_PARAM, user.name)
                    putString(USER_ADDRESS_PARAM, user.address)
                }
            }
    }
}