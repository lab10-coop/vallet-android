package io.lab10.vallet.events

import java.math.BigInteger

class TransferVoucherEvent(val value: BigInteger)
class RedeemVoucherEvent(val value: BigInteger)