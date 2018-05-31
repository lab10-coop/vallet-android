package io.lab10.vallet

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.model.VersionInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by mtfk on 01.02.18.
 */
@RunWith(AndroidJUnit4::class)
class IPFSUnitTest {

    private var ipfsServer : IPFS? = null

    @Before
    fun setIpfsServer() {
        val appContext = InstrumentationRegistry.getTargetContext()
        ipfsServer = IPFSManager.INSTANCE.getIPFSConnection(appContext)
    }

    @Test
    fun ipfs_testIPFSVersion() {
        Assert.assertNotNull(ipfsServer)
        val version = ipfsServer!!.info.version() as VersionInfo

        Assert.assertEquals("0.4.14", version.Version)
        Assert.assertEquals("6", version.Repo)

    }
    @Test
    fun ipfs_testAddFile() {
        Assert.assertNotNull(ipfsServer)

    }
}
