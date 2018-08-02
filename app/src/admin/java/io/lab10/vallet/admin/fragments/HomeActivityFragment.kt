package io.lab10.vallet.admin.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import io.lab10.vallet.activites.HistoryActivity
import io.lab10.vallet.R
import io.lab10.vallet.admin.activities.DebugActivity
import io.lab10.vallet.events.RedeemVoucherEvent
import io.lab10.vallet.events.TransferVoucherEvent
import kotlinx.android.synthetic.admin.fragment_home_activity.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import io.lab10.vallet.ValletApp
import io.lab10.vallet.HistoryRecyclerViewAdapter
import io.lab10.vallet.admin.activities.AdminActivity
import io.lab10.vallet.admin.activities.ShowQrCodeActivity
import io.lab10.vallet.events.TokenTotalSupplyEvent
import io.lab10.vallet.models.History
import io.lab10.vallet.models.ValletTransaction
import io.lab10.vallet.models.ValletTransaction_
import io.lab10.vallet.models.Wallet
import io.objectbox.android.AndroidScheduler

class HomeActivityFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private var debugCount: Int = 0
    private var debugOn: Boolean = false
    private var viewHolder: View? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        val valletTransactionBox = ValletApp.getBoxStore().boxFor(ValletTransaction::class.java)
        val query = valletTransactionBox.query().build()
        if (ValletApp.activeToken != null) {
            query.subscribe().on(AndroidScheduler.mainThread()).transform { transaction -> valletTransactionBox.query().build().property(ValletTransaction_.value).sum() }
                    .observer { sum ->
                        if (ValletApp.activeToken!!.tokenType == 0) {
                            viewHolder!!.voucherCountLabel.text = Wallet.convertATS2EUR(sum).toString()
                        } else {
                            viewHolder!!.voucherCountLabel.text = sum.toString()
                        }

                    }
        }
        query.subscribe().on(AndroidScheduler.mainThread()).transform{ transaction -> valletTransactionBox.query().orderDesc(ValletTransaction_.blockNumber).build().find(0,2)}
                .observer { recent ->
                    (viewAdapter as HistoryRecyclerViewAdapter).setTransaction(recent)
                    viewAdapter.notifyDataSetChanged()
                }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        if (isAdded) {
            val transfer = resources.getString(R.string.transfer)
            var transaction = ValletTransaction(0, transfer, event.value.toLong(), event.blockNumber.toLong(), event.transactionId, event.to)
            History.addTransaction(transaction)
            History.reloadTransactions()
            activity.runOnUiThread {
                viewAdapter.notifyDataSetChanged()
            }
        }
    }
    @Subscribe
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        if (isAdded) {
            val transfer = resources.getString(R.string.redeem)
            var transaction = ValletTransaction(0, transfer, event.value.toLong(), event.blockNumber.toLong(), event.transactionId, event.to)
            History.addTransaction(transaction)
            History.reloadTransactions()
            activity.runOnUiThread {
                viewAdapter.notifyDataSetChanged()
            }
        }
    }

    @Subscribe
    fun onTotalSupplyEvent(event: TokenTotalSupplyEvent) {
        if (ValletApp.activeToken!!.tokenAddress.equals(event.address)) {
            if (ValletApp.activeToken!!.tokenType == 0) {
                viewHolder!!.voucherCountLabel.text = Wallet.convertATS2EUR(event.value).toString()
            } else {
                viewHolder!!.voucherCountLabel.text = event.value.toString()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        viewHolder =  inflater.inflate(R.layout.fragment_home_activity, container, false) as View


        viewManager = LinearLayoutManager(activity)
        var recentTransaction = History.getRecent()
        if (recentTransaction.size > 0) {
            viewHolder!!.noActivitiesPlaceHolder.visibility = View.GONE
        }
        if ((activity as AdminActivity).voucher?.tokenType == 1) {
            viewAdapter = HistoryRecyclerViewAdapter(recentTransaction, 1)
            viewHolder!!.voucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon_white)
        } else {
            viewAdapter = HistoryRecyclerViewAdapter(recentTransaction, 0)
            viewHolder!!.voucherTypeIcon.setBackgroundResource(R.drawable.euro_icon_white)
        }

        if (ValletApp.activeToken != null)
            viewHolder!!.tokenNameLabel.text = ValletApp.activeToken!!.name

        recyclerView = viewHolder!!.historyRecycler.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewHolder!!.showQrCode.setOnClickListener() { _ ->
            val intent = Intent(activity, ShowQrCodeActivity::class.java)
            startActivity(intent)
        }

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

        viewHolder!!.view_history_label.setOnClickListener { v ->
            val intent = Intent(activity, HistoryActivity::class.java)
            startActivity(intent)
        }


        if (ValletApp.activeToken == null) {
            activity.findViewById<View>(R.id.progress_overlay).setVisibility(View.VISIBLE);
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            Web3jManager.INSTANCE.getCirculatingVoucher(activity, ValletApp.activeToken!!.tokenAddress)
        }
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
