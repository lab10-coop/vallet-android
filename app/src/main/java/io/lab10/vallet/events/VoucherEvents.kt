package io.lab10.vallet.events

import io.lab10.vallet.models.Users
import java.math.BigInteger

class TransferVoucherEvent(val address: String, val transactionId: String, val to: String, val value: BigInteger, val blockNumber: BigInteger, val tokenAddress: String)
class RedeemVoucherEvent(val address: String, val transactionId: String, val to: String, val value: BigInteger, val blockNumber: BigInteger, val tokenAddress: String)
class TokenCreateEvent(val address: String, val name: String, val type: String, val decimal: BigInteger)
class TokenNameEvent(val name: String, val address: String)
class TokenTotalSupplyEvent(val value: Long, val address: String)
class TokenTypeEvent(val name: String, val address: String)
class TokenBalanceEvent(val balance: Long, val address: String)
class ProductsListEvent
class ProductAddedEvent
class RefreshProductsEvent
class RefreshBalanceEvent
class ProductChangedEvent
class NoInternetEvent
class NewTokenEvent
class PriceListAddressEvent(val tokenAddress: String, val ipfsAddress: String)
class AddNewStoreEvent(val tokenAddress: String)
class ProductRemoveEvent
class ProductListPublishedEvent(val voucherId: Long, val ipfsAddress: String = "")
class DeepLinkUserAddEvent(val user: Users.User)

class IssueTokenEvent(val userAddress: String, val amount: String, val userName: String?)
class PendingTransactionEvent(val to: String, val amount: Long, val name: String, val blockNumber: Long, val transactionId: String, val tokenAddress: String)
class RefreshHistoryEvent


class PriceListStartRefreshEvent()
class PriceListEndRefreshEvent()


class StartRefreshingEvent()
class ProductPaidEvent(val price: Long)