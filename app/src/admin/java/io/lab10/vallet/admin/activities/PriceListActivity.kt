package io.lab10.vallet.admin.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import io.lab10.vallet.admin.fragments.ProductFragment
import io.lab10.vallet.admin.models.Products

class PriceListActivity : AppCompatActivity(), ProductFragment.OnListFragmentInteractionListener {
    val TAG = PriceListActivity::class.java.simpleName

    override fun onListFragmentInteraction(item: Products.Product) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_price_list)
    }
}
