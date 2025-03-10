package com.messaging.textrasms.manager.feature.compose.editing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.extensions.forwardTouches
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.extensions.associateByNotNull
import com.messaging.textrasms.manager.model.*
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.util.tryOrNull
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.contact_list_item.*
import kotlinx.android.synthetic.main.contact_list_item.view.*
import javax.inject.Inject

class ComposeItemAdapter @Inject constructor(
    private val colors: Colors,
    private val conversationRepo: ConversationRepository
) : QkAdapter<ComposeItem>() {

    val clicks: Subject<ComposeItem> = PublishSubject.create()
    val longClicks: Subject<ComposeItem> = PublishSubject.create()

    private val numbersViewPool = RecyclerView.RecycledViewPool()
    private val disposables = CompositeDisposable()

    var recipients: Map<String, Recipient> = mapOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var multiselect: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.contact_list_item, parent, false)
        view.icon.setTint(colors.theme().theme)
        view.numbers.setRecycledViewPool(numbersViewPool)
        view.numbers.adapter = PhoneNumberAdapter()
        view.numbers.forwardTouches(view)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                if (adapterPosition != -1) {

                    val item1 = getItem(adapterPosition)
                    clicks.onNext(item1)
                    data(item1)
                    multiselect = true

                }
            }
            view.setOnLongClickListener {
                if (adapterPosition != -1) {
                    val item = getItem(adapterPosition)
                    data(item)
                    longClicks.onNext(item)
                }
                true

            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val prevItem = if (position > 0) getItem(position - 1) else null
        val item = getItem(position)
        logDebug("datamessage" + item)
        when (item) {

            is ComposeItem.New -> bindNew(holder, item.value)
            is ComposeItem.Recent -> bindRecent(holder, item.value, prevItem)
            is ComposeItem.Starred -> bindStarred(holder, item.value, prevItem)
            is ComposeItem.Person -> bindPerson(holder, item.value, prevItem)
            is ComposeItem.Group -> bindGroup(holder, item.value, prevItem)
        }
    }

    fun data(contact: ComposeItem) {
        setselection(((ComposeItem.Person(contact.getContacts().get(0)).value.lookupKey)))
        notifyDataSetChanged()
    }

    fun removeitem(lookupKey: String) {
        addselection(lookupKey)
        notifyDataSetChanged()
    }

    private fun bindNew(holder: QkViewHolder, contact: Contact) {
        holder.index.isVisible = false

        holder.icon.isVisible = false

        holder.avatar.recipients = listOf(createRecipient(contact))

        holder.title.text = contact.numbers.joinToString { it.address }

        holder.subtitle.isVisible = false

        holder.numbers.isVisible = false
    }

    private fun bindRecent(holder: QkViewHolder, conversation: Conversation, prev: ComposeItem?) {
        holder.index.isVisible = false

        holder.icon.isVisible = prev !is ComposeItem.Recent
        holder.icon.setImageResource(R.drawable.ic_history_black_24dp)

        holder.avatar.recipients = conversation.recipients

        holder.title.text = conversation.getTitle()

        holder.subtitle.isVisible = conversation.recipients.size > 1 && conversation.name.isBlank()
        holder.subtitle.text = conversation.recipients.joinToString(", ") { recipient ->
            recipient.contact?.name ?: recipient.address
        }

        holder.numbers.isVisible = conversation.recipients.size == 1
        (holder.numbers.adapter as PhoneNumberAdapter).data = conversation.recipients
            .mapNotNull { recipient -> recipient.contact }
            .flatMap { contact -> contact.numbers }
    }

    private fun bindStarred(holder: QkViewHolder, contact: Contact, prev: ComposeItem?) {
        holder.index.isVisible = false

        holder.icon.isVisible = prev !is ComposeItem.Starred
        holder.icon.setImageResource(R.drawable.ic_star_black_24dp)

        holder.avatar.recipients = listOf(createRecipient(contact))

        holder.title.text = contact.name

        holder.subtitle.isVisible = false

        holder.numbers.isVisible = true
        (holder.numbers.adapter as PhoneNumberAdapter).data = contact.numbers
    }

    private fun bindGroup(holder: QkViewHolder, group: ContactGroup, prev: ComposeItem?) {
        holder.index.isVisible = false

        holder.icon.isVisible = prev !is ComposeItem.Group
        holder.icon.setImageResource(R.drawable.ic_people_black_24dp)

        holder.avatar.recipients = group.contacts.map(::createRecipient)

        holder.title.text = group.title

        holder.subtitle.isVisible = true
        holder.subtitle.text = group.contacts.joinToString(", ") { it.name }

        holder.numbers.isVisible = false
    }

    private fun bindPerson(holder: QkViewHolder, contact: Contact, prev: ComposeItem?) {
        holder.index.isVisible = true
        holder.index.text =
            if (contact.name.getOrNull(0)?.isLetter() == true) contact.name[0].toString() else "#"

        tryOrNull {
            holder.index.isVisible = prev !is ComposeItem.Person ||
                    (contact.name[0].isLetter() && !contact.name[0].equals(
                        prev.value.name[0],
                        ignoreCase = true
                    )) ||
                    (!contact.name[0].isLetter() && prev.value.name[0].isLetter())
        }

        holder.icon.isVisible = false

        holder.avatar.recipients = listOf(createRecipient(contact))

        holder.title.text = contact.name

        holder.subtitle.isVisible = false

        holder.numbers.isVisible = true

        if (isSelectedstring(contact.lookupKey)) {
            holder.select.setVisible(true)
        } else {
            holder.select.setVisible(false)

        }
        (holder.numbers.adapter as PhoneNumberAdapter).data = contact.numbers
    }

    private fun createRecipient(contact: Contact): Recipient {
        return recipients[contact.lookupKey] ?: Recipient(
            address = contact.numbers.firstOrNull()?.address ?: "",
            contact = contact
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        disposables += conversationRepo.getUnmanagedRecipients()
            .map { recipients -> recipients.associateByNotNull { recipient -> recipient.contact?.lookupKey } }
            .subscribe { recipients -> this@ComposeItemAdapter.recipients = recipients }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        disposables.clear()
    }

    override fun areItemsTheSame(old: ComposeItem, new: ComposeItem): Boolean {
        val oldIds = old.getContacts().map { contact -> contact.lookupKey }
        val newIds = new.getContacts().map { contact -> contact.lookupKey }
        return oldIds == newIds
    }

    override fun areContentsTheSame(old: ComposeItem, new: ComposeItem): Boolean {
        return false
    }

}
