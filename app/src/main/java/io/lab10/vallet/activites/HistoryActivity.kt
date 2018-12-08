package io.lab10.vallet.activites

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.HistoryRecyclerViewAdapter
import io.lab10.vallet.events.PendingTransactionEvent
import io.lab10.vallet.events.RedeemVoucherEvent
import io.lab10.vallet.events.TransferVoucherEvent
import io.lab10.vallet.models.History
import io.lab10.vallet.models.ValletTransaction
import kotlinx.android.synthetic.main.activity_history.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HistoryActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener{
    override fun onRefresh() {
        runOnUiThread {
            fetchHistory()
            viewAdapter.notifyDataSetChanged()
            swipe_container.setRefreshing(false);
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle(R.string.history)

        viewManager = LinearLayoutManager(this)
        if (ValletApp.activeToken?.tokenType != null) {
            viewAdapter = HistoryRecyclerViewAdapter(History.getTransactions(ValletApp.activeToken!!.tokenAddress), ValletApp.activeToken!!.tokenType)
            recyclerView = findViewById<RecyclerView>(R.id.historyRecycler).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
            swipe_container.setOnRefreshListener(this)

            managePlaceholder()
            fetchHistory()
        } else {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // TODO add latest block
    private fun fetchHistory() {
        Web3jManager.INSTANCE.fetchAllTransaction(this, ValletApp.activeToken!!.tokenAddress, ValletApp.wallet!!.address)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        runOnUiThread {
            var transaction = ValletTransaction(0, "", event.value.toLong(), event.blockNumber.toLong(), event.transactionId, event.to, event.tokenAddress)
            History.addTransaction(transaction)
            managePlaceholder()
            if (ValletApp.activeToken != null && ValletApp.activeToken!!.tokenAddress.equals(event.tokenAddress)) {
                (viewAdapter as HistoryRecyclerViewAdapter).setTransactions(History.getTransactions(ValletApp.activeToken!!.tokenAddress))
                viewAdapter.notifyDataSetChanged()
            }
        }


    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        runOnUiThread {
            var transaction = ValletTransaction(0, "", -event.value.toLong(), event.blockNumber.toLong(), event.transactionId, event.to, event.tokenAddress)
            History.addTransaction(transaction)
            if (ValletApp.activeToken != null && ValletApp.activeToken!!.tokenAddress.equals(event.tokenAddress)) {
                (viewAdapter as HistoryRecyclerViewAdapter).setTransactions(History.getTransactions(ValletApp.activeToken!!.tokenAddress))
                viewAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    private fun managePlaceholder() {
        if (recyclerView.adapter.itemCount == 0) {
            recyclerView.visibility = View.GONE
            empty_view.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            empty_view.visibility = View.GONE
        }
    }

}
