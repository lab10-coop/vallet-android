package io.lab10.vallet.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.integration.android.IntentIntegrator
import io.lab10.vallet.R
import kotlinx.android.synthetic.client.fragment_no_store.view.*

class NoStoreFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_no_store, container, false)
        view.add_store_button.setOnClickListener { button ->
            val integrator = IntentIntegrator(activity)
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan()        }
        return view
    }


    companion object {
        fun newInstance(): NoStoreFragment {
            val fragment = NoStoreFragment()
            return fragment
        }
    }

}