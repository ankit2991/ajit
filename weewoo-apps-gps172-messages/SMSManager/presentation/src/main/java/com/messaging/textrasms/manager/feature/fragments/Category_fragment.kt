package com.messaging.textrasms.manager.feature.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemeFragment
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.compose.GifAdapter
import com.messaging.textrasms.manager.model.Sticker
import com.messaging.textrasms.manager.model.logDebug
import xyz.teamgravity.checkinternet.CheckInternet
import java.io.File


class Category_fragment(
    var array: ArrayList<Sticker>,
    var pos: Int,
    val clickListener: OnItemClickListenergifsend
) : QkThemeFragment(), GifAdapter.OnItemClickListenergif {
    override fun onItemClickedgif(pic: Uri, name: String) {
        clickListener.onItemClickedgifsend(pic)


    }

    private lateinit var progressbar: ProgressBar


    companion object {
        var calledonce = false
        lateinit var progressbarsticker: ProgressBar


        fun newInstance(
            arrylist: ArrayList<Sticker>,
            pos: Int,
            clickListener: OnItemClickListenergifsend
        ): Category_fragment {

            return Category_fragment(arrylist, pos, clickListener)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.catogery_layout, container, false)

        val recyclerView_gif = view.findViewById<RecyclerView>(R.id.recyclerView_gif)

        var layoutManager = GridLayoutManager(activity, 3, LinearLayoutManager.VERTICAL, false)
        progressbar = view.findViewById(R.id.progressbar)
        progressbarsticker = view.findViewById(R.id.progressbarsticker)


        val no_internet = view.findViewById<TextView>(R.id.no_internet)
        if (!isNetworkAvailable()) {
            no_internet.setVisible(true)
        } else {
            no_internet.setVisible(false)
        }


        if (array.size != 0) {
            progressbar.setVisible(false)
        } else {
            progressbar.setVisible(true)

        }
        recyclerView_gif.layoutManager = layoutManager
        var adaptergif = GifAdapter(array.get(pos).getStickerurlArrayList(), this)
        recyclerView_gif.adapter = adaptergif

        LocalBroadcastManager.getInstance(this.requireContext())
            .registerReceiver(receiver, IntentFilter("RECEIVER_FILTER"))


        return view
    }


    interface OnItemClickListenergifsend {
        fun onItemClickedgifsend(pic: Uri)
    }

    var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent!!.action.equals("RECEIVER_FILTER")) {
                logDebug("checkpath" + Uri.fromFile(File(intent.extras!!.getString("key"))))
                if (calledonce) {
                    clickListener.onItemClickedgifsend(Uri.fromFile(File(intent.extras!!.getString("key"))))
                    calledonce = false

                }
            }
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null

//        var result =false
//        CheckInternet().check(){
//                connected ->
//            if (connected){
//                result = true
//            }else{
//                result = false
//            }
//        }
//        return result
    }


    override fun onDestroy() {
        super.onDestroy()
        this.context?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(receiver) }

    }


}