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
import io.lab10.vallet.admin.events.BTScanningActivityEvent
import io.lab10.vallet.admin.events.NewAddressEvent
import io.lab10.vallet.connectivity.BTUtils
import org.greenrobot.eventbus.EventBus

/**
 * Created by mtfk on 10.01.18.
 */
class BluetoothBroadcastReceiver : BroadcastReceiver() {


    val TAG = BluetoothBroadcastReceiver::class.java.simpleName
    var mDeviceList = ArrayList<BluetoothDevice>()


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    Log.d(TAG, "Found device waiting to finish scannaning ...")
                    mDeviceList.add(device)
                }
                BluetoothDevice.ACTION_UUID -> {
                    val uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID) as? Array<Parcelable>
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    if (uuids != null) {
                        for (uuid in uuids) {
                            Log.d(TAG, "Found UUID " + uuid)
                            val address = uuid.toString()
                            var decodedAddress = BTUtils.decodeAddress(address)
                            if (decodedAddress != null)
                                EventBus.getDefault().post(NewAddressEvent(decodedAddress, device.name));                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Started scanning")
                    EventBus.getDefault().post(BTScanningActivityEvent(true))
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Scanning is done asking for UUIDS")
                    if(!mDeviceList.isEmpty()) {
                        for (device in mDeviceList) {
                            // NOTE: fetching uuids needs to be called after scanning otherwise could not work
                            // in many cases. It is due to the implementation of API.
                            device.fetchUuidsWithSdp()
                        }
                    }

                    EventBus.getDefault().post(BTScanningActivityEvent(false))
                }

            }
        }
    }

}