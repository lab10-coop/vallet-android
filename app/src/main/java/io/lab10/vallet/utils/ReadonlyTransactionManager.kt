package io.lab10.vallet.utils

import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.tx.TransactionManager
import java.io.IOException
import java.math.BigInteger

class ReadonlyTransactionManager : TransactionManager {
    override fun getFromAddress(): String {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return "";
    }

    constructor(web3j: Web3j) : super(web3j) {}

    @Throws(IOException::class)
    override fun sendTransaction(
            gasPrice: BigInteger, gasLimit: BigInteger, to: String, data: String, value: BigInteger): EthSendTransaction {
        throw UnsupportedOperationException(
                "Only read operations are supported by this transaction manager")
    }
}