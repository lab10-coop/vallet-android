package io.lab10.vallet.admin.recievers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import io.lab10.vallet.connectivity.BTUtils

/**
 * Created by mtfk on 10.01.18.
 */
class BluetoothBroadcastReveiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    Toast.makeText(context, "Found device " + device.name, Toast.LENGTH_LONG).show()
                    Log.d(TAG, "Found device")
                    device.fetchUuidsWithSdp()
                }
                BluetoothDevice.ACTION_UUID -> {
                    val uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID) as Array<Parcelable>
                    for (uuid in uuids) {
                        Log.d(TAG, "Found UUID " + uuid)
                        val address = uuid.toString()
                        var decodedAddress = BTUtils.decodeAddress(address) as String?
                        if (decodedAddress != null)
                            Toast.makeText(context, "Found address: " + address, Toast.LENGTH_LONG).show()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Started scanning")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> Log.d(TAG, "Scanning is done")
            }
        }

    }
}