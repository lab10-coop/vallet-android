package io.lab10.vallet.admin.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp

import io.lab10.vallet.admin.activities.AddProductActivity
import io.lab10.vallet.admin.activities.AdminActivity
import io.lab10.vallet.events.*
import io.lab10.vallet.fragments.ProductFragment
import io.lab10.vallet.models.Product
import io.lab10.vallet.models.Products
import io.lab10.vallet.models.Voucher
import kotlinx.android.synthetic.admin.fragment_price_list.view.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus
import org.web3j.protocol.admin.Admin


class PriceListFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_price_list, container, false)

        view.addProductBtn.setOnClickListener() { v ->
            val intent = Intent(activity, AddProductActivity::class.java)
            startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
        }

        refreshProducts()

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == AddProductActivity.PRODUCT_RETURN_CODE) {
                refreshProducts()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductsListEvent(event: ProductsListEvent) {
        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.notifyAboutchange()
        productFragment.swiperefresh.isRefreshing = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductAdded(event: ProductAddedEvent) {
        refreshProducts()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefresh(event: ProductRefreshEvent){
        refreshProducts()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductRemove(event: ProductRemoveEvent) {
        refreshProductsLocal()
        Thread(Runnable {
            var addressName = IPFSManager.INSTANCE.publishProductList(activity);
            if (addressName != null) {
                EventBus.getDefault().post(ProductListPublishedEvent((activity as AdminActivity).voucher!!.id, addressName))
            }
        }).start()
    }

    @Subscribe
    fun onProductListPublished(event: ProductListPublishedEvent) {
        val voucher = (activity as AdminActivity).voucher
        if (voucher != null && voucher.ipnsAdddress != null && voucher.ipnsAdddress.isBlank()) {
            voucher.ipnsAdddress = event.ipnsAddress
            val voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            voucherBox.put(voucher)
        }
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        fun newInstance() = PriceListFragment()
    }

    private fun refreshProductsLocal() {
        val voucher = (activity as AdminActivity).voucher

        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.swiperefresh.isRefreshing = true;
        Products.refresh(voucher!!.tokenAddress)
        productFragment.notifyAboutchange()
        productFragment.swiperefresh.isRefreshing = false
    }
    private fun refreshProducts() {


        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.swiperefresh.isRefreshing = true;
        val voucher = (activity as AdminActivity).voucher

        if (voucher != null) {
            // Load first from local storage
            Products.refresh(voucher!!.tokenAddress)
            productFragment.notifyAboutchange()
            productFragment.swiperefresh.isRefreshing = false
            val token_name = "Lab10"
            PriceListManager.fetchPriceList(token_name)
        }
    }
}
