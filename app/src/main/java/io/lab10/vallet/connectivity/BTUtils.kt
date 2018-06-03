package io.lab10.vallet.connectivity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.widget.Toast
import java.math.BigInteger
import java.util.*
import java.util.regex.Pattern

/**
 * Created by mtfk on 01.01.18.
 */

object BTUtils {

    // It is max amount of characters which we can enconde in one service name
    val SERVICE_NAME_SIZE = 30

    // For some reason UUID is flipped we need to get it straight before extracting UUID
    private fun flipString(string: String) : String {
        var address = string.replace("-","")

        var flipped = ""
        var i = address.length
        while (i > 0) {
            flipped += address.substring(i-2, i)
            i = i - 2
        }
        var paddedData = flipped
        flipped = paddedData.substring(0, 8) + "-" +
                paddedData.substring(8, 12) + "-" +
                paddedData.substring(12, 16) + "-" +
                paddedData.substring(16, 20) + "-" +
                paddedData.substring(20)
        return flipped
    }

    internal fun decodeAddress(uuid: String): String? {
        val uuidR = flipString(uuid)
        val regex = "([a-f0-9]{8})-([a-f0-9]{4})-([a-f0-9]{4})-([a-f0-9]{4})-([a-f0-9]{10})76"
        val pattern = Pattern.compile(regex)
        val m = pattern.matcher(uuidR.toLowerCase())
        if (m.find()) {
            val addr = m.group(1) + m.group(2) + m.group(3) + m.group(4) +
                    m.group(5)
            return addr
        } else {
            return null
        }
    }

    internal fun encodeAddress(data: String): UUID {

        val paddedData = String.format("%-30s", data).replace(' ', '0')

        val uuidString = paddedData.substring(0, 8) + "-" +
                paddedData.substring(8, 12) + "-" +
                paddedData.substring(12, 16) + "-" +
                paddedData.substring(16, 20) + "-" +
                paddedData.substring(20) + "76"

        return UUID.fromString( uuidString )

    }

    fun startScanningForAddresses(activity: Activity) {

        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "This device does not support BT can't use scanning", Toast.LENGTH_SHORT)
            return
        }
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, 1)
            // TODO handle turn on off in activity
            // see https://developer.android.com/guide/topics/connectivity/bluetooth.html
        }
        // TODO find out what will happen if the scanning is ongoing
        val btStarted = mBluetoothAdapter.startDiscovery()
        if(btStarted) {
            Toast.makeText(activity, "Start scanning for addresses nearby", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity,"Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }
}

