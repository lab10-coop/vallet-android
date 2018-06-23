package io.lab10.vallet

import android.content.Intent
import android.media.Image
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import io.lab10.vallet.models.Voucher
import io.lab10.vallet.models.Vouchers
import io.lab10.vallet.models.Wallet
import kotlinx.android.synthetic.client.voucher_item.view.*

class VoucherAdapter(private val myDataset: MutableList<Voucher>) :
        RecyclerView.Adapter<VoucherAdapter.ViewHolder>() {

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView), View.OnClickListener {

        override fun onClick(v: View?) {
            val intent = Intent(mView.context, ProductListActivity::class.java)
            intent.action = "Start"
            intent.putExtra("EXTRA_TOKEN_ADDRESS", mView.voucherTokenAddress.text);
            mView.context.startActivity(intent)
        }

        val mVoucherName: TextView
        val mVoucherBalance: TextView
        val mVoucherTokenAddress: TextView
        val mVoucherType: ImageView

        init {
            mVoucherName = mView.findViewById(R.id.voucherName) as TextView
            mVoucherBalance = mView.findViewById(R.id.voucherBalance) as TextView
            mVoucherTokenAddress = mView.findViewById(R.id.voucherTokenAddress) as TextView
            mVoucherType = mView.findViewById(R.id.voucherTypeIcon) as ImageView
            mView.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " '" + mVoucherBalance.text + "'"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): VoucherAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.voucher_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mVoucherName.text = myDataset[position].name
        holder.mVoucherTokenAddress.text = myDataset[position].tokenAddress
        if (myDataset[position].type != 0) {
            holder.mVoucherType.setBackgroundResource(R.drawable.voucher_icon)
            holder.mVoucherBalance.text = myDataset[position].balance.toString()
        } else {
            holder.mVoucherBalance.text = Wallet.convertATS2EUR(myDataset[position].balance.toLong()).toString()
        }
    }

    override fun getItemCount() = myDataset.size
}