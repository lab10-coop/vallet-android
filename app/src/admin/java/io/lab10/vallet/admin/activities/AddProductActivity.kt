package io.lab10.vallet.admin.activities

import IPFSManager
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Parcelable
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.listeners.IPickResult
import io.lab10.vallet.R
import io.lab10.vallet.ValletApp
import io.lab10.vallet.admin.fragments.NFCTagDialogFragment
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.events.ProductAddedEvent
import io.lab10.vallet.models.*
import io.lab10.vallet.utils.EuroInputFilter
import io.objectbox.Box
import kotlinx.android.synthetic.admin.content_add_product.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class AddProductActivity : AppCompatActivity(), IPickResult, NFCTagDialogFragment.OnFragmentInteractionListener {
    override fun onNFCTagAdded(nfcTag: String) {
        setNFCTag(nfcTag)
    }

    override fun onPickResult(p0: PickResult?) {
        if (p0!!.getError() == null) {
            productPicture.setImageBitmap(p0.bitmap)
        } else {
            EventBus.getDefault().post(ErrorEvent("Pick image: " + p0!!.error.message))
        }
    }

    val REQUEST_IMAGE_CAPTURE = 101;

    val TAG = AddProductActivity::class.java.name
    private var pendingIntent: PendingIntent? = null
    private var nfcAdapter: NfcAdapter? = null
    private var token: Token? = null
    private var product: Product? = null
    private var tokenBox: Box<Token>? = null
    private var productBox: Box<Product>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        productBox = ValletApp.getBoxStore().boxFor(Product::class.java)
        if (intent.hasExtra("PRODUCT_ID")) {
            var productID = intent.getLongExtra("PRODUCT_ID", 0)
            product = productBox!!.query().equal(Product_.id, productID).build().findFirst()
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
        if (nfcAdapter == null || !nfcAdapter!!.isEnabled) {
            nfcLabel.visibility = View.GONE
            nfcAddTagButton.visibility = View.GONE
            nfc_tip.visibility = View.VISIBLE
        }
        productPicture.setOnClickListener() {
            PickImageDialog.build(PickSetup()).show(this);
        }

        nfcRemoveTagButton.setOnClickListener {
            removeNFCTag()
        }

        nfcAddTagButton.setOnClickListener {
            NFCTagDialogFragment.newInstance().show(supportFragmentManager, "")

        }
        if (token!!.tokenType.equals(Tokens.Type.EUR.type)) {
            productPriceInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            productPriceInput.setFilters(arrayOf<InputFilter>(EuroInputFilter(5, 2)))
        }

        if (product != null) {
            if (token!!.tokenType.equals(Tokens.Type.EUR.type)) {
                productPriceInput.setText(Wallet.convertATS2EUR((product as Product).price).toString())
            } else {
                productPriceInput.setText(Wallet.convertEUR2ATS((product as Product).price.toString()).toString())
            }
            saveProductBtn.text = resources.getString(R.string.save_product_button)
            add_product_title.text = "Edit product"
            productNameInput.setText((product as Product).name)
            var nfcTag = (product as Product).nfcTagId
            if(nfcTag != null  && nfcTag.length > 0) {
                setNFCTag(nfcTag)
            } else {
                removeNFCTag()
            }

            nfcTagValue.text = (product as Product).nfcTagId
            Picasso.get().load("https://ipfs.io/ipfs/" + (product as Product).imagePath).into(productPicture)
        }

        saveProductBtn.setOnClickListener() {
            var name = productNameInput.text.toString()
            var priceString = productPriceInput.text.toString()
            var nfcTagId = nfcTagValue.text.toString()
            var price = 0
            if (!priceString.trim().equals("")) {
                if (token!!.tokenType.equals(Tokens.Type.EUR.type)) {
                    price = Wallet.convertEUR2ATS(priceString)
                } else {
                    price = priceString.toInt()
                }
            }

            if (price > 0 && name.isNotEmpty()) {
                if (productPicture.drawable != null) {
                    val bitmap = getBitmapFromDrawable(productPicture.drawable)

                    storeProduct(name, price.toLong(), bitmap, nfcTagId)
                    var resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                }
                finish()
            } else {
                Toast.makeText(this, "Fill the name and the price", Toast.LENGTH_SHORT).show()
            }
        }

        closeButton.setOnClickListener() {
            finish()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    var MY_PERMISSIONS_REQUEST_CAMERA = 101


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Do nothing
                } else {
                    finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        val PRODUCT_RETURN_CODE = 5;
    }

    private fun storeProduct(name: String, price: Long, data: Bitmap, nfcTagId: String) {

        val cw = ContextWrapper(applicationContext)
        // TODO find out if we can push file directly to IPFS without storing it on local disk
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val image = File(directory, name + ".jpg")
        try {
            val outputStream = FileOutputStream(image);
            data.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (e: FileNotFoundException) {
            // TODO handle errors
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO handle errors
            e.printStackTrace()
        }

        if (product == null) {
            product = Product(0, name, price, "", nfcTagId)
        } else {
            (product as Product).name = name
            (product as Product).price = price
            (product as Product).nfcTagId = nfcTagId
        }
        token!!.products.add(product)
        tokenBox!!.put(token)

        // TODO we should use IntentService for all network activities
        // to avoid potential memory leaks. In this case we also should check
        // response and handle case where response will fail and inform user.
        Thread(Runnable {
            val address = IPFSManager.INSTANCE.getIPFSConnection().add.file(image, name)
            if (product != null) {
                product!!.imagePath = address.Hash
                productBox!!.put(product)
            }
            EventBus.getDefault().post(ProductAddedEvent())
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
            setNFCTag(result)
        }
    }

    override fun onResume() {
        super.onResume()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled()) {
                nfc_tip.visibility = View.VISIBLE
            } else {
                nfc_tip.visibility = View.GONE
            }

            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null);
        }

    }

    private fun setNFCTag(tag: String) {
        nfcTagValue.text = tag
        nfcTagValue.visibility = View.VISIBLE
        nfcLabel.text = resources.getString(R.string.add_product_nfc_tag_label)
        nfcAddTagButton.visibility = View.GONE
        nfcRemoveTagButton.visibility = View.VISIBLE
    }

    private fun removeNFCTag() {
        nfcTagValue.text = ""
        nfcTagValue.visibility = View.GONE
        nfcLabel.text = resources.getString(R.string.add_product_nfc_tag_hint)
        nfcRemoveTagButton.visibility = View.GONE
        nfcAddTagButton.visibility = View.VISIBLE
    }

    fun getBitmapFromDrawable(drawable: Drawable): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888);
            val canvas = Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.width, canvas.height);
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw IllegalArgumentException("unsupported drawable type");
        }
    }


}