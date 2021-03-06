package io.lab10.vallet.admin

import java.math.BigInteger
import java.util.ArrayList
import java.util.Arrays
import org.web3j.abi.EventEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import rx.Observable

/**
 *
 * Auto generated code.
 *
 * **Do not modify!**
 *
 * Please use the [web3j command line tools](https://docs.web3j.io/command_line.html),
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * [codegen module](https://github.com/web3j/web3j/tree/master/codegen) to update.
 *
 *
 * Generated with web3j version 3.2.0.
 */
class TokenFactory : Contract {

    protected constructor(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit) {}

    protected constructor(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit) {}

    fun getTokenCreatedEvents(transactionReceipt: TransactionReceipt): List<TokenCreatedEventResponse> {
        val event = Event("TokenCreated",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint8>() {

                }))
        val valueList = extractEventParameters(event, transactionReceipt)
        val responses = ArrayList<TokenCreatedEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = TokenCreatedEventResponse()
            typedResponse._address = eventValues.nonIndexedValues[0].value as String
            typedResponse._creator = eventValues.nonIndexedValues[1].value as String
            typedResponse._name = eventValues.nonIndexedValues[2].value as String
            typedResponse._symbol = eventValues.nonIndexedValues[3].value as String
            typedResponse._decimals = eventValues.nonIndexedValues[4].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun tokenCreatedEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<TokenCreatedEventResponse> {
        val event = Event("TokenCreated",
                Arrays.asList(),
                Arrays.asList(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Utf8String>() {

                }, object : TypeReference<Uint8>() {

                }))
        val filter = EthFilter(startBlock, endBlock, contractAddress)
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParameters(event, log)
            val typedResponse = TokenCreatedEventResponse()
            typedResponse._address = eventValues.nonIndexedValues[0].value as String
            typedResponse._creator = eventValues.nonIndexedValues[1].value as String
            typedResponse._name = eventValues.nonIndexedValues[2].value as String
            typedResponse._symbol = eventValues.nonIndexedValues[3].value as String
            typedResponse._decimals = eventValues.nonIndexedValues[4].value as BigInteger
            typedResponse
        }
    }

    fun createTokenContract(name: String, symbol: String, decimals: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "createTokenContract",
                Arrays.asList(org.web3j.abi.datatypes.Utf8String(name),
                        org.web3j.abi.datatypes.Utf8String(symbol),
                        org.web3j.abi.datatypes.generated.Uint8(decimals)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    class TokenCreatedEventResponse {
        var _address: String? = null

        var _creator: String? = null

        var _name: String? = null

        var _symbol: String? = null

        var _decimals: BigInteger? = null
    }

    companion object {
        private val BINARY = ""

        fun deploy(web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): RemoteCall<TokenFactory> {
            return Contract.deployRemoteCall(TokenFactory::class.java, web3j, credentials, gasPrice, gasLimit, BINARY, "")
        }

        fun deploy(web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): RemoteCall<TokenFactory> {
            return Contract.deployRemoteCall(TokenFactory::class.java, web3j, transactionManager, gasPrice, gasLimit, BINARY, "")
        }

        fun load(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): TokenFactory {
            return TokenFactory(contractAddress, web3j, credentials, gasPrice, gasLimit)
        }

        fun load(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): TokenFactory {
            return TokenFactory(contractAddress, web3j, transactionManager, gasPrice, gasLimit)
        }
    }
}
