package io.lab10.vallet

import android.content.Context
import android.graphics.BitmapFactory
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import io.lab10.vallet.fragments.ProductFragment.OnListFragmentInteractionListener
import com.squareup.picasso.Picasso
import io.lab10.vallet.events.ProductRemoveEvent
import io.lab10.vallet.models.*
import io.lab10.vallet.models.Token
import io.objectbox.Box
import kotlinx.android.synthetic.main.fragment_product.view.*
import org.greenrobot.eventbus.EventBus


/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class ProductRecyclerViewAdapter(private val mValues: List<Product>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<ProductRecyclerViewAdapter.ProductViewHolder>() {


    private var voucher: io.lab10.vallet.models.Token? = null
    private var voucherBox: Box<io.lab10.vallet.models.Token>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false)
        voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        // Fetch voucher base on the first product (all products are from the same
        // voucher so does not matter which one we will pick
        voucher = (voucherBox  as Box<io.lab10.vallet.models.Token>).query().equal(Token_.tokenAddress, mValues.first().token).build().findFirst()

        if (voucher!!.type != 0) {
            view.voucherTypeIcon.setBackgroundResource(R.drawable.voucher_icon)
        }
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.mItem = mValues.get(position)
        holder.mProductName.setText(mValues.get(position).name)

        if (voucher!!.type != 0) {
            holder.mProductPrice.setText(mValues.get(position).price.toString())
        } else {
            holder.mProductPrice.setText(Wallet.convertATS2EUR(mValues.get(position).price).toString())
        }

        holder.mProductNfcTagId.setText(mValues.get(position).nfcTagId.toString())
        val imageIPFSAddress = mValues.get(position).imagePath
        // First load cached local file
        var bmImg = BitmapFactory.decodeFile(mValues.get(position).localImagePath);
        holder.mProductImage.setImageBitmap(bmImg);
        Picasso.get().load("https://ipfs.io/ipfs/" + imageIPFSAddress).into(holder.mProductImage)

        holder.mProductImage.setOnClickListener {
            val product = holder.mItem
            if (product != null) {
                mListener?.onProductClickListner(product)
            }
        }
        holder.mProductImage.setOnLongClickListener() {
            mListener?.onProductLongClickListner(holder)
            true
        }
        holder.mBackgroundArea.setOnClickListener() {
            mListener?.onProductCancelRemoveListner(holder)
        }

        holder.mDeleteProductImage.setOnClickListener() {
            holder.mBackgroundArea.visibility = View.GONE
            removeProduct((holder.mItem as Product).id)
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

    private fun removeProduct(id: Long) {
        val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
        productBox.query().equal(Product_.id, id).build().remove()
        // TODO replace that by observer on the table
        EventBus.getDefault().post(ProductRemoveEvent())
    }

    inner class ProductViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mProductName: TextView
        val mProductImage: ImageView
        val mProductPrice: TextView
        val mProductNfcTagId: TextView
        val mDeleteProductImage: ImageView
        var mItem: Product? = null
        val mBackgroundArea: ConstraintLayout

        init {
            mProductName = mView.findViewById(R.id.productName) as TextView
            mProductImage = mView.findViewById(R.id.productImage) as ImageView
            mProductPrice = mView.findViewById(R.id.productPrice) as TextView
            mProductNfcTagId = mView.findViewById(R.id.productNfcTag) as TextView
            mDeleteProductImage = mView.findViewById(R.id.deleteButtonImage) as ImageView
            mBackgroundArea = mView.findViewById(R.id.backgroundArea)

        }

        override fun toString(): String {
            return super.toString() + " '" + mProductName.getText() + "'"
        }
    }
}
