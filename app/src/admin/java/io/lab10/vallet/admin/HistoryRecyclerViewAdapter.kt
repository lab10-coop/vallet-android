package io.lab10.vallet.admin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.lab10.vallet.R
import io.lab10.vallet.models.ValletTransaction
import io.lab10.vallet.models.Wallet

class HistoryRecyclerViewAdapter(private val history: MutableList<ValletTransaction>, val voucherType: Int) :
        RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mName: TextView
        val mValue: TextView
        val mVoucherTypeIcon: ImageView
        var mTransaction: ValletTransaction? = null

        init {
            mName = mView.findViewById(R.id.name) as TextView
            mValue = mView.findViewById(R.id.value) as TextView
            mVoucherTypeIcon = mView.findViewById(R.id.voucherTypeIcon) as ImageView
        }

        override fun toString(): String {
            return super.toString() + " '" + mTransaction!!.id + "'"
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): HistoryRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_item, parent, false) as View

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mName.text = history[position].name
        holder.mTransaction = history[position]
        if (voucherType == 0) {
            holder.mValue.text = Wallet.convertATS2EUR(history[position].value).toString()
        } else {
            holder.mValue.text = history[position].value.toString()
            holder.mVoucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon)
        }

    }

    override fun getItemCount() = history.size

    fun setTransaction(transactons: MutableList<ValletTransaction>) {
        history.clear()
        history.addAll(transactons)
    }
}