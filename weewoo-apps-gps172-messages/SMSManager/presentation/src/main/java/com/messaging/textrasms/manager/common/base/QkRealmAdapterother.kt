package com.messaging.textrasms.manager.common.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsOtherAdapter.Companion.isMultiSelectall
import com.messaging.textrasms.manager.model.Conversation
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.*
import timber.log.Timber

abstract class QkRealmAdapterother<T : RealmModel> :
    RealmRecyclerViewAdapter<T, QkViewHolder>(null, true) {

    /**
     * This view can be set, and the adapter will automatically control the visibility of this view
     * based on the data
     */
    var emptyView: View? = null
        set(value) {
            if (field === value) return

            field = value
            value?.setVisible(data?.isLoaded == true && data?.isEmpty() == true)
        }

    private val emptyListener: (OrderedRealmCollection<T>) -> Unit = { data ->
        emptyView?.setVisible(data.isLoaded && data.isEmpty())
    }

    val selectionChanges: Subject<List<Long>> = BehaviorSubject.create()

    var selection = listOf<Long>()

    /**
     * Toggles the selected state for a particular view
     *
     * If we are currently in selection mode (we have an active selection), then the state will
     * toggle. If we are not in selection mode, then we will only toggle if [force]
     */
    protected fun toggleSelection(id: Long, force: Boolean = true): Boolean {
        if (!force && selection.isEmpty()) return false

        selection = when (selection.contains(id)) {
            true -> selection - id
            false -> selection + id
        }

        selectionChanges.onNext(selection)
        return true
    }

    protected fun addelection(id: Long, force: Boolean = true): Boolean {
        if (!force && selection.isEmpty()) return false

        selection = when (selection.contains(id)) {
            true -> selection - id
            false -> selection + id
        }



        return true
    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

    fun clearSelection() {
        isMultiSelectall = false
        selection = listOf()
        selectionChanges.onNext(selection)
        notifyDataSetChanged()
    }

    override fun getItem(index: Int): T? {
        if (index < 0) {
            Timber.w("Only indexes >= 0 are allowed. Input was: $index")
            return null
        }

        return super.getItem(index)
    }

    override fun updateData(data: OrderedRealmCollection<T>?) {
        if (getData() === data) return
        removeListener(getData())
        addListener(data)
        data?.run(emptyListener)
        super.updateData(data)
    }


    fun selectionAll(conversations: RealmResults<Conversation>) {
        try {
            for (conversion in conversations) {
                if (!isSelected(conversion.id)) {
                    // toggleSelection(conversion.id)
                }
            }
            selectionChanges.onNext(selection)
//            ConversationsOtherAdapter.selectall()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addListener(data)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        removeListener(data)
    }

    private fun addListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.addChangeListener(emptyListener)
            is RealmList<T> -> data.addChangeListener(emptyListener)
        }
    }

    private fun removeListener(data: OrderedRealmCollection<T>?) {
        when (data) {
            is RealmResults<T> -> data.removeChangeListener(emptyListener)
            is RealmList<T> -> data.removeChangeListener(emptyListener)
        }
    }

}