import io.ipfs.kotlin.IPFS
import io.lab10.vallet.ValletApp
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.models.*
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
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

    private fun getServerAddress(): String {
      val configurationBox = ValletApp.getBoxStore().boxFor(Configuration::class.java)
      val ipfsAddress = configurationBox.query().equal(Configuration_.name, "ipfsAddress").build().findFirst()
        if (ipfsAddress != null) {
            return ipfsAddress.value
        } else {
            EventBus.getDefault().post(ErrorEvent("Ipfs configuration does not exist restart app or set new ipfs address in settings"))
            return ""
        }
    }

    fun getIPFSConnection(): IPFS {
        val okHttpClient = OkHttpClient.Builder()
        // TODO due to slow ipns we have to set high timeout otherwise publishing and resoloving will never work
        okHttpClient.connectTimeout(1000, TimeUnit.SECONDS)
        okHttpClient.readTimeout(1000,TimeUnit.SECONDS)
        val ipfs: IPFS by lazy { IPFS(base_url = getServerAddress(), okHttpClient = okHttpClient.build() ) }
        return ipfs
    }

    fun publishProductList(token: Token): String? {
        var productListFile = File.createTempFile("productList", null)
        productListFile.writeText(PriceList(token.name, token.products, token.tokenType, token.tokenAddress).toJson())
        val address = getIPFSConnection().add.file(productListFile)
        // TODO if we would be able to resolve performance issues with ipns we could use ipns here instead
        // For time being we store always actual ipfs address of the file.
        //return getIPFSConnection(context).name.publish(address.Hash)
        return address.Hash
    }

    fun fetchProductList(priceListAddress: String, ipns: Boolean) {
        // IF IPNS is provided we are trying to resolve it otherwise we take the direct address
        if (ipns) {
            val priceListIPFSAddress = getIPFSConnection().name.resolve(priceListAddress)
            if (priceListIPFSAddress != null) {
                val hash = priceListIPFSAddress.split("/")[2]
                val productListJson = getIPFSConnection().get.cat(hash)
                PriceList.fromJson(productListJson, priceListAddress)
            }
        } else {
            val productListJson = getIPFSConnection().get.cat(priceListAddress)
            PriceList.fromJson(productListJson, priceListAddress)
        }
    }



}