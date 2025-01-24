package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import java.util.*

class RoutesListAdapter(var onRouteClick:(RouteModel?)->Unit,var onDuplicateRoute:(RouteModel?)->Unit,var onDeleteRoute:(RouteModel?,Int)->Unit,var onRenameRoute:(RouteModel?,Int)->Unit,var onShareRoute:(RouteModel?,Int)->Unit) : RecyclerView.Adapter<RoutesListAdapter.ViewHolder>() {


    private var list: ArrayList< RouteModel>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_routes_list_on_drawer, parent, false)
        return ViewHolder(layoutView)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvName.text = list!![position].routeName
        holder.itemView.setOnClickListener { onRouteClick(list!![position]) }
        holder.ivOverFlowMenu.setOnClickListener { v -> showPopMenu(v, position) }
    }


    private fun showPopMenu(view: View, pos: Int) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.route_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_duplicate_route -> try {
                    onDuplicateRoute(list!![pos].clone())
                } catch (e: CloneNotSupportedException) {
                    e.printStackTrace()
                }

                R.id.action_rename_route -> onRenameRoute(list!![pos], pos)
                R.id.action_delete_route -> onDeleteRoute(list!![pos], pos)
                R.id.action_share_route -> onShareRoute(list!![pos], pos)
            }
            true
        }
        popup.show()
    }

    override fun getItemCount(): Int {
        return if (list == null)
            0
        else
            list!!.size
    }

    fun setData(list :ArrayList<RouteModel>?) {
        list?.let {
            this.list = list
            notifyDataSetChanged()
        }
    }

     class ViewHolder  constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var tvName: TextView = itemView.findViewById(R.id.tvName)
         var ivOverFlowMenu: ImageView = itemView.findViewById(R.id.ivOverFlowMenu)
     }


}