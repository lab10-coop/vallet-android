package io.lab10.vallet.events

import java.math.BigInteger

class TransferVoucherEvent(val from: String, val to: String, val value: BigInteger)
class RedeemVoucherEvent(val value: BigInteger)