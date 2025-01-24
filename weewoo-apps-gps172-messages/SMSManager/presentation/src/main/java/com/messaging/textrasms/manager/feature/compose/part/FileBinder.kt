package com.messaging.textrasms.manager.feature.compose.part

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.extensions.setBackgroundTint
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.feature.compose.BubbleUtils
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.MmsPart
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.util.resolveThemeColor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mms_file_list_item.*
import javax.inject.Inject

class FileBinder @Inject constructor(colors: Colors, private val context: Context) : PartBinder() {


    override val partLayout = R.layout.mms_file_list_item
    override var theme = colors.theme()

    override fun canBindPart(part: MmsPart) = true

    @SuppressLint("CheckResult")
    override fun bindPart(
        holder: QkViewHolder,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        BubbleUtils.getBubble(false, canGroupWithPrevious, canGroupWithNext, message.isMe())
            .let(holder.fileBackground::setBackgroundResource)

        holder.containerView.setOnClickListener { clicks.onNext(part.takeIf { it.isValid }!!.id) }

        Observable.just(part.getUri())
            .map(context.contentResolver::openInputStream)
            .map { inputStream -> inputStream.use { it.available() } }
            .map { bytes ->
                when (bytes) {
                    in 0..999 -> "$bytes B"
                    in 1000..999999 -> "${"%.1f".format(bytes / 1000f)} KB"
                    in 1000000..9999999 -> "${"%.1f".format(bytes / 1000000f)} MB"
                    else -> "${"%.1f".format(bytes / 1000000000f)} GB"
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ size ->
                if (ContextCompat.checkSelfPermission(
                        context,
                        "android.permission.READ_SMS"
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    holder.size.text = size
                }
            }, { throwable -> logDebug("Throw") })


        holder.filename.text = part.name

        val params = holder.fileBackground.layoutParams as FrameLayout.LayoutParams
        if (!message.isMe()) {
            holder.fileBackground.layoutParams = params.apply { gravity = Gravity.START }
            holder.fileBackground.setBackgroundTint(theme.theme)
            holder.icon.setTint(theme.textPrimary)
            holder.filename.setTextColor(theme.textPrimary)
            holder.size.setTextColor(theme.textTertiary)
        } else {
            holder.fileBackground.layoutParams = params.apply { gravity = Gravity.END }
            holder.fileBackground.setBackgroundTint(holder.containerView.context.resolveThemeColor(R.attr.bubbleColor))
            holder.icon.setTint(holder.containerView.context.resolveThemeColor(android.R.attr.textColorSecondary))
            holder.filename.setTextColor(holder.containerView.context.resolveThemeColor(android.R.attr.textColorPrimary))
            holder.size.setTextColor(holder.containerView.context.resolveThemeColor(android.R.attr.textColorTertiary))
        }
    }

}