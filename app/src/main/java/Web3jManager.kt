import android.content.Context
import android.util.Log
import io.lab10.vallet.R
import io.lab10.vallet.admin.ObserverAction
import io.lab10.vallet.admin.TokenFactory
import org.web3j.crypto.Credentials
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.tx.Contract
import rx.functions.Action
import java.math.BigInteger
import rx.functions.Action1
import org.web3j.protocol.core.methods.response.EthLog
import rx.Single
import rx.schedulers.Schedulers


/**
 * Created by mtfk on 01.02.18.
 */
class Web3jManager private constructor(){

    init {}

    var web3: Web3j? = null
    val contractAddress = "0x06b40eb4b6eece1dadd723cde8f6b290bcbcddf8"
    val TAG = Web3jManager::class.java.simpleName


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

    fun createWallet(context: Context, password: String): String {
        val appDirectory = context.filesDir

        val fileName = WalletUtils.generateNewWalletFile(
                password, appDirectory, false)
        return fileName
    }

    fun loadCredential(password: String, walletPath: String): Credentials {
        return WalletUtils.loadCredentials(
                password,
                walletPath)
    }

    fun getWalletAddress(walletFileName: String): String {
        var addr = walletFileName.split("--");
        return addr.last().split(".").first()
    }

    fun getBalance(context: Context, address: String) : EthGetBalance {
        // send asynchronous requests to get balance
        return getConnection(context).ethGetBalance("0x" + address, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get()

    }

    fun generateNewToken(context: Context, credentials: Credentials, decimal: Int) {


        var tokenFactory = TokenFactory.load(contractAddress, getConnection(context), credentials, Contract.GAS_PRICE,Contract.GAS_LIMIT)
        tokenFactory.createToken(decimal.toBigInteger())
    }

    fun poolTokenCreateEvent(context: Context) {
        val filter = EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, contractAddress)
        //filter.addSingleTopic("TokenCreated")



        Single.fromCallable {
            val ethLog = getConnection(context).ethGetLogs(filter).send()
            ethLog.logs
        }.subscribeOn(Schedulers.io())
                .subscribe {
                    // What you need to do with your result on the view
                    result -> Log.i(TAG, result.toString())
                }
    }

}