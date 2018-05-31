import android.content.Context
import io.lab10.vallet.R
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import org.spongycastle.crypto.tls.ConnectionEnd.client
import android.os.AsyncTask.execute



class FaucetManager private constructor() {
    init {}

    private object Holder { val INSTANCE = FaucetManager() }

    companion object {
        val INSTANCE: FaucetManager by lazy { Holder.INSTANCE }

    }

    private fun getServerAddress(context: Context): String {
        return context.getString(R.string.faucet_server)
    }

    fun getFoundsAndGenerateNewToken(context: Context, addr: String) {
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val voucherName = sharedPref.getString(context.resources.getString(R.string.shared_pref_voucher_name), "ATS")
        val voucherDecimal = sharedPref.getInt(context.resources.getString(R.string.shared_pref_voucher_decimal), 12)
        val voucherType = sharedPref.getString(context.resources.getString(R.string.shared_pref_voucher_type), "EUR")

        // TODO we should use IntentService for all network activities
        // to avoid potential memory leaks. In this case we also should check
        // response and handle case where response will fail and inform user.
        Thread(Runnable {
            val client = OkHttpClient()

            val request = Request.Builder()
                    .url(getServerAddress(context) + "addr/" + addr)
                    .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Web3jManager.INSTANCE.generateNewToken(context, voucherName, voucherType, voucherDecimal )
            }
        }).start()
    }

}