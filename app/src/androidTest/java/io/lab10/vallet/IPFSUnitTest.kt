package io.lab10.vallet

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.model.VersionInfo
import io.lab10.vallet.models.Products
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

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
        var hashlink = ipfsServer!!.add.file(File.createTempFile("temptestfile", null))
        Assert.assertEquals(hashlink.toString().length, 73)
    }

    @Test
    fun ipfs_testAddProducts() {
            val id = "Piwo"
            val name = "Piwo"
            val price = 123456
            var image = ipfsServer!!.add.file(File.createTempFile("testimage", null))
            var product = Products.Product(id, name, price, image.Hash)
            Products.addItem(product)
            val appContext = InstrumentationRegistry.getTargetContext()

            var address = IPFSManager.INSTANCE.publishProductList(appContext!!)
            Assert.assertEquals(address!!.length, 46)
    }
}
