package io.lab10.vallet.events

import java.math.BigInteger

class TransferVoucherEvent(val address: String, val transactionId: String, val to: String, val value: BigInteger, val blockNumber: BigInteger)
class RedeemVoucherEvent(val address: String, val transactionId: String, val to: String, val value: BigInteger, val blockNumber: BigInteger)
class TokenCreateEvent(val address: String)
class TokenNameEvent(val name: String, val address: String)
class TokenTypeEvent(val name: String, val address: String)