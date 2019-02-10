package io.lab10.vallet.admin.adapters

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.models.User
import io.lab10.vallet.models.User_
import io.lab10.vallet.models.ValletTransaction
import io.lab10.vallet.models.Wallet
import io.lab10.vallet.utils.Formatter

class SimpleHistoryViewAdapter(private val history: MutableList<ValletTransaction>, val voucherType: Int) :
        RecyclerView.Adapter<SimpleHistoryViewAdapter.ViewHolder>() {

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mValue: TextView = mView.findViewById(R.id.value) as TextView
        var mTransaction: ValletTransaction? = null
        var mIcon: ImageView = mView.findViewById(R.id.icon) as ImageView
        var mText: TextView = mView.findViewById(R.id.text) as TextView

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
            holder.mValue.text = Formatter.currency(Wallet.convertATS2EUR(history[position].value))
        } else {
            holder.mValue.text = history[position].value.toString()
        }
        holder.mText.text = history[position].description()
        if (history[position].value < 0) {
            holder.mIcon.setImageResource(R.drawable.ic_arrow_downward_24dp)
        } else {
            holder.mIcon.setImageResource(R.drawable.ic_arrow_upward_24dp)
        }
    }

    override fun getItemCount() = history.size

    fun setTransactions(transactons: MutableList<ValletTransaction>) {
        history.clear()
        history.addAll(transactons)
    }
}