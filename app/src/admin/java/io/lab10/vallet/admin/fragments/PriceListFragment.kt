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
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.ProductsListEvent
import io.lab10.vallet.fragments.ProductFragment
import io.lab10.vallet.models.Product
import io.lab10.vallet.models.Products
import kotlinx.android.synthetic.admin.fragment_price_list.view.*
import kotlinx.android.synthetic.main.fragment_product_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.EventBus




class PriceListFragment : Fragment(), ProductFragment.OnListFragmentInteractionListener {
    override fun onListFragmentInteraction(item: Product) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
    };

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        fun newInstance() = PriceListFragment()
    }

    fun refreshProducts() {


        var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
        productFragment.swiperefresh.isRefreshing = true;

        // Load first from local storage
        val productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
        var products = productBox.query().build().find()
        EventBus.getDefault().post(ProductsListEvent())

        if ((activity as AdminActivity).voucher != null) {

            // TODO we should use IntentService for all network activities
            // to avoid potential memory leaks. In this case we also should check
            // response and handle case where response will fail and inform user.
            Thread(Runnable {
                try {
                    val priceListIPNSAddress = (activity as AdminActivity).voucher!!.ipnsAdddress
                    IPFSManager.INSTANCE.fetchProductList(context, priceListIPNSAddress)
                    EventBus.getDefault().post(ProductsListEvent())
                } catch (e: Exception) {
                    if (isAdded) {
                        activity.runOnUiThread {
                            productFragment.swiperefresh.isRefreshing = false
                        }
                        EventBus.getDefault().post(ErrorEvent(e.message.toString()))
                    }
                }
            }).start()
        }
    }
}
