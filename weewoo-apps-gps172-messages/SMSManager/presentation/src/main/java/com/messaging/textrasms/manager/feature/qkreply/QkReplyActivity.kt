package com.messaging.textrasms.manager.feature.qkreply

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.extensions.autoScrollToStart
import com.messaging.textrasms.manager.common.util.extensions.setBackgroundTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.compose.MessagesAdapter
import com.messaging.textrasms.manager.util.resolveThemeColor
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.qkreply_activity.*
import javax.inject.Inject

class QkReplyActivity : QkThemedActivity(), QkReplyView {

    @Inject
    lateinit var adapter: MessagesAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val menuItemIntent: Subject<Int> = PublishSubject.create()
    override val textChangedIntent by lazy { message.textChanges() }
    override val changeSimIntent by lazy { sim.clicks() }
    override val sendIntent by lazy { send.clicks() }

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[QkReplyViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        setFinishOnTouchOutside(prefs.qkreplyTapDismiss.get())
        setContentView(R.layout.qkreply_activity)
        window.setBackgroundDrawable(null)
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        viewModel.bindView(this)

        toolbar.clipToOutline = true

        messages.adapter = adapter
        messages.adapter?.autoScrollToStart(messages)
        messages.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = messages.scrollToPosition(adapter.itemCount - 1)
        })

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            toolbar.setBackgroundTint(resolveThemeColor(com.google.android.material.R.attr.colorPrimaryFixed))
            background.setBackgroundTint(resolveThemeColor(android.R.attr.windowBackground))
            messageBackground.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
            composeBackgroundGradient.setBackgroundTint(resolveThemeColor(android.R.attr.windowBackground))
            composeBackgroundSolid.setBackgroundTint(resolveThemeColor(android.R.attr.windowBackground))
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
    }

    override fun render(state: QkReplyState) {
        if (state.hasError) {
            finish()
        }

        threadId.onNext(state.threadId)

        title = state.title

        toolbar.menu.findItem(R.id.expand)?.isVisible = !state.expanded
        toolbar.menu.findItem(R.id.collapse)?.isVisible = state.expanded

        adapter.data = state.data

        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        sim.setVisible(state.subscription != null)
        sim.contentDescription = getString(R.string.compose_sim_cd, state.subscription?.displayName)
        simIndex.text = "${state.subscription?.simSlotIndex?.plus(1)}"

        send.isEnabled = state.canSend
        send.imageAlpha = if (state.canSend) 255 else 128
    }

    override fun setDraft(draft: String) {
        message.setText(draft)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.qkreply, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        menuItemIntent.onNext(item.itemId)
        return true
    }

    override fun getActivityThemeRes(black: Boolean) = when {
        black -> R.style.AppThemeDialog_Black
        else -> R.style.AppThemeDialog
    }

}