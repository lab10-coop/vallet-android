import android.content.Context
import android.util.Log
import io.lab10.vallet.R
import io.lab10.vallet.Token
import io.lab10.vallet.admin.TokenFactory
import org.web3j.crypto.Credentials
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.exceptions.ContractCallException
import rx.Single
import rx.schedulers.Schedulers
import java.math.BigInteger


/**
 * Created by mtfk on 01.02.18.
 */
class Web3jManager private constructor(){

    init {}

    var web3: Web3j? = null
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

    fun getClientBalance(context: Context, address: String, credentials: Credentials) : BigInteger {
        val CONTRACT_ADDRESS = context.getString(R.string.token_factory_contract_address)
        var token = Token.load(CONTRACT_ADDRESS, getConnection(context), credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT)
        var balance = BigInteger.ZERO
        Single.fromCallable {
            try {
                token.balanceOf("0x" + address).send()
            }
            catch (e: ContractCallException ) {
                return@fromCallable BigInteger.ZERO;
            }
        }.subscribeOn(Schedulers.io())
                .subscribe() { result ->
                    balance = result;
                }
        return balance;
    }

    fun generateNewToken(context: Context, credentials: Credentials, decimal: Int) {
        val CONTRACT_ADDRESS = context.getString(R.string.token_factory_contract_address)

        var tokenFactory = TokenFactory.load(CONTRACT_ADDRESS, getConnection(context), credentials, Contract.GAS_PRICE,Contract.GAS_LIMIT)
        Single.fromCallable {
             tokenFactory.createToken(decimal.toBigInteger()).send()
        }.subscribeOn(Schedulers.io())
        .subscribe() { result ->
            var respons = tokenFactory.getTokenCreatedEvents(result)
            // TODO check if I am the owner and how many events can get back, how to choose proper one?
            var tokenContractaddress = respons.last()._address
            val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(context.getString(R.string.shared_pref_token_contract_address), tokenContractaddress)
            editor.commit()
        }
    }

    // TODO this probably won't be needed
    fun poolTokenCreateEvent(context: Context) {
        val CONTRACT_ADDRESS = context.getString(R.string.token_factory_contract_address)
        val filter = EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, CONTRACT_ADDRESS)

        //filter.addSingleTopic("TokenCreated")
        Single.fromCallable {
            getConnection(context).ethGetLogs(filter).send()
        }.subscribeOn(Schedulers.io())
        .subscribe {
            result -> Log.i(TAG, result.toString())
        }
    }

    fun issueTokensTo(context: Context, credentials: Credentials, to: String, amount: BigInteger) {
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val tokenContractAddress = sharedPref.getString(context.getString(R.string.shared_pref_token_contract_address), "0x0")

        // TODO validate if address is valid if not throw exception.
        var token = Token.Companion.load(tokenContractAddress,getConnection(context), credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT)
        Single.fromCallable {
            token.issue(to, amount).send()
        }.subscribeOn(Schedulers.io()).subscribe {
            result -> Log.i(TAG, result.toString())
        }
    }

}