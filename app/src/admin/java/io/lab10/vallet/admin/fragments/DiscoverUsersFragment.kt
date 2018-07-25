package io.lab10.vallet.admin.fragments

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import io.lab10.vallet.R
import io.lab10.vallet.admin.DiscoveryUserRecyclerViewAdapter
import io.lab10.vallet.admin.events.BTScanningActivityEvent
import io.lab10.vallet.admin.events.NewAddressEvent

import io.lab10.vallet.admin.models.BTUsers
import io.lab10.vallet.admin.recievers.BluetoothBroadcastReceiver
import io.lab10.vallet.connectivity.BTUtils
import kotlinx.android.synthetic.admin.fragment_user_list.*
import kotlinx.android.synthetic.admin.fragment_user_list.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import android.content.Intent
import io.lab10.vallet.events.ErrorEvent
import io.lab10.vallet.models.Wallet


/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class DiscoverUsersFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val REQUEST_BT_SCAN = 100
    private var mListener: OnListFragmentInteractionListener? = null
    private var bluetoothReceiver: BluetoothBroadcastReceiver? = BluetoothBroadcastReceiver()
    private var adapter: DiscoveryUserRecyclerViewAdapter? = null
    override fun onRefresh() {
        BTUtils.startScanningForAddresses(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_UUID)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        activity.registerReceiver(bluetoothReceiver, filter)

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: BTScanningActivityEvent) {
       swipe_container.isRefreshing = event.isRunning
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_user_list, container, false)

        view.swipe_container.setOnRefreshListener(this)
        // Set the adapter
        if (view.list is RecyclerView) {
            val context = view.list.getContext()
            val recyclerView = view.list as RecyclerView
            adapter = DiscoveryUserRecyclerViewAdapter(BTUsers.getUsers(), mListener)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
        }

        view.scanQrcodeButton.setOnClickListener { v->
            val integrator = IntentIntegrator.forSupportFragment(this)
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan()
        }

        view.scanBTButton.setOnClickListener() {
            BTUtils.startScanningForAddresses(activity)
        }
        return view
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: NewAddressEvent) {
        var user = BTUsers.User(event.address, event.address, event.deviceName)
        BTUsers.addItem(user)
        adapter!!.notifyDataSetChanged()
        val debugMode = context.getSharedPreferences("voucher_pref", Context.MODE_PRIVATE).getBoolean(resources.getString(R.string.shared_pref_debug_mode), false)
        if (debugMode) {
            // TODO replace by eventbus
            Toast.makeText(context, event.deviceName + " " + event.address, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity.unregisterReceiver(bluetoothReceiver)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: BTUsers.User)
    }

    companion object {

        fun newInstance(): DiscoverUsersFragment {
            val fragment = DiscoverUsersFragment()
            return fragment
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null && result.contents != null) {
                if (result.contents.split(";").size == 2) {
                    val address = result.contents.split(";")[0]
                    val userName = result.contents.split(";")[1]
                    if (Wallet.isValidAddress(address)) {
                        var user = BTUsers.User(address, address, userName)
                        IssueDialogFragment.newInstance(user).show(fragmentManager, userName)
                    } else {
                        EventBus.getDefault().post(ErrorEvent("Invalid wallet address"))
                    }
                } else {
                    EventBus.getDefault().post(ErrorEvent("Invalid QR code"))
                }

            }
        }

        if (requestCode == REQUEST_BT_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                BTUtils.startScanningForAddresses(activity)
            }
        }
    }
}
