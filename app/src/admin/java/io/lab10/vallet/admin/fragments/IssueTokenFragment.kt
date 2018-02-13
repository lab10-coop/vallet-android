package io.lab10.vallet.admin.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import io.lab10.vallet.R
import io.lab10.vallet.admin.models.Users
import kotlinx.android.synthetic.admin.fragment_issue_token.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [IssueTokenFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [IssueTokenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IssueTokenFragment : Fragment() {

    // TODO: Rename and change types of parameters
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
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_issue_token, container, false)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    fun updateUser(user: Users.User) {
        addressLabel.text = user.name + ": " + user.address
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
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        val USER_NAME_PARAM = "user_name"
        val USER_ADDRESS_PARAM = "user_address"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param user_name Name of the user (Right now just BT device name).
         * @param user_address ETH address of the user.
         * @return A new instance of fragment IssueTokenFragment.
         */
        fun newInstance(): IssueTokenFragment {
            val fragment = IssueTokenFragment()
            return fragment
        }
    }
}// Required empty public constructor
