package io.lab10.vallet.utils

import io.lab10.vallet.events.NoInternetEvent
import org.greenrobot.eventbus.EventBus
import java.net.InetAddress

class NetworkUtils {
    companion object {
        fun isInternetAvailable() {
            doAsync {
                try {
                    var ipAddr = InetAddress.getByName("google.com")
                    if (ipAddr.equals("")) {
                        EventBus.getDefault().post(NoInternetEvent())
                    }
                } catch (error: Exception) {
                    EventBus.getDefault().post(NoInternetEvent())
                }
            }
        }
        fun doAsync(f: () -> Unit) {
            Thread { f() }.start()
        }
    }


}