package io.lab10.vallet

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import java.io.File
import org.junit.rules.ExpectedException
import org.junit.Rule
import org.web3j.crypto.CipherException


/**
 * Created by mtfk on 01.02.18.
 */
@RunWith(AndroidJUnit4::class)
class ArtisUnitTest {

    private lateinit var node : Web3j
    private lateinit var appContext: Context

    @Rule @JvmField
    var exception = ExpectedException.none()

    @Before
    fun setArtisNode() {
        appContext = InstrumentationRegistry.getTargetContext()
        node = Web3jManager.INSTANCE.getConnection(appContext)
        Assert.assertNotNull(node)
        Assert.assertNotNull(appContext)

    }

    @Test
    fun artis_testVersion() {
        Assert.assertNotNull(node)
        val version = node.web3ClientVersion().send() as Web3ClientVersion
        val clientVersion = version.getWeb3ClientVersion()
        Assert.assertEquals(clientVersion, "Parity//v1.8.3-beta-b49c44a-20171114/x86_64-linux-gnu/rustc1.21.0")
    }

    @Test
    fun artis_createWallet() {
        val password = "8e38DZDRT0Jy"
        val walletFileName = Web3jManager.INSTANCE.createWallet(appContext,password)
        Assert.assertNotNull(walletFileName)
    }

    @Test
    fun artis_loadCredential() {
        val password = "8e38DZDRT0Jy"
        val walletFileName = Web3jManager.INSTANCE.createWallet(appContext, password)
        val walletPath = File(appContext.filesDir, walletFileName)
        val credential = Web3jManager.INSTANCE.loadCredential(password, walletPath.absolutePath)
        Assert.assertNotNull(credential)
    }

    @Test
    fun throwsInvalidPasswordProvided() {
        exception.expect(CipherException::class.java)
        exception.expectMessage("Invalid password provided")

        val password = "8e38DZDRT0Jy"
        val wrongPassword = "wrongpassword"
        val walletFileName = Web3jManager.INSTANCE.createWallet(appContext, password)
        val walletPath = File(appContext.filesDir, walletFileName)
        Web3jManager.INSTANCE.loadCredential(wrongPassword, walletPath.absolutePath)
    }
}