package io.lab10.vallet.admin.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lab10.vallet.R
import kotlinx.android.synthetic.admin.fragment_nfctag_dialog.*
import kotlinx.android.synthetic.admin.fragment_nfctag_dialog.view.*

class NFCTagDialogFragment : DialogFragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_nfctag_dialog, container, false)

        view.addButton.setOnClickListener() { v ->
            val nfcTagValue = nfcTagInput.text.toString()
            mListener!!.onNFCTagAdded(nfcTagValue)
            dialog.dismiss()
        }

        view.closeNfcDialogButton.setOnClickListener() { v ->
            dialog.dismiss()
        }

        // Inflate the layout for this fragment
        return view
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
        fun onNFCTagAdded(nfcTag: String)
    }

    companion object {

        fun newInstance() = NFCTagDialogFragment()
    }
}