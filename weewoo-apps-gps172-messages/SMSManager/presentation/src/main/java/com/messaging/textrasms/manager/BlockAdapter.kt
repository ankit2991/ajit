package com.messaging.textrasms.manager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.databinding.ListRowBinding
import com.messaging.textrasms.manager.model.FilterBlockedNumber
import io.realm.RealmResults

class BlockAdapter(context: Context) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {
    var callLogInfoArrayList: ArrayList<FilterBlockedNumber>
    var context: Context
    var onItemClickListenerblock: OnCallLogItemClickListenerblock? = null
    private val VIEW_TYPE_ITEM = 1
    private val VIEW_TYPE_LOADER = 2
    fun addAllCallLog(getblockdata: RealmResults<FilterBlockedNumber>) {
        callLogInfoArrayList.clear()
        callLogInfoArrayList.addAll(getblockdata)
    }

    interface OnCallLogItemClickListenerblock {
        fun onItemClicked(callLogInfo: FilterBlockedNumber?)
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

    private var isNativeAdLoaded: kotlin.Boolean = false
    fun addAllCallLog(list: List<FilterBlockedNumber>) {
        callLogInfoArrayList = ArrayList()
        callLogInfoArrayList.clear()
        Log.d("addAllCallLog", "addAllCallLog: " + list.size)
        callLogInfoArrayList.addAll(list!!)

    }

    override fun getItemCount(): Int {
        return callLogInfoArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (callLogInfoArrayList[position] == null) VIEW_TYPE_LOADER else VIEW_TYPE_ITEM
    }

    inner class BlockViewHolder(var itemBinding: ListRowBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(callLog: FilterBlockedNumber) {

            if (callLog.address.equals("") || callLog.address == null) {
                itemBinding.textViewName.setText("Blocked Number");
            } else
                itemBinding.textViewName.setText(callLog.address);

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
