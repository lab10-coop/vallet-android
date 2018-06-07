package io.lab10.vallet.admin.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import kotlinx.android.synthetic.admin.content_add_product.*
import android.graphics.Bitmap
import io.lab10.vallet.models.Products
import java.io.*
import android.graphics.drawable.BitmapDrawable
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import io.lab10.vallet.admin.models.Wallet
import io.lab10.vallet.utils.EuroInputFilter


class AddProductActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1;

    val TAG = AddProductActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        productPicture.setOnClickListener() { v ->
            var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

        if (Wallet.isEuroType(this)) {
            productPriceInput.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            productPriceInput.setFilters(arrayOf<InputFilter>(EuroInputFilter(5, 2)))
        }

        saveProductBtn.setOnClickListener() { v ->
            var id = productNameInput.text.toString()
            var name = productNameInput.text.toString()
            var priceString = productPriceInput.text.toString()
            var price = 0
            if (priceString != null && !priceString.trim().equals("")) {
                price = Wallet.convertEUR2ATS(priceString)
            }

            val bitmap = (productPicture.getDrawable() as BitmapDrawable).bitmap

            storeProduct(id, name, price, bitmap)
            var resultIntent = Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }

        closeButton.setOnClickListener() { v ->
            finish()
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

    private fun storeProduct(id: String, name: String, price: Int, data: Bitmap) {
        // TODO we should store it locally as well
        val saveImage = File(filesDir, name + ".jpg")
        try {
            val outputStream = FileOutputStream(saveImage);
            data.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (e: FileNotFoundException) {
            // TODO handle errors
            e.printStackTrace()
        } catch ( e: IOException) {
            // TODO handle errors
            e.printStackTrace()
        }

        // TODO we should use IntentService for all network activities
        // to avoid potential memory leaks. In this case we also should check
        // response and handle case where response will fail and inform user.
        Thread(Runnable {
            val address = IPFSManager.INSTANCE.getIPFSConnection(this).add.file(saveImage, name)
            var product = Products.Product(id, name, price, address.Hash)
            Products.addItem(product)
            var addressName = IPFSManager.INSTANCE.publishProductList(this);
            val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(resources.getString(R.string.shared_pref_product_list_ipns_address), addressName)
            editor.commit()
            Log.d(TAG, "Address of products list: " +  addressName)
        }).start()
    }


}
