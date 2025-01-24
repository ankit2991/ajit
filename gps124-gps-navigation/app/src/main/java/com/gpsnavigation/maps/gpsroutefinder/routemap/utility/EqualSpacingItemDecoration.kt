package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.MainScreenAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.PopularPlacesAdapter


class EqualSpacingItemDecoration(private val spacingShort: Int, private val spacingLong: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val viewHolder = parent.getChildViewHolder(view)
        val position = viewHolder.adapterPosition
        val lp = view.layoutParams as GridLayoutManager.LayoutParams
        val columnNumber = lp.spanIndex
        when (viewHolder) {
            is MainScreenAdapter.ItemsViewTwoColumnsHolder -> {
                if (columnNumber == 0) {
                    outRect.left = spacingShort / 2
                    outRect.right = spacingLong / 2
                } else {
                    outRect.left = spacingLong / 2
                    outRect.right = spacingShort / 2
                }
            }

            is MainScreenAdapter.ItemsViewThreeColumnsHolder -> {
                when (columnNumber) {
                    0 -> {
                        outRect.left = spacingShort / 2
                        outRect.right = spacingShort / 2
                        outRect.top = spacingShort
                    }

                    2 -> {
                        outRect.left = spacingShort / 2
                        outRect.right = spacingShort / 2
                        outRect.top = spacingShort
                    }

                    else -> {
                        outRect.left = spacingShort / 2
                        outRect.right = spacingShort / 2
                        outRect.top = spacingShort
                    }
                }
            }

            is PopularPlacesAdapter.ItemsViewThreeColumnsHolder -> {
                when (columnNumber) {
                    0 -> {
                        outRect.left = spacingShort / 2
                        outRect.right = spacingShort / 2
                        outRect.top = spacingShort
                    }

                    2 -> {
                        outRect.left = spacingShort / 2
                        outRect.right = spacingShort / 2
                        outRect.top = spacingShort
                    }

                    else -> {
                        outRect.left = spacingShort / 2
                        outRect.right = spacingShort / 2
                        outRect.top = spacingShort
                    }
                }
            }
            is PopularPlacesAdapter.ItemsViewOneColumnsHolder -> {
                when (columnNumber) {
                    0 -> {
                        outRect.left = spacingShort / 2
                        outRect.right = spacingShort / 2
                    }
                }
            }

            else -> {
                outRect.left = spacingShort / 2
                outRect.right = spacingShort / 2
                outRect.top = spacingShort
            }
        }
    }
}