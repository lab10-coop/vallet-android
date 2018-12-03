import android.content.Context
import io.ipfs.kotlin.IPFS
import io.lab10.vallet.R
import io.lab10.vallet.models.PriceList
import io.lab10.vallet.models.Products
import io.lab10.vallet.models.Token
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
        // TODO to avoid passing context we could store all configs in DB
      return "http://ipfs.mars.lab10.io:5001/api/v0/"
    }

    fun getIPFSConnection(context: Context): IPFS {
        val okHttpClient = OkHttpClient.Builder()
        // TODO due to slow ipns we have to set high timeout otherwise publishing and resoloving will never work
        okHttpClient.connectTimeout(1000, TimeUnit.SECONDS)
        okHttpClient.readTimeout(1000,TimeUnit.SECONDS)
        val ipfs: IPFS by lazy { IPFS(base_url = getServerAddress(context), okHttpClient = okHttpClient.build() ) }
        return ipfs
    }

    fun publishProductList(context: Context, token: Token): String? {
        var productListFile = File.createTempFile("productList", null)
        productListFile.writeText(PriceList(token.name, token.products, token.tokenType, token.tokenAddress).toJson())
        val address = getIPFSConnection(context).add.file(productListFile)
        // TODO if we would be able to resolve performance issues with ipns we could use ipns here instead
        // For time being we store always actual ipfs address of the file.
        //return getIPFSConnection(context).name.publish(address.Hash)
        return address.Hash
    }

    fun fetchProductList(context: Context, priceListAddress: String, ipns: Boolean) {
        // IF IPNS is provided we are trying to resolve it otherwise we take the direct address
        if (ipns) {
            val priceListIPFSAddress = getIPFSConnection(context).name.resolve(priceListAddress)
            if (priceListIPFSAddress != null) {
                val hash = priceListIPFSAddress.split("/")[2]
                val productListJson = getIPFSConnection(context).get.cat(hash)
                //PriceList.fromJson(productListJson)
            }
        } else {
            val productListJson = getIPFSConnection(context).get.cat(priceListAddress)
            //PriceList.fromJson(productListJson)
        }
    }



}