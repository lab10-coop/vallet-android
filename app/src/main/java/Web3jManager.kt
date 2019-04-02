import android.content.Context
import android.util.Log
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import io.lab10.vallet.R
import io.lab10.vallet.Token
import io.lab10.vallet.ValletApp
import io.lab10.vallet.admin.TokenFactory
import io.lab10.vallet.models.Wallet
import io.lab10.vallet.events.*
import io.lab10.vallet.models.Configuration
import io.lab10.vallet.models.Configuration_
import io.lab10.vallet.utils.Base58Util
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
import org.web3j.protocol.core.methods.response.EthLog
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.ChainId
import org.web3j.tx.Contract
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.exceptions.ContractCallException
import org.web3j.tx.response.Callback
import org.web3j.tx.response.EmptyTransactionReceipt
import org.web3j.tx.response.QueuingTransactionReceiptProcessor
import rx.Observable
import rx.Single
import rx.schedulers.Schedulers
import java.io.File
import java.math.BigInteger


/**
 * Created by mtfk on 01.02.18.
 */
class Web3jManager private constructor() {

    init {
    }

    var web3: Web3j? = null
    val TAG = Web3jManager::class.java.simpleName
    val GAS_PRICE = Contract.GAS_PRICE


    private object Holder {
        val INSTANCE = Web3jManager()
    }

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

    fun getConnection(context: Context): Web3j {
        return Web3jFactory.build(HttpService(getNodeAddress(context)))
    }

    fun createWallet(context: Context, password: String): String {
        val appDirectory = context.filesDir

        val fileName = WalletUtils.generateNewWalletFile(
                password, appDirectory, false)
        return fileName
    }

    fun restoreWallet(context: Context, password: String, fileName: String, fileContent: String) : String {
        val appDirectory = context.filesDir

        val destination = File(appDirectory, fileName)
        destination.writeText(fileContent)
        return fileName
    }

    fun loadCredential(context: Context): Credentials? {
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val password = sharedPref.getString(context.resources.getString(R.string.shared_pref_wallet_password), "")

        val walletFile = ValletApp.wallet!!.filePath
        if (walletFile != "") {
            val walletPath = File(context.filesDir, walletFile)
            return WalletUtils.loadCredentials(password, walletPath)
        } else {
            return null
        }

    }

    fun getWalletAddressFromFile(walletFileName: String): String {
        var addr = walletFileName.split("--");
        return "0x" + addr.last().split(".").first()
    }

    fun getBalance(context: Context, address: String): EthGetBalance? {
        try {
            // send asynchronous requests to get balance
            return getConnection(context).ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get()
        } catch (error: Exception) {
            Crashlytics.logException(error)
            EventBus.getDefault().post(ErrorEvent("Error: fetch balance: " + error.message))
            return null
        }

    }

    fun getTokenContractAddress(context: Context) {
        if (ValletApp.wallet != null) {

            val readOnlyTransactionManager = ReadonlyTransactionManager(getConnection(context))
            val tokenFactory = TokenFactory.load(getContractAddress(context), getConnection(context), readOnlyTransactionManager, GAS_PRICE, Contract.GAS_LIMIT)
            // TODO: Optimize the query and do not listen for whole ledger but specific some blocks which are
            // since user joined (did first interaction with blockchain) the network
            // Notice: that this would be triggered just once while user starts first time the app
            // and the active token is not set. When we will support multi tokens this could change
            tokenFactory.tokenCreatedEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io()).onErrorReturn {
                        Crashlytics.logException(it)
                        // If error occur we return empty token create event response and triggering event with error which we got
                        EventBus.getDefault().post(ErrorEvent("getTokenContractAddress: " + it.message))
                        TokenFactory.TokenCreatedEventResponse()
                    }.subscribe() { event ->
                        var log = event as TokenFactory.TokenCreatedEventResponse
                        // if the creator address does not match the token which is active now we just ignore that event
                        if (log._address != null && log._creator.equals(ValletApp.wallet!!.address)) {
                            EventBus.getDefault().post(TokenCreateEvent(log._address as String, log._name as String, log._symbol as String, log._decimals as BigInteger))
                        }
                    }
        } else {
            EventBus.getDefault().post(ErrorEvent("Wallet was not properly created"))
        }
    }

    fun getCirculatingVoucher(context: Context, tokenContractAddress: String) {
        // NOTE: For some reason to get totalSupply of the token it is required to pass credentials, can not be done with readonlytransactionmanager
        // as the web3j will fail. Not fully understand why, if this is a bug in web3j or the readonlytransactionmanager is missing something.
        // TODO find out why ^
        val credentials = loadCredential(context)
        try {
            var token = Token.Companion.load(tokenContractAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
            if (Wallet.isValidAddress(tokenContractAddress)) {
                Single.fromCallable {
                    token.totalSupply().send()
                }.subscribeOn(Schedulers.io())
                .onErrorReturn {
                    Crashlytics.logException(it)

                    // If error occur we return zero value and triggering event with error which we got
                    EventBus.getDefault().post(ErrorEvent("getCirculatingVoucher: " + it.message))
                    BigInteger.ZERO
                }
                .subscribe { result ->
                    EventBus.getDefault().post(TokenTotalSupplyEvent(result.toLong(), tokenContractAddress))
                }
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    fun getTokenName(context: Context, tokenAddress: String) {
        // NOTE: For some reason to get name of the token it is required to pass credentials, can not be done with readonlytransactionmanager
        // as the web3j will fail. Not fully understand why, if this is a bug in web3j or the readonlytransactionmanager is missing something.
        // TODO find out why ^
        val credentials = loadCredential(context)
        try {

            var token = Token.Companion.load(tokenAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                token.name().send()
            }
            .onErrorReturn {
                Crashlytics.logException(it)
                // If error occur we return empty string and triggering event with error which we got
                EventBus.getDefault().post(ErrorEvent("getTokenName: " + it.message))
                ""
            }
            .subscribeOn(Schedulers.io()).subscribe { result ->
                EventBus.getDefault().post(TokenNameEvent(result, tokenAddress))
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    fun getTokenType(context: Context, tokenAddress: String) {
        val credentials = loadCredential(context)
        try {

            var token = Token.Companion.load(tokenAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                token.symbol().send()
            }
            .onErrorReturn {
                Crashlytics.logException(it)
                // If error occur we return empty string and triggering event with error which we got
                EventBus.getDefault().post(ErrorEvent("getTokenType: " + it.message))
                ""
            }
            .subscribeOn(Schedulers.io()).subscribe { result ->
                EventBus.getDefault().post(TokenTypeEvent(result, tokenAddress))
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
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

    fun getClientBalance(context: Context, tokenContractAddress: String, address: String) {
        val credentials = loadCredential(context)
        var error = false
        if (credentials != null) {
            var token = Token.load(tokenContractAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
            Single.fromCallable {
                try {
                    token.balanceOf(address).send()
                } catch (e: ContractCallException) {
                    Crashlytics.logException(e)
                    return@fromCallable BigInteger.ZERO;
                }
            }
            .onErrorReturn {
                Crashlytics.logException(it)
                // If error occur we return zero value and triggering event with error which we got
                EventBus.getDefault().post(ErrorEvent("getClientBalance: " + it.message))
                error = true
                BigInteger.ZERO
            }
            .subscribeOn(Schedulers.io())
            .subscribe() { result ->
                if (!error) {
                    EventBus.getDefault().post(TokenBalanceEvent(result.toLong(), tokenContractAddress))
                }
            }
        }
    }

    // TODO combine that with all circulating and balance as all those methods depend on same events.
    fun fetchAllTransaction(context: Context, tokenAddress: String, walletAddress: String) {
        try {
            val readOnlyTransactionManager = ReadonlyTransactionManager(getConnection(context))
            var token = Token.load(tokenAddress, getConnection(context), readOnlyTransactionManager, GAS_PRICE, Contract.GAS_LIMIT)
            token.transferEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn {
                        Crashlytics.logException(it)
                        // If error occur we return empty token transfer response and triggering event with error which we got
                        EventBus.getDefault().post(ErrorEvent("fetchIssuedTransactions: " + it.message))
                        Token.TransferEventResponse()
                    }
                    .subscribe() { event ->
                        var log = event as Token.TransferEventResponse
                        emitTransactionEvent(log, walletAddress, tokenAddress)

                    }
            token.redeemEventObservable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn {
                        Crashlytics.logException(it)
                        // If error occur we return empty token redeem response and triggering event with error which we got
                        EventBus.getDefault().post(ErrorEvent("fetchRedeemTransactions: " + it.message))
                        Token.RedeemEventResponse()
                    }
                    .subscribe() { event ->
                        var log = event as Token.RedeemEventResponse
                        emitRedeemEvent(log, walletAddress, tokenAddress)
                    }

        } catch (e: Exception) {
            Crashlytics.logException(e)
            if (e.message != null)
                EventBus.getDefault().post(ErrorEvent(e.message.toString()))

        }
    }

    fun generateNewToken(context: Context, name: String, symbol: String, decimal: Int) {
        val contractAddress = getContractAddress(context)
        val credentials = loadCredential(context)
        // TODO take care of the case when credential will be null.
        var tokenFactory = TokenFactory.load(contractAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
        Single.fromCallable {
            tokenFactory.createTokenContract(name, symbol, decimal.toBigInteger()).send()
        }.subscribeOn(Schedulers.io())
        .onErrorReturn {
            Crashlytics.logException(it)
            // If error occur we return empty transaction receipt response and triggering event with error which we got
            EventBus.getDefault().post(ErrorEvent("generateNewToken: " + it.message))
            TransactionReceipt()
        }
        .subscribe() { result ->
            if (result.transactionHash != null) {
                var respons = tokenFactory.getTokenCreatedEvents(result)
                // TODO check if I am the owner and how many events can get back, how to choose proper one?
                EventBus.getDefault().post(TokenCreateEvent(respons.last()._address as String, respons.last()._name as String, respons.last()._symbol as String, respons.last()._decimals as BigInteger))
            }
        }
    }

    fun storePriceList(context: Context, voucherId: Long, tokenContractAddress: String, ipfsAddress: String) {
        val credentials = loadCredential(context)
        // TODO validate if address is valid if not throw exception.
        var token = Token.Companion.load(tokenContractAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
        Single.fromCallable {
            var base32ipfsAddress = Base58Util.decode(ipfsAddress)
            token.setPriceListAddress(base32ipfsAddress.drop(2).toByteArray()).send()
        }.onErrorReturn {
            Crashlytics.logException(it)
            EventBus.getDefault().post(ErrorEvent("storePriceList: " + it.message))
            TransactionReceipt()

        }
        .subscribeOn(Schedulers.io()).subscribe { event ->
            val transaction = event as TransactionReceipt
            if (event.logs != null) {
                val log = event.logs.first()
                EventBus.getDefault().post(MessageEvent("Price list address updated"))
            }
        }
    }

    fun fetchPriceListAddress(context: Context, tokenContractAddress: String) {
        val credentials = loadCredential(context)
        // TODO validate if address is valid if not throw exception.
        var token = Token.Companion.load(tokenContractAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
        Single.fromCallable {
            token.priceListAddress.send()
        }.onErrorReturn {
            Crashlytics.logException(it)
            EventBus.getDefault().post((ErrorEvent("fetchPriceListAddress: " + it.message)))
            "".toByteArray()
        }
        .subscribeOn(Schedulers.io()).subscribe { address ->
            val refiiled = address.toMutableList()
            refiiled.addAll(0, byteArrayOf(18, 32).toList())
            val ipfsAddress = Base58Util.encode(refiiled.toByteArray())
            EventBus.getDefault().post(PriceListAddressEvent(tokenContractAddress, ipfsAddress))
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
        .onErrorReturn {
            Crashlytics.logException(it)
            // If error occur we return empty EthLog and triggering event with error which we got
            EventBus.getDefault().post(ErrorEvent("poolTokenCreate: " + it.message))
            EthLog()
        }
        .subscribe { result ->
            Log.i(TAG, result.toString())
        }
    }

    fun issueTokensTo(context: Context, to: String, amount: BigInteger, tokenContractAddress: String, userName: String) {
        val credentials = loadCredential(context)
        // TODO check if address is valid if not throw exception.

        val transactionReceiptProcessor = QueuingTransactionReceiptProcessor(getConnection(context), object : Callback {
            override fun accept(transactionReceipt: TransactionReceipt?) {
                if (transactionReceipt != null) {
                    transactionReceipt.transactionHash
                }
            }

            override fun exception(exception: java.lang.Exception?) {
                if (exception != null) {
                    exception.message
                }
            }

        }, 10, 1000)


        val transactionManager = RawTransactionManager(
                getConnection(context), credentials, ChainId.NONE, transactionReceiptProcessor)

        var token = Token.Companion.load(tokenContractAddress, getConnection(context), transactionManager, GAS_PRICE, Contract.GAS_LIMIT)
        Observable.fromCallable {
            token.issue(to, amount).send()
        }.onErrorReturn {
            Crashlytics.logException(it)
            // If error occur we return empty transaction receipt response and triggering event with error which we got
            EventBus.getDefault().post(ErrorEvent("issueTokensTo: " + it.message))
            TransactionReceipt()
        }.subscribeOn(Schedulers.io()).subscribe {
            if (it is EmptyTransactionReceipt) {
                EventBus.getDefault().postSticky(PendingTransactionEvent(to, amount.toLong(), "Pending ...", Long.MAX_VALUE, it.transactionHash, tokenContractAddress))
            }
        }
    }

    fun redeemToken(context: Context, amount: BigInteger, tokenContractAddress: String, productName: String) {
        val credentials = loadCredential(context)
        // TODO validate if address is valid if not throw exception.
        var token = Token.Companion.load(tokenContractAddress, getConnection(context), credentials!!, GAS_PRICE, Contract.GAS_LIMIT)
        Single.fromCallable {
            token.redeem(amount).send()
        }.subscribeOn(Schedulers.io())
        .onErrorReturn {
            Crashlytics.logException(it)
            // If error occur we return empty transaction receipt response and triggering event with error which we got
            EventBus.getDefault().post(ErrorEvent("redeemToken: " + it.message))
            TransactionReceipt()
        }
        .subscribe {
            if (it.transactionHash != null && it.blockNumberRaw != null) {
                EventBus.getDefault().postSticky(PendingTransactionEvent(tokenContractAddress, -amount.toLong(), productName, it.getBlockNumber().toLong(), it.transactionHash, tokenContractAddress))
            }
        }
    }

    private fun emitTransactionEvent(log: Token.TransferEventResponse, address: String, tokenAddress: String) {
        if (log._value != null && log._from != null && log._to != null && log._transactionId != null && log._blockNumber != null)
            if (log._to.equals(address) || ValletApp.isAdmin)
                EventBus.getDefault().post(TransferVoucherEvent(address, log._transactionId as String, log._to as String, log._value as BigInteger, log._blockNumber as BigInteger, tokenAddress))
    }

    private fun emitRedeemEvent(log: Token.RedeemEventResponse, address: String, tokenAddress: String) {
        if (log._value != null)
            if (log._from.equals(address) || ValletApp.isAdmin)
                EventBus.getDefault().post(RedeemVoucherEvent(address, log._transactionId as String, log._from as String, log._value as BigInteger, log._blockNumber as BigInteger, tokenAddress))
    }

}