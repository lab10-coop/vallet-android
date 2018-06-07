package io.lab10.vallet.admin.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import io.lab10.vallet.R
import io.lab10.vallet.admin.activities.DebugActivity
import io.lab10.vallet.events.RedeemVoucherEvent
import io.lab10.vallet.events.TransferVoucherEvent
import kotlinx.android.synthetic.admin.fragment_home_activity.*
import kotlinx.android.synthetic.admin.fragment_home_activity.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigInteger

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeActivityFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeActivityFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private var debugCount: Int = 0
    private var debugOn: Boolean = false
    private var viewHolder: View? = null

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        if (isAdded) {
            activity.runOnUiThread {
                val voucherSum = viewHolder!!.voucherCountLabel.text as String
                var currentValue = BigInteger.ZERO
                if (voucherSum.length > 0) {
                    currentValue = voucherSum.toBigInteger()
                }
                currentValue += event.value
                viewHolder!!.voucherCountLabel.text = currentValue.toString()
            }
        }


    }
    @Subscribe
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        if (isAdded) {
            activity.runOnUiThread {
                val voucherSum = viewHolder!!.voucherCountLabel.text as String
                var currentValue = BigInteger.ZERO
                if (voucherSum.length > 0) {
                    currentValue = voucherSum.toBigInteger()
                }
                currentValue -= event.value
                viewHolder!!.voucherCountLabel.text = currentValue.toString()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        viewHolder =  inflater.inflate(R.layout.fragment_home_activity, container, false) as View

        viewHolder!!.voucherTypeIcon.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    debugCount += 1
                    if (debugCount > 5) {
                        debugOn = true
                    }
                }
                MotionEvent.ACTION_UP -> {

                    if (debugOn) {
                        val intent = Intent(activity, DebugActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            return@OnTouchListener true
        })
        return viewHolder
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
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
           HomeActivityFragment()
    }
}
