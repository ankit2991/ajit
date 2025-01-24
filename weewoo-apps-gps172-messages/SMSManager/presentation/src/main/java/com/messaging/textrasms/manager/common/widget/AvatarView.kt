package com.messaging.textrasms.manager.common.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.setBackgroundTint
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.model.Recipient
import com.messaging.textrasms.manager.util.GlideApp
import kotlinx.android.synthetic.main.avatar_view.view.*
import javax.inject.Inject


class AvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
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
    private var themep: Colors.Themep
    private var recipient1: Recipient? = null

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        theme = colors.theme()
        themep = colors.themeprefix()

        View.inflate(context, R.layout.avatar_view, this)
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    /**
     * Use the [contact] information to display the avatar.
     */

    fun setRecipientgroup(recipient: Recipient?) {
        lookupKey = recipient?.contact?.lookupKey
        name = recipient?.contact?.name
        photoUri = recipient?.contact?.photoUri
        lastUpdated = recipient?.contact?.lastUpdate
        theme = colors.theme(recipient)
        themep = colors.themeprefix(recipient)
        updateView()

    }

    fun setRecipient(recipient: Recipient?) {
        recipient1 = recipient
        lookupKey = recipient?.contact?.lookupKey
        name = recipient?.contact?.name
        photoUri = recipient?.contact?.photoUri
        lastUpdated = recipient?.contact?.lastUpdate
        theme = colors.theme(recipient)
        themep = colors.themeprefix(recipient)
        if (recipient1 != null) {
            updateView()
        } else {
            updateViewinside()
        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    internal var mDebugLog = true
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }


    private fun updateView() {
        // Apply theme

        setBackgroundTint(theme.theme)
        initial.setTextColor(themep.themep)
        icon.setTint(themep.themep)
        if (name?.isNotEmpty() == true) {
            val initials = name
                ?.substringBefore(',')
                ?.split(" ").orEmpty()
                .filter { subname -> subname.isNotEmpty() }
                .map { subname -> subname[0].toString() }
            if (initials.isNotEmpty()) {
                initial.text = initials.first()
            }
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

    private fun updateViewinside() {
        // Apply theme

        setBackgroundTint(theme.theme)
        initial.setTextColor(themep.themep)
        if (recipient1 == null) {


            icon.setTint(themep.textPrimary)


        } else {
            icon.setTint(themep.themep)

        }

        logDebug("checkcolor" + themep.themep)
        logDebug(">>" + theme.theme)
        if (name?.isNotEmpty() == true) {
            val initials = name
                ?.substringBefore(',')
                ?.split(" ").orEmpty()
                .filter { subname -> subname.isNotEmpty() }
                .map { subname -> subname[0].toString() }

            initial.text = initials.first()
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