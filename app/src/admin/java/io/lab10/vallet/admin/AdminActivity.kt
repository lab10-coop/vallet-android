package io.lab10.vallet.admin

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.lab10.vallet.R
import android.content.IntentFilter
import android.bluetooth.BluetoothAdapter
import android.util.Log
import kotlinx.android.synthetic.admin.activity_admin.*


class AdminActivity : AppCompatActivity() {

    val TAG = "AdminActivity"
    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    Toast.makeText(applicationContext, "Found device " + device.name, Toast.LENGTH_LONG)
                    Log.d(TAG, "Found device")
                    device.fetchUuidsWithSdp()
                }
                BluetoothDevice.ACTION_UUID -> {
                    val uuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID) as Array<Parcelable>
                    for (uuid in uuids) {
                        Log.d(TAG, "Found UUID " + uuid)
                        val address = uuid.toString()
                        Toast.makeText(applicationContext, "Found address: " + address, Toast.LENGTH_LONG).show()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d(TAG, "Started scanning")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> Log.d(TAG, "Scanning is done")
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_UUID)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        registerReceiver(broadCastReceiver, filter)

        val sharedPref = getSharedPreferences("voucher_pref", Context.MODE_PRIVATE)
        val voucherName = sharedPref.getString(resources.getString(R.string.shared_pref_voucher_name), "")
        voucherNameLabel.text = voucherName.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_scan -> startScanningForAddresses()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun startScanningForAddresses() {

        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(applicationContext, "This device does not support BT can't use scanning", Toast.LENGTH_LONG)
            return
        }
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
            // TODO handle turn on off in activity
            // see https://developer.android.com/guide/topics/connectivity/bluetooth.html
        }
        val btStarted = mBluetoothAdapter.startDiscovery()
        if(btStarted) {
            Toast.makeText(this, "Start scanning for addresses nearby", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this,"Something went wrong", Toast.LENGTH_LONG).show()
        }
    }
}
