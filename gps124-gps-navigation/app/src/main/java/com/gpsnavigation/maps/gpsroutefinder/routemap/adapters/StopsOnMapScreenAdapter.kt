package com.gpsnavigation.maps.gpsroutefinder.routemap.view.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PlaceModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.REST
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.STARTED_SHOW_SS_BUTTONS
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.STARTED
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TimelineView
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.shakeView
import com.gpsnavigation.maps.gpsroutefinder.routemap.view.adapters.StopsOnMapScreenAdapter.ViewHolder
import java.text.SimpleDateFormat

 class StopsOnMapScreenAdapter(private val defaultStopStayTime: Int, var onEditStop:(PlaceModel, Int)->Unit, var onStartButtonClick:(PlaceModel, Int)->Unit, var onStopButtonClick:(PlaceModel, Int)->Unit, var onStopClick:(PlaceModel, Int)->Unit) : Adapter<ViewHolder>() {

    var completedStopTime = 0.0
    var wholeStopItemClickedPos = -1
    private var list: ArrayList<PlaceModel>? = null
    private var isHomeExist = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stop_on_map_screen, parent, false)
        return ViewHolder(layoutView, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, list!!.size)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stop = list!![position]
        holder.placeName.text = stop.placeName?: ""
        holder.placeAddress.text = stop.placeAddress?: ""
        holder.tvTime.text = ""
        if (position == 0) {
            completedStopTime = System.currentTimeMillis().toDouble()
            holder.tvTime.text = SimpleDateFormat("hh:mm aa").format(completedStopTime)
            //we put current time bcz its start point of route
        }
        if (stop.stopDuration != null) {
            var stopStayTime = stop.stopStayTime
            if (stopStayTime <= defaultStopStayTime) stopStayTime = defaultStopStayTime
            val stopStayTimeInMilli = stopStayTime * 60 * 1000.toLong()
            val stopDurationInMilli = stop.stopDuration!!.toInt() * 1000.toLong()
            completedStopTime = completedStopTime + (stopStayTimeInMilli + stopDurationInMilli)
            holder.tvTime.text = SimpleDateFormat("hh:mm aa").format(completedStopTime)
        }
        holder.btnPopUpMenu.setOnClickListener { v: View ->
            showPopMenu(
                v,
                position
            )
        }
        if (list!![position].placeType == RFMainActivity.HOME) {
            holder.timeLineView.markerSize = holder.timeLineView.context.resources.getDimensionPixelSize(R.dimen._15sdp)
            holder.timeLineView.marker = ContextCompat.getDrawable(
                holder.timeLineView.context,
                R.drawable.ic_home_stop
            )
        } else if (list!![position].placeType == RFMainActivity.DEST) {
            holder.timeLineView.markerSize = holder.timeLineView.context.resources.getDimensionPixelSize(R.dimen._15sdp)
            holder.timeLineView.marker = ContextCompat.getDrawable(
                holder.timeLineView.context,
                R.drawable.ic_dest_stop
            )
        }
        if (list!![position].placeType == RFMainActivity.HOME || list!![position].placeType == RFMainActivity.DEST) {
            holder.btnPopUpMenu.visibility = View.GONE
            holder.timeLineView.text = ""
            isHomeExist = true
        } else { //check required for  if we directly add stop from map and home and dest not exist in list then n start from 1
            holder.timeLineView.marker = ContextCompat.getDrawable(
                holder.timeLineView.context,
                R.drawable.blue_circle
            )
            if (isHomeExist) holder.timeLineView.text =
                position.toString() + "" else holder.timeLineView.text =
                (position + 1).toString() + ""
            holder.btnPopUpMenu.visibility = View.VISIBLE
            if (list!![position].isASAP) holder.timeLineView.marker =
                holder.timeLineView.context.getDrawable(R.drawable.red_circle) else holder.timeLineView.marker =
                holder.timeLineView.context.getDrawable(R.drawable.blue_circle)
        }
        if (list!![position].stopStatus == STARTED) {
            holder.timeLineView.setStartLineColor(Color.BLUE, getItemViewType(position))
            holder.timeLineView.setEndLineColor(Color.BLUE, getItemViewType(position))
            holder.btnStartDirection.visibility = View.GONE
            holder.btnDoneDirection.visibility = View.GONE
        } else if (list!![position].stopStatus == STARTED_SHOW_SS_BUTTONS) {
            holder.btnStartDirection.visibility = View.VISIBLE
            holder.btnDoneDirection.visibility = View.VISIBLE
            shakeView(holder.btnStartDirection,2000,3f)
            holder.timeLineView.setStartLineColor(Color.GRAY, getItemViewType(position))
            holder.timeLineView.setEndLineColor(Color.GRAY, getItemViewType(position))
            holder.btnStartDirection.text = holder.btnStartDirection.context.getString(R.string.start)
        } else if (list!![position].stopStatus == REST) {
            holder.btnStartDirection.visibility = View.GONE
            holder.btnDoneDirection.visibility = View.GONE
            holder.timeLineView.setStartLineColor(Color.GRAY, getItemViewType(position))
            holder.timeLineView.setEndLineColor(Color.GRAY, getItemViewType(position))
        }
        if (list!![position].isCompletedStop) holder.btnStartDirection.text =
            holder.btnStartDirection.context.getString(R.string.restarted) else holder.btnStartDirection.text =
            holder.btnStartDirection.context.getString(R.string.start)
        holder.btnStartDirection.setOnClickListener { v: View? ->
           onStartButtonClick(
                stop,
                position
            )
        }
        holder.btnDoneDirection.setOnClickListener { v: View? ->
            onStopButtonClick(
                stop,
                position
            )
        }
        holder.itemView.setOnClickListener(OnClickListener { v: View? ->
            onStopClick(
                stop,
                position
            )
        })
    }

    private fun showPopMenu(view: View, pos: Int) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.stop_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_edit_stop -> onEditStop(list!![pos], pos)
            }
            true
        }
        popup.show()
    }

    override fun getItemCount(): Int {
        return if (list == null) 0 else list!!.size
    }


    fun setData(list: ArrayList<PlaceModel>?) {
        this.list = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var placeName: TextView
        var placeAddress: TextView
        var tvTime: TextView
        var btnStartDirection: Button
        var btnDoneDirection: Button
        var btnPopUpMenu: ImageButton
        var timeLineView: TimelineView

        init {
            timeLineView = itemView.findViewById(R.id.timeLineView)
            tvTime = itemView.findViewById(R.id.tvTime)
            btnPopUpMenu = itemView.findViewById(R.id.btnPopUpMenu)
            btnStartDirection = itemView.findViewById(R.id.btnStartDirection)
            btnDoneDirection = itemView.findViewById(R.id.btnDoneDirection)
            placeName = itemView.findViewById(R.id.tvPlaceName)
            placeAddress = itemView.findViewById(R.id.tvPlaceAddress)
            timeLineView.initLine(viewType)
        }
    }

}