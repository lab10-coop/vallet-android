package io.lab10.vallet.admin.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import io.lab10.vallet.admin.fragments.ProductFragment
import io.lab10.vallet.admin.models.Products
import kotlinx.android.synthetic.main.activity_price_list.*
import android.app.Activity
import android.util.Log


class PriceListActivity : AppCompatActivity(), ProductFragment.OnListFragmentInteractionListener {
    val TAG = PriceListActivity::class.java.simpleName

    override fun onListFragmentInteraction(item: Products.Product) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_price_list)

        addProductBtn.setOnClickListener() { v ->
            val intent = Intent(this, AddProductActivity::class.java)
            startActivityForResult(intent, AddProductActivity.PRODUCT_RETURN_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_CANCELED){
            if (requestCode == AddProductActivity.PRODUCT_RETURN_CODE && data != null) {
                val product = data.getParcelableExtra<Products.Product>(AddProductActivity.PRODUCT_RETURN_STRING)
                var productFragment = supportFragmentManager.findFragmentById(R.id.product_fragment) as ProductFragment
                if(product.isValid()) {
                    productFragment.addProduct(product)
                }
            }
        }
    }
}
