package io.lab10.vallet.admin.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.lab10.vallet.R
import io.lab10.vallet.admin.ProductRecyclerViewAdapter
import io.lab10.vallet.admin.models.Products

import kotlinx.android.synthetic.main.fragment_product_list.view.*

class ProductFragment : Fragment() {
    private var mListener: OnListFragmentInteractionListener? = null
    private var adapter: ProductRecyclerViewAdapter? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
            val view = inflater!!.inflate(R.layout.fragment_product_list, container, false)

            // Set the adapter
            if (view.productList is RecyclerView) {
                val context = view.productList.getContext()
                val recyclerView = view.productList as RecyclerView
                adapter = ProductRecyclerViewAdapter(Products.getProducts(), mListener)
                recyclerView.layoutManager = GridLayoutManager(context, 2)
                recyclerView.adapter = adapter
            }

            return view
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun addProduct(product: Products.Product) {
        Products.addItem(product)
        adapter!!.notifyDataSetChanged()
    }

    fun notifyAboutchange() {
        adapter!!.notifyDataSetChanged()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: Products.Product)
    }

    companion object {
        fun newInstance(): ProductFragment {
            val fragment = ProductFragment()
            return fragment
        }
    }
}
