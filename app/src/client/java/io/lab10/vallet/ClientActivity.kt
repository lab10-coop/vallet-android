package io.lab10.vallet

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.lab10.vallet.connectivity.BTUtils

import kotlinx.android.synthetic.client.activity_client.*



class ClientActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        productListBtn.setOnClickListener() { v ->
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menuGetVoucher -> startBroadcastingAddress()
        }
        return true
    }

    fun startBroadcastingAddress() {
        // TODO make sure that BT is on if not turn it on
        Toast.makeText(this,"Broadcasting address", Toast.LENGTH_LONG).show()
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // TODO store eth address in preferences
        val address = "Lab10Vallet"
        val uuid = BTUtils.encodeAddress(address)
        val btSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(getString(R.string.app_name), uuid)

    }

}
