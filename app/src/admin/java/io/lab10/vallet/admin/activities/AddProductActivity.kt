package io.lab10.vallet.admin.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import io.lab10.vallet.R
import kotlinx.android.synthetic.admin.content_add_product.*
import android.graphics.Bitmap
import java.io.*
import android.graphics.drawable.BitmapDrawable
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.widget.Toast
import io.lab10.vallet.ValletApp
import io.lab10.vallet.models.Product
import io.lab10.vallet.models.Token
import io.lab10.vallet.models.Wallet
import io.lab10.vallet.utils.EuroInputFilter
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.support.graphics.drawable.VectorDrawableCompat
import io.lab10.vallet.events.ProductAddedEvent
import io.lab10.vallet.events.ProductListPublishedEvent
import io.lab10.vallet.models.Product_
import io.objectbox.Box
import org.greenrobot.eventbus.EventBus

class AddProductActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 101;

    val TAG = AddProductActivity::class.java.name
    private var pendingIntent: PendingIntent? = null
    private var nfcAdapter: NfcAdapter? = null
    private var token: Token? = null
    private var product: Product? = null
    private var tokenBox: Box<Token> ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        if (intent.hasExtra("PRODUCT_ID")) {
            var productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
            var productID = intent.getLongExtra("PRODUCT_ID",0)
            product = productBox.query().equal(Product_.id, productID).build().findFirst()
        }
        tokenBox = ValletApp.getBoxStore().boxFor(Token::class.java)
        // TODO add multi token capabilities
        token = tokenBox!!.query().build().findFirst()

        if (token == null) {
            Toast.makeText(this, "Voucher is not yet created", Toast.LENGTH_LONG).show()
            finish()
        }
        pendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, this.javaClass)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter == null){
            Toast.makeText(this,
                    "NFC NOT supported on this devices!",
                    Toast.LENGTH_LONG).show();
        }else if(!nfcAdapter!!.isEnabled()){
            Toast.makeText(this,
                    "To use NFC you have to enabled it!",
                    Toast.LENGTH_LONG).show();
        }

        productPicture.setOnClickListener() {
            var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

        if (token!!.tokenType == 0) {
            productPriceInput.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            productPriceInput.setFilters(arrayOf<InputFilter>(EuroInputFilter(5, 2)))
        }

        if (product != null) {
            productPriceInput.setText((product as Product).price.toString())
            productNameInput.setText((product as Product).name)
            productNfcTagInput.setText((product as Product).nfcTagId)
            var bmImg = BitmapFactory.decodeFile((product as Product).localImagePath);
            productPicture.setImageBitmap(bmImg);
        }

        saveProductBtn.setOnClickListener() {
            var name = productNameInput.text.toString()
            var priceString = productPriceInput.text.toString()
            var nfcTagId = productNfcTagInput.text.toString()
            var price = 0
            if (!priceString.trim().equals("")) {
                if (token!!.tokenType == 0) {
                    price = Wallet.convertEUR2ATS(priceString)
                } else {
                    price = priceString.toInt()
                }
            }

            if (price > 0 && name.isNotEmpty()) {
                val bitmap = getBitmapFromDrawable(productPicture.getDrawable())

                storeProduct(name, price.toLong(), bitmap, nfcTagId)
                var resultIntent = Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Fill the name nad the price", Toast.LENGTH_SHORT).show()
            }
        }

        closeButton.setOnClickListener() {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val extras = data.extras
                val imageBitmap = extras!!.get("data") as Bitmap
                productPicture.setImageBitmap(imageBitmap)
            }
        }
    }

    companion object {
        val PRODUCT_RETURN_CODE = 5;
    }

    private fun storeProduct(name: String, price: Long, data: Bitmap, nfcTagId: String) {

        val cw = ContextWrapper(applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val image = File(directory, name + ".jpg")
        try {
            val outputStream = FileOutputStream(image);
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
            val address = IPFSManager.INSTANCE.getIPFSConnection(this).add.file(image, name)
            if (product == null) {
                product = Product(0, name, price, address.Hash, image.absolutePath, nfcTagId)
            } else {
                (product as Product).name = name
                (product as Product).price = price
                (product as Product).nfcTagId = nfcTagId
            }
            token!!.products.add(product)
            tokenBox!!.put(token)
            EventBus.getDefault().post(ProductAddedEvent())
            var addressName = IPFSManager.INSTANCE.publishProductList(this)
            if (addressName != null && addressName.isNotBlank())
                EventBus.getDefault().post(ProductListPublishedEvent(token!!.id, addressName))
            Log.d(TAG, "Address of products list: " +  addressName)
        }).start()
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        resolveIntent(intent)
    }

    private fun resolveIntent(intent: Intent) {
        val action = intent.action

        if (NfcAdapter.ACTION_TAG_DISCOVERED == action
                || NfcAdapter.ACTION_TECH_DISCOVERED == action
                || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            val tag = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as Tag
            var result = ""
            for (b in tag.id) {
                val st = String.format("%02X", b)
                result += st
            }
            productNfcTagInput.setText(result)
        }
    }

    override fun onResume() {
        super.onResume()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled())
                Toast.makeText(this,
                        "To use NFC you have to enabled it!",
                        Toast.LENGTH_SHORT).show()

            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null);
        }

    }

    fun getBitmapFromDrawable(drawable: Drawable): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap (drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888);
            val canvas = Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.width, canvas.height);
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw IllegalArgumentException ("unsupported drawable type");
        }
    }

}