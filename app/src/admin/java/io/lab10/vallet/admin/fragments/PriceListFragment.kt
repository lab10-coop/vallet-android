package io.lab10.vallet.admin.fragments

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp

import io.lab10.vallet.admin.activities.AddProductActivity
import io.lab10.vallet.events.*
import io.lab10.vallet.fragments.ProductListFragment
import io.lab10.vallet.models.Products
import io.lab10.vallet.models.Token
import kotlinx.android.synthetic.admin.activity_admin.view.*
import kotlinx.android.synthetic.admin.fragment_price_list.view.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus


class PriceListFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private var pendingIntent: PendingIntent? = null
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_price_list, container, false)



        pendingIntent = PendingIntent.getActivity(activity, 0,
                Intent(activity, this.javaClass)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if(nfcAdapter == null){
            Toast.makeText(activity,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
        }else if(!nfcAdapter!!.isEnabled()){
            Toast.makeText(activity,
                    "To use NFC you have to enabled it!",
                    Toast.LENGTH_LONG).show();
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
        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductListFragment
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
        ValletApp.activeToken!!.storage().store()
    }

    @Subscribe
    fun onProductListPublished(event: ProductListPublishedEvent) {
        val token = ValletApp.activeToken
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
        val token = ValletApp.activeToken

        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductListFragment
        productFragment.swiperefresh.isRefreshing = true;
        Products.refresh(token!!)
        productFragment.notifyAboutchange()
        productFragment.swiperefresh.isRefreshing = false
    }
    private fun reloadProducts() {

        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductListFragment
        productFragment.swiperefresh.isRefreshing = true;
        val token = ValletApp.activeToken

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
}
