package io.lab10.vallet.admin.activities

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.admin.HistoryRecyclerViewAdapter
import io.lab10.vallet.events.RedeemVoucherEvent
import io.lab10.vallet.events.TransferVoucherEvent
import io.lab10.vallet.models.History
import io.lab10.vallet.models.ValletTransaction
import io.lab10.vallet.models.Voucher
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    var voucher: Voucher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        var voucherBox = ValletApp.getBoxStore().boxFor(Voucher::class.java)
        voucher = voucherBox.query().build().findFirst()

        viewManager = LinearLayoutManager(this)
        viewAdapter = HistoryRecyclerViewAdapter(History.getTransactions(), voucher!!.type)

        recyclerView = findViewById<RecyclerView>(R.id.historyRecycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }


        fetchHistory()
    }

    private fun fetchHistory() {
        Web3jManager.INSTANCE.fetchAllTransaction(this, voucher!!.tokenAddress)
    }

    @Subscribe
    fun onTransferVoucherEvent(event: TransferVoucherEvent) {
        runOnUiThread {
            var transaction = ValletTransaction(0, "Transfer", event.value.toLong(), event.blockNumber.toLong(), event.transactionId)
            History.addItem(transaction)
            viewAdapter.notifyDataSetChanged()
        }


    }
    @Subscribe
    fun onTransferVoucherEvent(event: RedeemVoucherEvent) {
        runOnUiThread {
            var transaction = ValletTransaction(0, "Transfer", event.value.toLong(), event.blockNumber.toLong(), event.transactionId)
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
