package com.messaging.textrasms.manager.feature.compose.part

import android.content.Context
import com.bumptech.glide.Glide
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.common.widget.BubbleImageView
import com.messaging.textrasms.manager.extensions.isImage
import com.messaging.textrasms.manager.extensions.isVideo
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.MmsPart
import kotlinx.android.synthetic.main.mms_preview_list_item.*
import javax.inject.Inject
import com.bumptech.glide.request.RequestOptions





class MediaBinder @Inject constructor(colors: Colors, private val context: Context) : PartBinder() {


    override val partLayout = R.layout.mms_preview_list_item
    override var theme = colors.theme()

    override fun canBindPart(part: MmsPart) = part.isImage() || part.isVideo()

    override fun bindPart(
        holder: QkViewHolder,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        holder.video.setVisible(part.isVideo())
        holder.containerView.setOnClickListener { clicks.onNext(part.id) }

        holder.thumbnail.bubbleStyle = when {
            !canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_FIRST else BubbleImageView.Style.IN_FIRST
            canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_MIDDLE else BubbleImageView.Style.IN_MIDDLE
            canGroupWithPrevious && !canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_LAST else BubbleImageView.Style.IN_LAST
            else -> BubbleImageView.Style.ONLY
        }

        val options = RequestOptions()
        options.centerCrop()
        Glide.with(context).load(part.getUri()).apply(options).into(holder.thumbnail)
    }


}