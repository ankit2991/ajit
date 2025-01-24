package com.messaging.textrasms.manager.feature.conversationinfo

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.common.util.extensions.dpToPx
import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoItem.ConversationInfoMedia
import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoItem.ConversationInfoRecipient

class GridSpacingItemDecoration(
    private val adapter: ConversationInfoAdapter,
    private val context: Context
) : RecyclerView.ItemDecoration() {

    private val spanCount = 3
    private val spacing = 2.dpToPx(context)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val item = adapter.getItem1(position)

        if (item is ConversationInfoRecipient && adapter.getItem(position + 1) !is ConversationInfoRecipient) {
            outRect.bottom = 8.dpToPx(context)
        } else if (item is ConversationInfoMedia) {
            val firstPartIndex = adapter.data.indexOfFirst { it is ConversationInfoMedia }
            val localPartIndex = position - firstPartIndex

            val column = localPartIndex % spanCount

            outRect.top = spacing
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
        }
    }

}