package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ItemPopularPlacesAdBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ItemPopularPlacesBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdNativeListener
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PopularPlacesModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx

class PopularPlacesAdapter(
    private val items: List<PopularPlacesModel>,
    private var clickListener: (PopularPlacesModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    inner class ItemsViewOneColumnsHolder(var binding: ItemPopularPlacesAdBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ItemsViewThreeColumnsHolder(var binding: ItemPopularPlacesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ONE_COLUMNS -> {
                val binding = ItemPopularPlacesAdBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemsViewOneColumnsHolder(binding)
            }

            else -> {
                val binding = ItemPopularPlacesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemsViewThreeColumnsHolder(binding)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        when (holder) {
            is PopularPlacesAdapter.ItemsViewOneColumnsHolder -> {
                with(holder.binding.root) {
                    removeAllViews()
                    updatePadding(
                        top = 0
                    )
                    isVisible = false

                    MaxAdManager.createNativeAd(
                        context,
                        this,
                        object : MaxAdNativeListener {

                            override fun onAdLoaded(adLoad: Boolean) {
                                if (adLoad) {
                                    isVisible = true
                                    updatePadding(
                                        top = context.resources.getDimensionPixelSize(R.dimen._10sdp)
                                    )
                                }
                            }
                        },
                        260.dpToPx)
                }
            }

            is PopularPlacesAdapter.ItemsViewThreeColumnsHolder -> {
                val context = holder.binding.root.context
                getItem(position).also { item ->
                    holder.binding.apply {
                        title.text = item.title
                        icon.setImageResource(item.icon)

                        title.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            context.resources.getDimension(R.dimen._8ssp)
                        )
                        title.layoutParams.height = getItemWidth(context, 3) * 4 / 21
                        card.layoutParams.height = getItemWidth(context, 3) * 19 / 21

                        root.setOnClickListener {
                            clickListener(item)
                        }
                    }
                }
            }
        }
    }

    private fun getItemWidth(context: Context, columnsCount: Int) =
        with(context) {
            (resources.displayMetrics.widthPixels - (resources.getDimensionPixelSize(R.dimen._10sdp) * (columnsCount + 1))) / columnsCount
        }

    private fun getItem(position: Int): PopularPlacesModel {
        val idx = position - (position + 1) / (ITEM_BLOCK_COUNT + 1)
        return items[idx]
    }

    override fun getItemCount(): Int {
        return items.size + items.size / ITEM_BLOCK_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % (ITEM_BLOCK_COUNT + 1) != 0) THREE_COLUMNS
        else ONE_COLUMNS
    }

    override fun getItemId(position: Int): Long {
        return if ((position + 1) % (ITEM_BLOCK_COUNT + 1) != 0) {
            val idx = position - (position + 1) / (ITEM_BLOCK_COUNT + 1)
            items[idx].id
        } else {
            position.toLong() + 100L
        }
    }

    companion object {
        private const val ONE_COLUMNS = 0
        private const val THREE_COLUMNS = 1

        const val ITEM_BLOCK_COUNT = 9
    }
}