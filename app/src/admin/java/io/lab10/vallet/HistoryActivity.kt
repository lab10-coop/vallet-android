package io.lab10.vallet

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.lab10.vallet.admin.HistoryRecyclerViewAdapter
import io.lab10.vallet.events.RedeemVoucherEvent
import io.lab10.vallet.events.TransferVoucherEvent
import io.lab10.vallet.models.History
import kotlinx.android.synthetic.admin.fragment_home_activity.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigInteger
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        fetchHistory()

        viewManager = LinearLayoutManager(this)
        viewAdapter = HistoryRecyclerViewAdapter(History.getTransactions())

        recyclerView = findViewById<RecyclerView>(R.id.historyRecycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun fetchHistory() {
        Web3jManager.INSTANCE.fetchAllTransaction(this)
    }

    @Subscribe
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        runOnUiThread {
            var transaction = History.Transaction(event.transactionId, "Transfer", event.value)
            History.addItem(transaction)
            viewAdapter.notifyDataSetChanged()
        }


    }
    @Subscribe
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        runOnUiThread {
            var transaction = History.Transaction("TODO", "Spent", event.value)
            History.addItem(transaction)
            viewAdapter.notifyDataSetChanged()
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

}
