import android.content.Context
import io.lab10.vallet.R
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory


/**
 * Created by mtfk on 01.02.18.
 */
class Web3jManager private constructor(){

    init {}

    var web3: Web3j? = null


    private object Holder { val INSTANCE = Web3jManager() }

    companion object {
        val INSTANCE: Web3jManager by lazy { Holder.INSTANCE }
    }

    private fun getNodeAddress(context: Context): String {
        return context.getString(R.string.artis_node_address)
    }

    fun getConnection(context: Context): Web3j{
       return Web3jFactory.build(HttpService(getNodeAddress(context)))
    }
}