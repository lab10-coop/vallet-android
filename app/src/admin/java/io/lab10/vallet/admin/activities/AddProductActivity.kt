package io.lab10.vallet.admin.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import kotlinx.android.synthetic.admin.content_add_product.*
import android.graphics.Bitmap
import io.lab10.vallet.admin.models.Products



class AddProductActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        productPicture.setOnClickListener() { v ->
            var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

        saveProductBtn.setOnClickListener() { v ->
            var id = productNameInput.text.toString()
            var name = productNameInput.text.toString()
            var priceSring = productPriceInput.text.toString()
            var price = 0
            if (priceSring != null && !priceSring.trim().equals("")) {
                price = Integer.parseInt(priceSring)
            }
            val image = "" // TODO implement image path
            var product = Products.Product(id, name, price, image)

            var resultIntent = Intent();
            resultIntent.putExtra(PRODUCT_RETURN_STRING, product);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data.extras
            val imageBitmap = extras!!.get("data") as Bitmap
            productPicture.setImageBitmap(imageBitmap)
        }
    }

    companion object {
        val PRODUCT_RETURN_CODE = 5;
        val PRODUCT_RETURN_STRING = "product"
    }


}
