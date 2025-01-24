package com.gps.maps.navigation.routeplanner.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PlaceModel
import java.util.*

class StopsOnEditScreenAdapter(stayTime: Int,var onStopClick:(PlaceModel, Int)->Unit) : RecyclerView.Adapter<StopsOnEditScreenAdapter.ViewHolder>() {

    private var completedStopTime: Double = 0.toDouble()
    private var defaultStopStayTime: Int
    private var list: ArrayList<PlaceModel>? = null

    init {
        this.defaultStopStayTime = stayTime
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_stop_on_edit_screen, parent, false)
        return ViewHolder(
            layoutView,
            viewType
        )
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stop = list!![position]
        holder.placeName.text = stop.placeName?: ""
        holder.placeAddress.text = stop.placeAddress?: ""
        if (list!![position].isASAP)
            holder.ivStop.setImageResource(R.drawable.ic_red_stop_marker)
        else
            holder.ivStop.setImageResource(R.drawable.ic_stop_marker)
        if (stop.stopDuration != null) {
            var stopStayTime = stop.stopStayTime
            if (stopStayTime <= defaultStopStayTime)
                stopStayTime = defaultStopStayTime
            if (position == 0) {
                val stopDurationInMilli = (Integer.parseInt(stop.stopDuration!!) * 1000).toLong()
                completedStopTime = (System.currentTimeMillis() + stopDurationInMilli).toDouble()
                //we put current time bcz its start point of route
                // holder.tvTime.setText(new SimpleDateFormat("hh:mm aa").format(System.currentTimeMillis()));
            } else {
                // holder.tvTime.setText(new SimpleDateFormat("hh:mm aa").format(completedStopTime));
                val stopStayTimeInMilli = (stop.stopStayTime * 60 * 1000).toLong()
                val stopDurationInMilli = (Integer.parseInt(stop.stopDuration!!) * 1000).toLong()
                completedStopTime = completedStopTime + (stopStayTimeInMilli + stopDurationInMilli)
            }
        }
        holder.itemView.setOnClickListener {
            onStopClick(stop, position)
        }

    }



    override fun getItemCount(): Int {
        return if (list == null)
            0
        else
            list!!.size
    }


    fun setData(list: ArrayList<PlaceModel>?) {
        this.list = list
        notifyDataSetChanged()
    }

    public class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var ivStop: ImageView
        var placeName: TextView
        var placeAddress: TextView

        init {
            ivStop = itemView.findViewById(R.id.ivStop)
            placeName = itemView.findViewById(R.id.tvPlaceName)
            placeAddress = itemView.findViewById(R.id.tvPlaceAddress)
        }
    }


}


