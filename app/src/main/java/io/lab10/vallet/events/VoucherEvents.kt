package io.lab10.vallet.events

import java.math.BigDecimal
import java.math.BigInteger

class TransferVoucherEvent(val address: String, val transactionId: String, val to: String, val value: BigInteger, val blockNumber: BigInteger)
class RedeemVoucherEvent(val address: String, val transactionId: String, val to: String, val value: BigInteger, val blockNumber: BigInteger)
class TokenCreateEvent(val address: String, val name: String, val type: String, val decimal: BigInteger)
class TokenNameEvent(val name: String, val address: String)
class TokenTypeEvent(val name: String, val address: String)
class ProductsListEvent
class TokenRedeemEvent(val address: String)