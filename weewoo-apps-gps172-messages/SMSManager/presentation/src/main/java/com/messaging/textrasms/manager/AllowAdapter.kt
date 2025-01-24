package com.messaging.textrasms.manager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.databinding.ListRowBinding
import com.messaging.textrasms.manager.model.AllowNumber

class AllowAdapter(context: Context) : RecyclerView.Adapter<AllowAdapter.BlockViewHolder>() {
    var callLogInfoArrayList: ArrayList<AllowNumber>
    var context: Context
    var onItemClickListenerblock: OnCallLogItemClickListenerblock? = null


    interface OnCallLogItemClickListenerblock {
        fun onItemClicked(callLogInfo: AllowNumber?)
    }

    fun setOnItemClickListener(listener: OnCallLogItemClickListenerblock?) {
        onItemClickListenerblock = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val itemBinding: ListRowBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_row,
            parent,
            false
        )
        return BlockViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        holder.bind(callLogInfoArrayList[position])
    }

    fun addAllCallLog(list: ArrayList<AllowNumber>) {
        callLogInfoArrayList = java.util.ArrayList()
        callLogInfoArrayList.clear()
        Log.d("addAllCallLog", "addAllCallLog: " + list.size)
        callLogInfoArrayList.addAll(list!!)
    }

    override fun getItemCount(): Int {
        return callLogInfoArrayList.size
    }

    inner class BlockViewHolder(var itemBinding: ListRowBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(callLog: AllowNumber) {

            itemBinding.imageViewProfile.setImageResource(R.drawable.ic_check_white_24dp)
            if (callLog.address.equals("") || callLog.address == null) {
                itemBinding.textViewName.setText("Allow Number");
            } else
                itemBinding.textViewName.setText(callLog.address);
            ;
            if (callLog.sender) {
                itemBinding.format.text = "Sender"
            } else {
                itemBinding.format.text = "Content"
            }
            itemBinding.mainLayout.setOnClickListener {
                if (onItemClickListenerblock != null) onItemClickListenerblock!!.onItemClicked(
                    callLog
                )
            }
        }
    }

    init {
        callLogInfoArrayList = ArrayList()
        this.context = context
    }
}
