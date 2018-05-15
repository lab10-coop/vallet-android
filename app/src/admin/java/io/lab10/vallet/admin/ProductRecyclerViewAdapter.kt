package io.lab10.vallet.admin

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.lab10.vallet.R

import io.lab10.vallet.admin.fragments.ProductFragment.OnListFragmentInteractionListener
import io.lab10.vallet.admin.models.Products
import io.lab10.vallet.admin.models.Products.Product
import android.graphics.BitmapFactory


/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class ProductRecyclerViewAdapter(private val mValues: List<Product>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues.get(position)
        holder.mProductName.setText(mValues.get(position).name)
        holder.mProductPrice.setText(mValues.get(position).price.toString())
        // TODO load picture from IPFS
        val bitmap = BitmapFactory.decodeFile(mValues.get(position).imagePath)
        holder.mProductImage.setImageBitmap(bitmap)

        holder.mView.setOnClickListener {
            val product = holder.mItem
            if (product != null) {
                mListener?.onListFragmentInteraction(product)
            }
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mProductName: TextView
        val mProductImage: ImageView
        val mProductPrice: TextView
        var mItem: Products.Product? = null

        init {
            mProductName = mView.findViewById(R.id.productName) as TextView
            mProductImage = mView.findViewById(R.id.productImage) as ImageView
            mProductPrice = mView.findViewById(R.id.productPrice) as TextView

        }

        override fun toString(): String {
            return super.toString() + " '" + mProductName.getText() + "'"
        }
    }
}
