package com.messaging.textrasms.manager.common.base

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

/**
 * Base RecyclerView.Adapter that provides some convenience when creating a new Adapter, such as
 * data list handing and item animations
 */
abstract class QkAdapter<T> : RecyclerView.Adapter<QkViewHolder>() {

    var data: List<T> = ArrayList()
        set(value) {
            if (field === value) return
//            if (!value.isNullOrEmpty()) {
            val diff = DiffUtil.calculateDiff(getDiffUtilCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
            onDatasetChanged()
            //    }

            emptyView?.setVisible(value.isEmpty())

        }

    /**
     * This view can be set, and the adapter will automatically control the visibility of this view
     * based on the data
     */
    var emptyView: View? = null
        set(value) {
            field = value
            field?.isVisible = data.isEmpty()
        }

    val selectionChanges: Subject<List<Long>> = BehaviorSubject.create()

    private val selection = mutableListOf<Long>()
    private val selectionstring = mutableListOf<String>()
    protected fun setselection(id: String): Boolean {
        when (selectionstring.contains(id)) {
//            true -> selectionstring.remove(id)
            false -> selectionstring.add(id)
            else -> {}
        }

        selectionChanges.onNext(selection)
        return true
    }

    protected fun addselection(id: String): Boolean {
        when (selectionstring.contains(id)) {
            true -> selectionstring.remove(id)
            else -> {}
        }

        selectionChanges.onNext(selection)
        return true

    }

    protected fun isSelectedstring(id: String): Boolean {
        return selectionstring.contains(id)
    }

    /**
     * Toggles the selected state for a particular view
     *
     * If we are currently in selection mode (we have an active selection), then the state will
     * toggle. If we are not in selection mode, then we will only toggle if [force]
     */
    protected fun toggleSelection(id: Long, force: Boolean = true): Boolean {
        if (!force && selection.isEmpty()) return false

        when (selection.contains(id)) {
            true -> selection.remove(id)
            false -> selection.add(id)
        }

        selectionChanges.onNext(selection)
        return true
    }

    protected fun isSelected(id: Long): Boolean {
        return selection.contains(id)
    }

    fun clearSelection() {
        selection.clear()
        selectionChanges.onNext(selection)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return data[position]
    }

    fun getItem1(position: Int): T? {
        if (position > 0 && data.size > position) {
            return data[position]
        }
        return null
    }

    override fun getItemCount(): Int {
        return data.size
    }

    open fun onDatasetChanged() {}

    /**
     * Allows the adapter implementation to provide a custom DiffUtil.Callback
     * If not, then the abstract implementation will be used
     */
    private fun getDiffUtilCallback(oldData: List<T>, newData: List<T>): DiffUtil.Callback {
        return object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areItemsTheSame(oldData[oldItemPosition], newData[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areContentsTheSame(oldData[oldItemPosition], newData[newItemPosition])

            override fun getOldListSize() = oldData.size

            override fun getNewListSize() = newData.size
        }
    }

    protected open fun areItemsTheSame(old: T, new: T): Boolean {
        return old == new
    }

    protected open fun areContentsTheSame(old: T, new: T): Boolean {
        return old == new
    }

}