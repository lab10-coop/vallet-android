package io.lab10.vallet.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.invalidateOptionsMenu
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import io.lab10.vallet.R
import io.lab10.vallet.ProductRecyclerViewAdapter
import io.lab10.vallet.models.Products

import kotlinx.android.synthetic.main.fragment_product_list.view.*
import android.view.*
import io.lab10.vallet.events.RefreshProductsEvent
import io.lab10.vallet.models.Product
import org.greenrobot.eventbus.EventBus
import android.view.MenuInflater
import io.lab10.vallet.events.RefreshBalanceEvent


class ProductListFragment : Fragment() {
    private var mListener: OnListFragmentInteractionListener? = null
    private var adapter: ProductRecyclerViewAdapter? = null
    private var recyclerView: RecyclerView? = null

    private val MY_CAMERA_REQUEST_CODE = 100;


    companion object {
        fun newInstance(): ProductListFragment {
            val fragment = ProductListFragment()
            return fragment
        }    }
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_product_list, container, false)

        setHasOptionsMenu(true)

        // Set the adapter
            if (view.productList is RecyclerView) {
                val context = view.productList.getContext()
                recyclerView = view.productList as RecyclerView
                adapter = ProductRecyclerViewAdapter(Products.getProducts(), mListener)
                recyclerView!!.layoutManager = GridLayoutManager(context, 2)
                recyclerView!!.adapter = adapter
            }

        view.swiperefresh.setOnRefreshListener {
            EventBus.getDefault().post(RefreshProductsEvent())
            EventBus.getDefault().post(RefreshBalanceEvent())
        }

        setupPermissions()
        return view
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_CAMERA_REQUEST_CODE)
        }
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

    fun notifyAboutchange() {
        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (menu.findItem(R.id.menu_delete) != null) {
            if (adapter!!.deleteMode) {
                menu.findItem(R.id.menu_delete).setIcon(R.drawable.ic_done_white_24dp)
            } else {
                menu.findItem(R.id.menu_delete).setIcon(R.drawable.ic_delete_white_24dp)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.menu_delete -> {
                adapter!!.deleteMode = !adapter!!.deleteMode
                adapter!!.notifyDataSetChanged()
                invalidateOptionsMenu(activity)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
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
        fun onProductClickListner(item: Product)
    }

}
