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
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp

import io.lab10.vallet.admin.activities.AddProductActivity
import io.lab10.vallet.admin.activities.AdminActivity
import io.lab10.vallet.events.*
import io.lab10.vallet.fragments.ProductFragment
import io.lab10.vallet.models.Products
import io.lab10.vallet.models.Token
import kotlinx.android.synthetic.admin.fragment_price_list.view.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus


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

        reloadProducts()

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
                reloadProducts()
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
        refreshProductsLocal()
        storeRemotly()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductsChanged(event: ProductChangedEvent) {
        refreshProductsLocal()
    }

    private fun storeRemotly() {
        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        // TODO support multiple tokens
        val token = tokenBox.query().build().findFirst()
        token!!.storage().store()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefresh(event: ProductRefreshEvent){
        reloadProducts()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onProductRemove(event: ProductRemoveEvent) {
        refreshProductsLocal()
        getActiveToken().storage().store()
    }

    @Subscribe
    fun onProductListPublished(event: ProductListPublishedEvent) {
        val token = getActiveToken()
        if (token != null && token.ipnsAdddress != null && token.ipnsAdddress.isBlank()) {
            token.ipnsAdddress = event.secret
            val voucherBox = ValletApp.getBoxStore().boxFor(Token::class.java)
            voucherBox.put(token)
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
        val token = getActiveToken()

        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.swiperefresh.isRefreshing = true;
        Products.refresh(token!!)
        productFragment.notifyAboutchange()
        productFragment.swiperefresh.isRefreshing = false
    }
    private fun reloadProducts() {

        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.swiperefresh.isRefreshing = true;
        val token = getActiveToken()

        if (token != null) {
            // Load first from local storage
            Products.refresh(token!!)
            productFragment.notifyAboutchange()
            productFragment.swiperefresh.isRefreshing = false
            if (token!!.remoteWriteStoragePresent()) {
                token.storage().fetch()
            } else {
                token.storage().create()
            }
        }
    }

    private fun getActiveToken(): Token {
        val tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        // TODO support multiple tokens
       return tokenBox.query().build().findFirst()!!
    }
}
