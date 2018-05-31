package io.lab10.vallet

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.web3j.crypto.CipherException
import org.web3j.crypto.ContractUtils
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.Web3jService
import org.web3j.tx.Contract
import java.io.File
import java.math.BigInteger

/**
 * Created by mtfk on 17.02.18.
 */
@RunWith(AndroidJUnit4::class)
class ArtisWalletUnitTest {

    private lateinit var node : Web3j
    private lateinit var appContext: Context
    private val WALLET_ADDRESS_SIZE = 40;
    private lateinit var walletFileName : String
    private val password = "8e38DZDRT0Jy"

    @Rule
    @JvmField
    var exception = ExpectedException.none()

    @Before
    fun setArtisNode() {
        appContext = InstrumentationRegistry.getTargetContext()
        node = Web3jManager.INSTANCE.getConnection(appContext)
        Assert.assertNotNull(node)
        Assert.assertNotNull(appContext)
    }

    @Before
    fun artis_createWallet() {
        walletFileName = Web3jManager.INSTANCE.createWallet(appContext,password)
        Assert.assertNotNull(walletFileName)
    }

    @Test
    fun artis_fetchWalletAddress() {
        val walletAddress = Web3jManager.INSTANCE.getWalletAddress(walletFileName)
        Assert.assertNotNull(walletAddress)
        Assert.assertTrue(WalletUtils.isValidAddress(walletAddress))
        Assert.assertEquals(walletAddress.length, WALLET_ADDRESS_SIZE)
    }

    @Test
    fun artis_loadCredential() {
        val walletPath = File(appContext.filesDir, walletFileName)
        val credential = Web3jManager.INSTANCE.loadCredential(appContext)
        Assert.assertNotNull(credential)
    }

/* TODO since password right now is fetched inside loadCredential we do not have an easy way to test it
   but the password will be removed from there and as soon as this will happen this test could be valid again
   @Test
    fun throwsInvalidPasswordProvided() {
        exception.expect(CipherException::class.java)
        exception.expectMessage("Invalid password provided")
        val wrongPassword = "wrongpassword"
        val walletPath = File(appContext.filesDir, walletFileName)
        Web3jManager.INSTANCE.loadCredential(appContext)
    }*/

    @Test
    fun artis_getWalletBalance() {
        val walletAddress = Web3jManager.INSTANCE.getWalletAddress(walletFileName)
        var balance = Web3jManager.INSTANCE.getBalance(appContext, walletAddress)
        val expected_balance = BigInteger.ZERO
        Assert.assertEquals(balance.balance, expected_balance)
    }
}