package com.android.gpslocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

class SuggestedPlacesAdapter(var placeClickListner: (AddressModel)->Unit) : Adapter<SuggestedPlacesAdapter.ViewHolder>() {

    private var placesList: List<AddressModel>? = null

    fun setData(suggestedPlacesList: List<AddressModel>?) {
        this.placesList = suggestedPlacesList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_suggested_places, parent, false)
        return ViewHolder(layoutView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.placeName.text = placesList!![position].placeName
        holder.placeAddress.text = placesList!![position].Address
        holder.itemView.setOnClickListener {
            placeClickListner(placesList!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (placesList == null) 0 else placesList!!.size
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var placeName: TextView
        internal var placeAddress: TextView

        init {
            placeName = itemView.findViewById(R.id.tvPlaceName)
            placeAddress = itemView.findViewById(R.id.tvPlaceAddress)
        }
    }
}