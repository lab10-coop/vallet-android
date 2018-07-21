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
import java.math.BigInteger
import android.text.InputFilter
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.models.Token
import io.lab10.vallet.models.Wallet
import io.lab10.vallet.utils.EuroInputFilter
import org.greenrobot.eventbus.EventBus

class IssueTokenFragment : DialogFragment() {

    private var userName: String? = null
    private var userAddress: String? = null
    private var voucher: Token? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            userName = arguments.getString(USER_NAME_PARAM)
            userAddress = arguments.getString(USER_ADDRESS_PARAM)
        }
        var voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        voucher = voucherBox.query().build().findFirst()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_issue_token, container, false)

        view.issueUserName.text = userName

        if (voucher!!.type == 0) {
            view.voucherAmountInput.setFilters(arrayOf<InputFilter>(EuroInputFilter(4, 2)))
            view.voucherTypeIcon.setBackgroundResource(R.drawable.euro_icon_black)
        }

        view.issueButton.setOnClickListener() { v ->
            val amountInput = voucherAmountInput.text.toString()

            if (amountInput.isBlank()) {
                EventBus.getDefault().post(ErrorEvent("Value must be present"))
            } else {

                try {
                    var address = Wallet.formatAddress(userAddress)
                    var amount = BigInteger.ZERO
                    if (voucher!!.type == 0) {
                        amount = BigInteger.valueOf(Wallet.convertEUR2ATS(amountInput).toLong())
                    } else {
                        amount = BigInteger(amountInput)
                    }
                    if (amount > BigInteger.ZERO) {
                        Web3jManager.INSTANCE.issueTokensTo(activity, address, amount, voucher!!.tokenAddress)
                        // Request funds for user to be able to consume tokens
                        // TODO calculate how much we should request base on the amount
                        FaucetManager.INSTANCE.getFounds(context, address)
                        dialog.dismiss()
                    } else {
                        EventBus.getDefault().post(ErrorEvent("Value must be positive"))
                    }
                } catch (e: Exception) {
                    EventBus.getDefault().post(ErrorEvent(e.message.toString()))
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