import android.content.Context
import io.lab10.vallet.R
import okhttp3.OkHttpClient
import okhttp3.Request
import io.lab10.vallet.events.DebugEvent
import io.lab10.vallet.events.ErrorEvent
import org.greenrobot.eventbus.EventBus


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

    fun getFounds(context: Context, addr: String) {
        val sharedPref = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)

        // TODO we should use IntentService for all network activities
        // to avoid potential memory leaks. In this case we also should check
        // response and handle case where response will fail and inform user.
        Thread(Runnable {
            try {
                val client = OkHttpClient()

                val request = Request.Builder()
                        .url(getServerAddress(context) + "addr/" + addr)
                        .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {

                    if (sharedPref.getBoolean(context.resources.getString(R.string.shared_pref_debug_mode), false))
                        EventBus.getDefault().post(DebugEvent("Funds granted"))
                }
            } catch (e: Exception){
                if (sharedPref.getBoolean(context.resources.getString(R.string.shared_pref_debug_mode), false))
                    if (e.message != null)
                        EventBus.getDefault().post(ErrorEvent(e.message.toString()))
            }
        }).start()
    }

}