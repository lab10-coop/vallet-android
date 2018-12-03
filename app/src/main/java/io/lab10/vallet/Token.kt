package io.lab10.vallet

import java.math.BigInteger
import java.util.ArrayList
import java.util.Arrays
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
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
 * Generated with web3j version 3.2.0. and modified by mtfk
 */
class Token : Contract {

    val priceListAddress: RemoteCall<ByteArray>
        get() {
            val function = Function("getPriceListAddress",
                    Arrays.asList(),
                    Arrays.asList<TypeReference<*>>(object : TypeReference<Bytes32>() {

                    }))
            return executeRemoteCallSingleValueReturn(function, ByteArray::class.java)
        }

    protected constructor(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit) {}

    protected constructor(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger) : super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit) {}

    fun getTransferEvents(transactionReceipt: TransactionReceipt): List<TransferEventResponse> {
        val event = Event("Transfer",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val valueList = extractEventParameters(event, transactionReceipt)
        val responses = ArrayList<TransferEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = TransferEventResponse()
            typedResponse._from = eventValues.indexedValues[0].value as String
            typedResponse._to = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun transferEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<TransferEventResponse> {
        val event = Event("Transfer",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val filter = EthFilter(startBlock, endBlock, contractAddress)
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParameters(event, log)
            val typedResponse = TransferEventResponse()
            typedResponse._from = eventValues.indexedValues[0].value as String
            typedResponse._to = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            typedResponse._blockNumber = log.blockNumber
            typedResponse._transactionId = log.transactionHash
            typedResponse
        }
    }

    fun getRedeemEvents(transactionReceipt: TransactionReceipt): List<RedeemEventResponse> {
        val event = Event("Redeem",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val valueList = extractEventParameters(event, transactionReceipt)
        val responses = ArrayList<RedeemEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = RedeemEventResponse()
            typedResponse._from = eventValues.indexedValues[0].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun redeemEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<RedeemEventResponse> {
        val event = Event("Redeem",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val filter = EthFilter(startBlock, endBlock, contractAddress)
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParameters(event, log)
            val typedResponse = RedeemEventResponse()
            typedResponse._from = eventValues.indexedValues[0].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            typedResponse._blockNumber = log.blockNumber
            typedResponse._transactionId = log.transactionHash
            typedResponse
        }
    }

    fun getApprovalEvents(transactionReceipt: TransactionReceipt): List<ApprovalEventResponse> {
        val event = Event("Approval",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val valueList = extractEventParameters(event, transactionReceipt)
        val responses = ArrayList<ApprovalEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = ApprovalEventResponse()
            typedResponse._owner = eventValues.indexedValues[0].value as String
            typedResponse._spender = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            responses.add(typedResponse)
        }
        return responses
    }

    fun approvalEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<ApprovalEventResponse> {
        val event = Event("Approval",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }, object : TypeReference<Address>() {

                }),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        val filter = EthFilter(startBlock, endBlock, contractAddress)
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParameters(event, log)
            val typedResponse = ApprovalEventResponse()
            typedResponse._owner = eventValues.indexedValues[0].value as String
            typedResponse._spender = eventValues.indexedValues[1].value as String
            typedResponse._value = eventValues.nonIndexedValues[0].value as BigInteger
            typedResponse
        }
    }

    fun getPriceListUpdateEvents(transactionReceipt: TransactionReceipt): List<PriceListUpdateEventResponse> {
        val event = Event("PriceListUpdate",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bytes32>() {

                }),
                Arrays.asList())
        val valueList = extractEventParameters(event, transactionReceipt)
        val responses = ArrayList<PriceListUpdateEventResponse>(valueList.size)
        for (eventValues in valueList) {
            val typedResponse = PriceListUpdateEventResponse()
            typedResponse._address = eventValues.indexedValues[0].value as ByteArray
            responses.add(typedResponse)
        }
        return responses
    }

    fun priceListUpdateEventObservable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Observable<PriceListUpdateEventResponse> {
        val event = Event("PriceListUpdate",
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bytes32>() {

                }),
                Arrays.asList())
        val filter = EthFilter(startBlock, endBlock, contractAddress)
        filter.addSingleTopic(EventEncoder.encode(event))
        return web3j.ethLogObservable(filter).map { log ->
            val eventValues = extractEventParameters(event, log)
            val typedResponse = PriceListUpdateEventResponse()
            typedResponse._address = eventValues.indexedValues[0].value as ByteArray
            typedResponse
        }
    }

    fun name(): RemoteCall<String> {
        val function = Function("name",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Utf8String>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun controllerLocked(): RemoteCall<Boolean> {
        val function = Function("controllerLocked",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Bool>() {

                }))
        return executeRemoteCallSingleValueReturn(function, Boolean::class.java)
    }

    fun totalSupply(): RemoteCall<BigInteger> {
        val function = Function("totalSupply",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun decimals(): RemoteCall<BigInteger> {
        val function = Function("decimals",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint8>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun version(): RemoteCall<String> {
        val function = Function("version",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Utf8String>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun symbol(): RemoteCall<String> {
        val function = Function("symbol",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Utf8String>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun controller(): RemoteCall<String> {
        val function = Function("controller",
                Arrays.asList(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Address>() {

                }))
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun setController(_newController: String): RemoteCall<TransactionReceipt> {
        val function = Function(
                "setController",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.Address(_newController)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun lockController(): RemoteCall<TransactionReceipt> {
        val function = Function(
                "lockController",
                Arrays.asList(),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun issue(_receiver: String, _value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "issue",
                Arrays.asList(org.web3j.abi.datatypes.Address(_receiver),
                        org.web3j.abi.datatypes.generated.Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun redeem(_value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "redeem",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.generated.Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun setPriceListAddress(addr: ByteArray): RemoteCall<TransactionReceipt> {
        val function = Function(
                "setPriceListAddress",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.generated.Bytes32(addr)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun transfer(_to: String, _value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "transfer",
                Arrays.asList(org.web3j.abi.datatypes.Address(_to),
                        org.web3j.abi.datatypes.generated.Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun transferFrom(_from: String, _to: String, _value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "transferFrom",
                Arrays.asList(org.web3j.abi.datatypes.Address(_from),
                        org.web3j.abi.datatypes.Address(_to),
                        org.web3j.abi.datatypes.generated.Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun approve(_spender: String, _value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "approve",
                Arrays.asList(org.web3j.abi.datatypes.Address(_spender),
                        org.web3j.abi.datatypes.generated.Uint256(_value)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun approveAndCall(_spender: String, _value: BigInteger, _extraData: ByteArray): RemoteCall<TransactionReceipt> {
        val function = Function(
                "approveAndCall",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.Address(_spender),
                        org.web3j.abi.datatypes.generated.Uint256(_value),
                        org.web3j.abi.datatypes.DynamicBytes(_extraData)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun withdrawTokens(_token: String, _to: String, _amount: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
                "withdrawTokens",
                Arrays.asList(org.web3j.abi.datatypes.Address(_token),
                        org.web3j.abi.datatypes.Address(_to),
                        org.web3j.abi.datatypes.generated.Uint256(_amount)),
                emptyList())
        return executeRemoteCallTransaction(function)
    }

    fun balanceOf(_owner: String): RemoteCall<BigInteger> {
        val function = Function("balanceOf",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.Address(_owner)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun allowance(_owner: String, _spender: String): RemoteCall<BigInteger> {
        val function = Function("allowance",
                Arrays.asList<Type<*>>(org.web3j.abi.datatypes.Address(_owner),
                        org.web3j.abi.datatypes.Address(_spender)),
                Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256>() {

                }))
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    class TransferEventResponse {
        var _from: String? = null
        var _to: String? = null
        var _value: BigInteger? = null
        var _transactionId: String? = null
        var _blockNumber: BigInteger? = null
    }

    class RedeemEventResponse {
        var _from: String? = null

        var _value: BigInteger? = null
        var _transactionId: String? = null
        var _blockNumber: BigInteger? = null
    }

    class ApprovalEventResponse {
        var _owner: String? = null

        var _spender: String? = null

        var _value: BigInteger? = null
    }

    class PriceListUpdateEventResponse {
        var _address: ByteArray? = null
    }

    companion object {
        private val BINARY = ""

        fun deploy(web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger, _controller: String, _name: String, _symbol: String, _decimals: BigInteger): RemoteCall<Token> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(org.web3j.abi.datatypes.Address(_controller),
                    org.web3j.abi.datatypes.Utf8String(_name),
                    org.web3j.abi.datatypes.Utf8String(_symbol),
                    org.web3j.abi.datatypes.generated.Uint8(_decimals)))
            return Contract.deployRemoteCall(Token::class.java, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        fun deploy(web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger, _controller: String, _name: String, _symbol: String, _decimals: BigInteger): RemoteCall<Token> {
            val encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(org.web3j.abi.datatypes.Address(_controller),
                    org.web3j.abi.datatypes.Utf8String(_name),
                    org.web3j.abi.datatypes.Utf8String(_symbol),
                    org.web3j.abi.datatypes.generated.Uint8(_decimals)))
            return Contract.deployRemoteCall(Token::class.java, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor)
        }

        fun load(contractAddress: String, web3j: Web3j, credentials: Credentials, gasPrice: BigInteger, gasLimit: BigInteger): Token {
            return Token(contractAddress, web3j, credentials, gasPrice, gasLimit)
        }

        fun load(contractAddress: String, web3j: Web3j, transactionManager: TransactionManager, gasPrice: BigInteger, gasLimit: BigInteger): Token {
            return Token(contractAddress, web3j, transactionManager, gasPrice, gasLimit)
        }
    }
}
