import android.content.Context
import io.ipfs.kotlin.IPFS
import io.lab10.vallet.R

/**
 * Created by mtfk on 01.02.18.
 */
class IPFSManager private constructor() {
    init {}

    private object Holder { val INSTANCE = IPFSManager() }

    companion object {
        val INSTANCE: IPFSManager by lazy { Holder.INSTANCE }

    }

    private fun getServerAddress(context: Context): String {
      return context.getString(R.string.ipfs_server)
    }

    fun getIPFSConnection(context: Context): IPFS {
        val ipfs: IPFS by lazy { IPFS(base_url = getServerAddress(context) ) }
        return ipfs
    }

}