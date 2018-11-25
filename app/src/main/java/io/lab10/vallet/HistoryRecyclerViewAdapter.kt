package io.lab10.vallet

import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.lab10.vallet.models.*

class HistoryRecyclerViewAdapter(private val history: MutableList<ValletTransaction>, val voucherType: String) :
        RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mValue: TextView = mView.findViewById(R.id.value) as TextView
        val mText: TextView = mView.findViewById(R.id.text) as TextView
        var mTransaction: ValletTransaction? = null
        var mIcon: ImageView = mView.findViewById(R.id.icon) as ImageView


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

        // TODO history could change in the background while we are setting the view and the app could crash
        try {
            if (history.size > 0 && history.size >= position) {
                holder.mTransaction = history[position]
                if (voucherType.equals(Tokens.Type.EUR.type)) {
                    holder.mValue.text = Wallet.convertATS2EUR(history[position].value).toString() + "€"
                } else {
                    holder.mValue.text = history[position].value.toString()
                }
                holder.mText.text = history[position].description()
                if (history[position].value < 0) {
                    holder.mIcon.setImageResource(R.drawable.ic_arrow_downward_24dp)
                }
            }
        } catch(e: Exception) {
            // TODO do nothing handle the crash by solving changing history
            // Find out how to make sure that history won't change while we are loading the view
        }

    }

    override fun getItemCount() = history.size

    fun setTransactions(transactons: MutableList<ValletTransaction>) {
        history.clear()
        history.addAll(transactons)
    }
}