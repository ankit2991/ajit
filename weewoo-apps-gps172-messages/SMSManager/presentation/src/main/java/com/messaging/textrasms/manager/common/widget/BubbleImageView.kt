package com.messaging.textrasms.manager.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.ImageView
import com.messaging.textrasms.manager.common.util.extensions.dpToPx

class BubbleImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ImageView(context, attrs) {

    enum class Style(
        val topLeft: Boolean,
        val topRight: Boolean,
        val bottomRight: Boolean,
        val bottomLeft: Boolean
    ) {

        ONLY(true, true, true, true),
        IN_FIRST(true, true, true, false),
        IN_MIDDLE(false, true, true, false),
        IN_LAST(false, true, true, true),
        OUT_FIRST(true, true, false, true),
        OUT_MIDDLE(true, false, false, true),
        OUT_LAST(true, false, true, true)
    }

    var bubbleStyle = Style.ONLY
        set(value) {
            field = value
            setPath()
            invalidate()
        }

    private val path = Path()
    private val radiusSmall = 4.dpToPx(context).toFloat()
    private val radiusLarge = 18.dpToPx(context).toFloat()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setPath()
    }

    override fun onDraw(canvas: Canvas) {
        if (!path.isEmpty) {
            canvas.clipPath(path)
        }

        super.onDraw(canvas)
    }

    private fun setPath() {
        path.rewind()

        val width = width.toFloat()
        val height = height.toFloat()

        val cornerRectSmall =
            RectF().apply { set(-radiusSmall, -radiusSmall, radiusSmall, radiusSmall) }
        val cornerRectLarge =
            RectF().apply { set(-radiusLarge, -radiusLarge, radiusLarge, radiusLarge) }

        if (bubbleStyle.topLeft) {
            cornerRectLarge.offsetTo(0f, 0f)
            path.arcTo(cornerRectLarge, 180f, 90f)
        } else {
            cornerRectSmall.offsetTo(0f, 0f)
            path.arcTo(cornerRectSmall, 180f, 90f)
        }

        if (bubbleStyle.topRight) {
            cornerRectLarge.offsetTo(width - radiusLarge * 2, 0f)
            path.arcTo(cornerRectLarge, 270f, 90f)
        } else {
            cornerRectSmall.offsetTo(width - radiusSmall * 2, 0f)
            path.arcTo(cornerRectSmall, 270f, 90f)
        }

        if (bubbleStyle.bottomRight) {
            cornerRectLarge.offsetTo(width - radiusLarge * 2, height - radiusLarge * 2)
            path.arcTo(cornerRectLarge, 0f, 90f)
        } else {
            cornerRectSmall.offsetTo(width - radiusSmall * 2, height - radiusSmall * 2)
            path.arcTo(cornerRectSmall, 0f, 90f)
        }

        if (bubbleStyle.bottomLeft) {
            cornerRectLarge.offsetTo(0f, height - radiusLarge * 2)
            path.arcTo(cornerRectLarge, 90f, 90f)
        } else {
            cornerRectSmall.offsetTo(0f, height - radiusSmall * 2)
            path.arcTo(cornerRectSmall, 90f, 90f)
        }

        path.close()
    }

}