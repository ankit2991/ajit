package com.messaging.textrasms.manager.feature.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.mms.ContentType
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkRealmAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.extensions.isImage
import com.messaging.textrasms.manager.extensions.isVideo
import com.messaging.textrasms.manager.model.MmsPart
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.gallery_image_page.*
import kotlinx.android.synthetic.main.gallery_image_page.view.*
import javax.inject.Inject


class GalleryPagerAdapter @Inject constructor(private val context: Context) :
    QkRealmAdapter<MmsPart>() {

    companion object {
        private const val VIEW_TYPE_INVALID = 0
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_VIDEO = 2
    }

    val clicks: Subject<View> = PublishSubject.create()

    private val contentResolver = context.contentResolver

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return QkViewHolder(when (viewType) {
            VIEW_TYPE_IMAGE -> inflater.inflate(R.layout.gallery_image_page, parent, false).apply {

                image.attacher.run {
                    javaClass.getDeclaredField("mMinScale").run {
                        isAccessible = true
                        setFloat(image.attacher, 1f)
                    }
                    javaClass.getDeclaredField("mMidScale").run {
                        isAccessible = true
                        setFloat(image.attacher, 1f)
                    }
                    javaClass.getDeclaredField("mMaxScale").run {
                        isAccessible = true
                        setFloat(image.attacher, 3f)
                    }
                }
            }

            VIEW_TYPE_VIDEO -> inflater.inflate(R.layout.gallery_video_page, parent, false)

            else ->
                inflater.inflate(R.layout.gallery_invalid_page, parent, false)

        }.apply { setOnClickListener(clicks::onNext) })
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val part = getItem(position) ?: return
        when (getItemViewType(position)) {
            VIEW_TYPE_IMAGE -> {
                when (part.getUri().let(contentResolver::getType)) {
                    ContentType.IMAGE_GIF -> Glide.with(context)
                        .asGif()
                        .load(part.getUri())
                        .into(holder.image)

                    else -> Glide.with(context)
                        .asBitmap()
                        .load(part.getUri())
                        .into(holder.image)
                }
            }

            VIEW_TYPE_VIDEO -> {
                val fileUri: Uri = Uri.parse(part.getUri().toString())

                val intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setDataAndType(fileUri, "video/*")
                context.startActivity(intent)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val part = getItem(position)
        return when {
            part?.isImage() == true -> VIEW_TYPE_IMAGE
            part?.isVideo() == true -> VIEW_TYPE_VIDEO
            else -> VIEW_TYPE_INVALID
        }
    }

    fun destroy() {
    }

}
