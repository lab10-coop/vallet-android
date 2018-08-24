package io.lab10.vallet.admin.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.lab10.vallet.R
import io.lab10.vallet.models.ValletTransaction
import io.lab10.vallet.models.Wallet

class SimpleHistoryViewAdapter(private val history: MutableList<ValletTransaction>, val voucherType: Int) :
        RecyclerView.Adapter<SimpleHistoryViewAdapter.ViewHolder>() {

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mValue: TextView
        var mTransaction: ValletTransaction? = null

        init {
            mValue = mView.findViewById(R.id.value) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + mTransaction!!.id + "'"
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {

            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_item, parent, false) as View
            return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mTransaction = history[position]
        if (voucherType == 0) {
            holder.mValue.text = Wallet.convertATS2EUR(history[position].value).toString() + "€"
        } else {
            holder.mValue.text = history[position].value.toString()
        }
    }

    override fun getItemCount() = history.size

    fun setTransaction(transactons: MutableList<ValletTransaction>) {
        history.clear()
        history.addAll(transactons)
    }
}