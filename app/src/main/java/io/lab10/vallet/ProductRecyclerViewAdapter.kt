package io.lab10.vallet

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import io.lab10.vallet.fragments.ProductFragment.OnListFragmentInteractionListener
import com.squareup.picasso.Picasso
import io.lab10.vallet.models.Product
import kotlinx.android.synthetic.main.fragment_product.view.*


/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class ProductRecyclerViewAdapter(private val mValues: List<Product>, private val mListener: OnListFragmentInteractionListener?, private val voucherType: Int) : RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false)
        if (voucherType != 0) {
            view.voucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues.get(position)
        holder.mProductName.setText(mValues.get(position).name)
        holder.mProductPrice.setText(mValues.get(position).price.toString())
        holder.mProductNfcTagId.setText(mValues.get(position).nfcTagId.toString())
        val imageIPFSAddress = mValues.get(position).imagePath
        // First load cached local file
        Picasso.get().load(mValues.get(position).localImagePath).into(holder.mProductImage)
        Picasso.get().load("https://ipfs.io/ipfs/" + imageIPFSAddress).into(holder.mProductImage);

        holder.mView.setOnClickListener {
            val product = holder.mItem
            if (product != null) {
                mListener?.onListFragmentInteraction(product)
            }
        }

        // TODO move to common class
        val sharedPref = holder.mView.context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val debugMode = sharedPref!!.getBoolean(holder.mView.context.resources.getString(R.string.shared_pref_debug_mode), false)
        if (debugMode) {
            holder.mView.productNfcTag.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mProductName: TextView
        val mProductImage: ImageView
        val mProductPrice: TextView
        val mProductNfcTagId: TextView
        var mItem: Product? = null

        init {
            mProductName = mView.findViewById(R.id.productName) as TextView
            mProductImage = mView.findViewById(R.id.productImage) as ImageView
            mProductPrice = mView.findViewById(R.id.productPrice) as TextView
            mProductNfcTagId = mView.findViewById(R.id.productNfcTag) as TextView

        }

        override fun toString(): String {
            return super.toString() + " '" + mProductName.getText() + "'"
        }
    }
}
