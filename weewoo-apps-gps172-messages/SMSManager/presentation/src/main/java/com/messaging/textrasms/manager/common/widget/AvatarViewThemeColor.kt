package com.messaging.textrasms.manager.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.setBackgroundTint
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.util.GlideApp
import kotlinx.android.synthetic.main.avatar_view.view.*
import javax.inject.Inject

class AvatarViewThemeColor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var navigator: Navigator

    private var lookupKey: String? = null
    private var name: String? = null
    private var photoUri: String? = null
    private var lastUpdated: Long? = null
    private var theme: Colors.Theme

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        theme = colors.theme()

        View.inflate(context, R.layout.avatar_view, this)
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        // Apply theme
        setBackgroundTint(context.resources.getColor(R.color.incoming))
        initial.setTextColor(context.resources.getColor(R.color.textPrimary))
        icon.setTint(context.resources.getColor(R.color.textPrimary))

        if (name?.isNotEmpty() == true) {
            val initials = name
                ?.substringBefore(',')
                ?.split(" ").orEmpty()
                .filter { subname -> subname.isNotEmpty() }
                .map { subname -> subname[0].toString() }

            initial.text =
                if (initials.size > 1) initials.first() + initials.last() else initials.first()
            icon.visibility = GONE
        } else {
            initial.text = null
            icon.visibility = VISIBLE
        }

        photo.setImageDrawable(null)
        photoUri?.let { photoUri ->
            GlideApp.with(photo)
                .load(photoUri)
                .into(photo)
        }
    }
}