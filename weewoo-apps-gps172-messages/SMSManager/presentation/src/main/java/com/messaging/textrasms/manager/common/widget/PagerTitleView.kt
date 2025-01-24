package com.messaging.textrasms.manager.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.extensions.forEach
import com.messaging.textrasms.manager.extensions.Optional
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.uber.autodispose.android.ViewScopeProvider
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.tab_view.view.*
import javax.inject.Inject

class PagerTitleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var conversationRepo: ConversationRepository

    private val recipientId: Subject<Long> = BehaviorSubject.create()

    var pager: ViewPager? = null
        set(value) {
            if (field !== value) {
                field = value
                recreate()
            }
        }

    init {
        if (!isInEditMode) appComponent.inject(this)
    }

    fun setRecipientId(id: Long) {
        recipientId.onNext(id)
    }

    private fun recreate() {
        removeAllViews()

        pager?.adapter?.count?.forEach { position ->
            val view = LayoutInflater.from(context).inflate(R.layout.tab_view, this, false)
            view.label.text = pager?.adapter?.getPageTitle(position)
            view.setOnClickListener { pager?.currentItem = position }

            addView(view)
        }

        childCount.forEach { index ->
            getChildAt(index).isActivated = index == pager?.currentItem
        }

        pager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                childCount.forEach { index ->
                    getChildAt(index).isActivated = index == position
                }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val states = arrayOf(
            intArrayOf(android.R.attr.state_activated),
            intArrayOf(-android.R.attr.state_activated)
        )

        recipientId
            .distinctUntilChanged()
            .map { recipientId -> Optional(conversationRepo.getRecipient(recipientId)) }
            .switchMap { recipient -> colors.themeObservable(recipient.value) }
            .map { theme ->
                val textSecondary = context.resolveThemeColor(android.R.attr.textColorSecondary)
                ColorStateList(states, intArrayOf(theme.theme, textSecondary))
            }
            .autoDispose(ViewScopeProvider.from(this))
            .subscribe { colorStateList ->
                childCount.forEach { index ->
                    (getChildAt(index) as? TextView)?.setTextColor(colorStateList)
                }
            }
    }

}
