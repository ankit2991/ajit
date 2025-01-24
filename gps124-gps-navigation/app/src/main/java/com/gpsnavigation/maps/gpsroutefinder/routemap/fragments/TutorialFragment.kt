package com.gpsnavigation.maps.gpsroutefinder.routemap.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.gpsnavigation.maps.gpsroutefinder.routemap.R


private const val LAYOUT_PARAM = "layout_param"

class TutorialFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var layout: Int? = null
    private var callback: OnClickListener? = null

    interface OnClickListener {
        fun onSkipClick()
        fun onPinImageClick(pinResourceId: Int?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as OnClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnSkipClickListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            layout = it.getInt(LAYOUT_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutRes = arguments?.getInt(LAYOUT_PARAM) ?: R.layout.fragment_tutorial_first
        // Inflate the layout for this fragment
        val view = inflater.inflate(layoutRes, container, false)
        initListeners(view)
        return view;
    }

    fun initListeners(view: View) {

        view.findViewById<TextView>(R.id.tvSkip)?.setOnClickListener {
            callback?.onSkipClick()
        }

        view.findViewById<ImageView>(R.id.pin1)?.setOnClickListener {
            callback?.onPinImageClick(R.id.pin1)
        }

        view.findViewById<ImageView>(R.id.pin2)?.setOnClickListener {
            callback?.onPinImageClick(R.id.pin2)
        }

        view.findViewById<ImageView>(R.id.pin3)?.setOnClickListener {
            callback?.onPinImageClick(R.id.pin3)
        }

        view.findViewById<ImageView>(R.id.pin4)?.setOnClickListener {
            callback?.onPinImageClick(R.id.pin4)
        }
    }


    fun getAddressText(): String {
        val editText = view?.findViewById<EditText>(R.id.address)
        return editText?.text.toString()
    }

    fun getTagText(): String {
        val editText = view?.findViewById<EditText>(R.id.tag)
        return editText?.text.toString()
    }

    companion object {
        @JvmStatic
        fun newInstance(@LayoutRes layoutRes: Int) =
            TutorialFragment().apply {
                arguments = Bundle().apply {
                    putInt(LAYOUT_PARAM, layoutRes)
                }
            }
    }
}