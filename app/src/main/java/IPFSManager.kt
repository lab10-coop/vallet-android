import android.content.Context
import io.ipfs.kotlin.IPFS
import io.lab10.vallet.R
import io.lab10.vallet.models.Products
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Created by mtfk on 01.02.18.
 */
class IPFSManager private constructor() {
    init {}

    private object Holder { val INSTANCE = IPFSManager() }

    companion object {
        val INSTANCE: IPFSManager by lazy { Holder.INSTANCE }

    }

    private fun getServerAddress(context: Context): String {
      return context.getString(R.string.ipfs_server)
    }

    fun getIPFSConnection(context: Context): IPFS {
        val okHttpClient = OkHttpClient.Builder()
        // TODO due to slow ipns we have to set high timeout otherwise publishing and resoloving will never work
        okHttpClient.connectTimeout(1000, TimeUnit.SECONDS)
        okHttpClient.readTimeout(1000,TimeUnit.SECONDS)
        val ipfs: IPFS by lazy { IPFS(base_url = getServerAddress(context), okHttpClient = okHttpClient.build() ) }
        return ipfs
    }

    fun publishProductList(context: Context): String? {
        var productListFile = File.createTempFile("productList", null)
        productListFile.writeText(Products.toJson())
        val address = getIPFSConnection(context).add.file(productListFile)
        return getIPFSConnection(context).name.publish(address.Hash)
    }

    fun fetchProductList(context: Context, priceListIPNSAddress: String, tokenAddress: String): String {
        if (priceListIPNSAddress.length > 0) {
            val priceListIPFSAddress = getIPFSConnection(context).name.resolve(priceListIPNSAddress)
            if (priceListIPFSAddress != null) {
                val hash = priceListIPFSAddress.split("/")[2]
                val productListJson = getIPFSConnection(context).get.cat(hash)
                Products.fromJson(productListJson, tokenAddress)
            }
        }
        return ""
    }



}