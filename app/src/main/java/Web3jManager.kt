import android.content.Context
import android.util.Log
import io.lab10.vallet.R
import io.lab10.vallet.Token
import io.lab10.vallet.admin.TokenFactory
import io.lab10.vallet.models.Wallet
import io.lab10.vallet.events.*
import io.lab10.vallet.utils.ReadonlyTransactionManager
import org.greenrobot.eventbus.EventBus
import org.web3j.crypto.Credentials
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.tx.Contract
import org.web3j.tx.exceptions.ContractCallException
import rx.Single
import rx.schedulers.Schedulers
import java.io.File
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

    fun getNodeAddress(context: Context): String {
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        return sharedPref.getString(context.getString(R.string.shared_pref_artis_node_address), context.getString(R.string.artis_node_address))
    }

    fun getContractAddress(context: Context): String {
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        return sharedPref.getString(context.getString(R.string.shared_pref_factory_contract_address), context.getString(R.string.token_factory_contract_address))
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

    fun loadCredential(context: Context): Credentials? {
        // TODO take care of it
        val password = "123"
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val walletFile = sharedPref.getString(context.resources.getString(R.string.shared_pref_voucher_wallet_file), "")
        if (walletFile != "") {
            val walletPath = File(context.filesDir, walletFile)
            return WalletUtils.loadCredentials(password, walletPath)
        } else {
            return null
        }

    }

    fun getWalletAddress(walletFileName: String): String {
        var addr = walletFileName.split("--");
        return "0x" + addr.last().split(".").first()
    }

    fun getBalance(context: Context, address: String) : EthGetBalance {
        // send asynchronous requests to get balance
        return getConnection(context).ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get()

    }

    fun getTokenContractAddress(context: Context) {
        val voucherWalletAddress = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE).getString(context.resources.getString(R.string.shared_pref_voucher_wallet_address), "0x0")

        val readOnlyTransactionManager = ReadonlyTransactionManager(getConnection(context))
        val tokenFactory = TokenFactory.load(getContractAddress(context), getConnection(context), readOnlyTransactionManager, Contract.GAS_PRICE, Contract.GAS_LIMIT)
        tokenFactory.tokenCreatedEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                .subscribeOn(Schedulers.io()).subscribe() { event ->
                    var log = event as TokenFactory.TokenCreatedEventResponse
                    if (log._address != null && log._creator.equals(getWalletAddress(voucherWalletAddress)))
                        EventBus.getDefault().post(TokenCreateEvent(log._address as String))

                }
    }

    fun getCirculatingVoucher(context: Context) {
        val readOnlyTransactionManager = ReadonlyTransactionManager(getConnection(context))
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val tokenContractAddress = sharedPref.getString(context.resources.getString(R.string.shared_pref_token_contract_address), "")
        if (Wallet.isValidAddress(tokenContractAddress)) {
            var token = Token.load(tokenContractAddress, getConnection(context), readOnlyTransactionManager, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            token.transferEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io()).subscribe() { event ->
                        var log = event as Token.TransferEventResponse
                        // TODO add address to support multiple vouchers
                        emitTransactionEvent(log, "")

                    }
            token.redeemEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io()).subscribe() { event ->
                        var log = event as Token.RedeemEventResponse
                        // TODO add address to support multiple vouchers
                        emitRedeemEvent(log, "")
                    }
        }
    }

    fun getVoucherBalance(context: Context, tokenAddress: String) {
        val readOnlyTransactionManager = ReadonlyTransactionManager(getConnection(context))
        if (Wallet.isValidAddress(tokenAddress)) {
            var token = Token.load(tokenAddress, getConnection(context), readOnlyTransactionManager, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            token.transferEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io()).subscribe() { event ->
                        var log = event as Token.TransferEventResponse
                        if (matchClientAddress(context, log._to))
                            emitTransactionEvent(log, tokenAddress)

                    }
            token.redeemEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io()).subscribe() { event ->
                        var log = event as Token.RedeemEventResponse
                        emitRedeemEvent(log, tokenAddress)
                    }
        }
    }

    fun getTokenName(context: Context, tokenAddress: String) {
        // NOTE: For some reason to get name of the token it is required to pass credentials, can not be done with readonlytransactionmanager
        // as the web3j will fail. Not fully understand why, if this is a bug in web3j or the readonlytransactionmanager is missing something.
        // TODO find out why ^
        val credentials = loadCredential(context)
        try {

            var token = Token.Companion.load(tokenAddress,getConnection(context), credentials!!, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                token.name().send()
            }.subscribeOn(Schedulers.io()).subscribe { result ->
                EventBus.getDefault().post(TokenNameEvent(result, tokenAddress))
            }
        } catch (e: Exception) {
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    fun getTokenType(context: Context, tokenAddress: String) {
        val credentials = loadCredential(context)
        try {

            var token = Token.Companion.load(tokenAddress,getConnection(context), credentials!!, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                token.symbol().send()
            }.subscribeOn(Schedulers.io()).subscribe { result ->
                EventBus.getDefault().post(TokenTypeEvent(result, tokenAddress))
            }
        } catch (e: Exception) {
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    private fun matchClientAddress(context: Context, address: String?): Boolean {
        if (address == null)
            return false;
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val walletAddress = sharedPref!!.getString(context.resources.getString(R.string.shared_pref_voucher_wallet_address), "")
        return address.equals(walletAddress)

    }
    fun getClientBalance(context: Context, address: String) : BigInteger {
        val contractAddress = getContractAddress(context)
        val credentials = loadCredential(context)
        var balance = BigInteger.ZERO
        if (credentials != null) {
            var token = Token.load(contractAddress, getConnection(context), credentials!!, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                try {
                    token.balanceOf(address).send()
                } catch (e: ContractCallException) {
                    return@fromCallable BigInteger.ZERO;
                }
            }.subscribeOn(Schedulers.io())
                    .subscribe() { result ->
                        balance = result;
                    }
        }
        return balance;
    }

    // TODO combine that with all circulating and balance as all those methods depend on same events.
    fun fetchAllTransaction(context: Context, tokenAddress: String) {
        try {
            val readOnlyTransactionManager = ReadonlyTransactionManager(getConnection(context))
            var token = Token.load(tokenAddress, getConnection(context), readOnlyTransactionManager, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            token.transferEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io()).subscribe() { event ->
                        var log = event as Token.TransferEventResponse
                        emitTransactionEvent(log, tokenAddress)

                    }
            token.redeemEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io()).subscribe() { event ->
                        var log = event as Token.RedeemEventResponse
                        emitRedeemEvent(log, tokenAddress)
                    }

        } catch (e: Exception) {
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    fun generateNewToken(context: Context, name: String, symbol: String, decimal: Int) {
        val contractAddress = getContractAddress(context)
        val credentials = loadCredential(context)
        // TODO take care of the case when credential will be null.
        var tokenFactory = TokenFactory.load(contractAddress, getConnection(context), credentials!!, Contract.GAS_PRICE,Contract.GAS_LIMIT)
        Single.fromCallable {
             tokenFactory.createTokenContract(name, symbol, decimal.toBigInteger()).send()
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

    fun issueTokensTo(context: Context, to: String, amount: BigInteger) {
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val tokenContractAddress = sharedPref.getString(context.getString(R.string.shared_pref_token_contract_address), "0x0")
        val credentials = loadCredential(context)
        // TODO validate if address is valid if not throw exception.
        try {

            var token = Token.Companion.load(tokenContractAddress,getConnection(context), credentials!!, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                try {
                token.issue(to, amount).send()
                } catch (e: Exception) {
                    if (e.message != null) {
                        EventBus.getDefault().post(ErrorEvent(e.message.toString()))
                    } else {
                        EventBus.getDefault().post(ErrorEvent("Unknown error"))
                    }
                }
            }.subscribeOn(Schedulers.io()).subscribe {
                EventBus.getDefault().post(MessageEvent(context.getString(R.string.message_voucher_issued)))
            }
        } catch (e: Exception) {
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    fun redeemToken(context: Context, amount: BigInteger, tokenContractAddress: String) {
        val credentials = loadCredential(context)
        // TODO validate if address is valid if not throw exception.
        try {

            var token = Token.Companion.load(tokenContractAddress,getConnection(context), credentials!!, Contract.GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                token.redeem(amount).send()
            }.subscribeOn(Schedulers.io()).subscribe {
                EventBus.getDefault().post(TokenRedeemEvent(tokenContractAddress))
            }
        } catch (e: Exception) {
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    private fun emitTransactionEvent(log: Token.TransferEventResponse, address: String) {
        if (log._value != null && log._from != null && log._to != null && log._transactionId != null && log._blockNumber != null)
            EventBus.getDefault().post(TransferVoucherEvent(address, log._transactionId as String, log._to as String, log._value as BigInteger, log._blockNumber as BigInteger))
    }

    private fun emitRedeemEvent(log: Token.RedeemEventResponse, address: String) {
        if (log._value != null)
            EventBus.getDefault().post(RedeemVoucherEvent(address, log._transactionId as String, log._from as String, log._value as BigInteger, log._blockNumber as BigInteger))
    }

}