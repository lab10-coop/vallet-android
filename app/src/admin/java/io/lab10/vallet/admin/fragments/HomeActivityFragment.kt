package io.lab10.vallet.admin.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import io.lab10.vallet.R
import io.lab10.vallet.admin.activities.DebugActivity
import kotlinx.android.synthetic.admin.fragment_home_activity.view.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomeActivityFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomeActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeActivityFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private var debugCount: Int = 0
    private var debugOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var inflater =  inflater.inflate(R.layout.fragment_home_activity, container, false)

        inflater.voucherTypeIcon.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    debugCount += 1
                    if (debugCount > 5) {
                        debugOn = true
                    }
                }
                MotionEvent.ACTION_UP -> {

                    if (debugOn) {
                        val intent = Intent(activity, DebugActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            return@OnTouchListener true
        })
        return inflater
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeActivityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
           HomeActivityFragment()
    }
}
