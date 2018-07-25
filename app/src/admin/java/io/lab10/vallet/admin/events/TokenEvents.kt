package io.lab10.vallet.admin.events

import java.math.BigInteger


class TokenStoredEvent(val address: String)
class CreateTokenEvent(val name: String)
class IssueTokenEvent(val userAddress: String, val amount: String, val userName: String?)