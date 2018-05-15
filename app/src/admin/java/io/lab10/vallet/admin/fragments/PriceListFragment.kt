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

import io.lab10.vallet.admin.activities.AddProductActivity
import io.lab10.vallet.admin.models.Products
import kotlinx.android.synthetic.admin.fragment_price_list.view.*


class PriceListFragment : Fragment(), ProductFragment.OnListFragmentInteractionListener {
    override fun onListFragmentInteraction(item: Products.Product) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_price_list, container, false)

        view.addProductBtn.setOnClickListener() { v ->
            val intent = Intent(activity, AddProductActivity::class.java)
            startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_CANCELED){
            if (requestCode == AddProductActivity.PRODUCT_RETURN_CODE && data != null) {
                val product = data.getParcelableExtra<Products.Product>(AddProductActivity.PRODUCT_RETURN_STRING)
                var productFragment = childFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
                if(product.isValid()) {
                    productFragment.addProduct(product)
                }
            }
        }
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        fun newInstance() = PriceListFragment()
    }
}
