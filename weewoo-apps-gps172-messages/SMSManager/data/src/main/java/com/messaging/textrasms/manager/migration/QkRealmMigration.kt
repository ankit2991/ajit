package com.messaging.textrasms.manager.migration

import android.annotation.SuppressLint
import com.messaging.textrasms.manager.extensions.map
import com.messaging.textrasms.manager.mapper.CursorToContactImpl
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.util.Preferences
import io.realm.*
import java.util.*
import javax.inject.Inject

class QkRealmMigration @Inject constructor(
    private val cursorToContact: CursorToContactImpl,
    private val prefs: Preferences
) : RealmMigration {

    companion object {
        const val SchemaVersion: Long = 15
    }

    @SuppressLint("ApplySharedPref")
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var version = oldVersion
        logDebug("takingtime" + "100")
        if (version == 0L) {
            realm.schema.get("MmsPart")
                ?.removeField("image")

            version++
        }

        if (version == 1L) {
            realm.schema.get("Message")
                ?.addField("subId", Int::class.java)

            version++
        }

        if (version == 2L) {
            realm.schema.get("Conversation")
                ?.addField("name", String::class.java, FieldAttribute.REQUIRED)

            version++
        }

        if (version == 3L) {
            realm.schema.create("ScheduledMessage")
                .addField(
                    "id",
                    Long::class.java,
                    FieldAttribute.PRIMARY_KEY,
                    FieldAttribute.REQUIRED
                )
                .addField("date", Long::class.java, FieldAttribute.REQUIRED)
                .addField("subId", Long::class.java, FieldAttribute.REQUIRED)
                .addRealmListField("recipients", String::class.java)
                .addField("sendAsGroup", Boolean::class.java, FieldAttribute.REQUIRED)
                .addField("body", String::class.java, FieldAttribute.REQUIRED)
                .addRealmListField("attachments", String::class.java)

            version++
        }

        if (version == 4L) {
            realm.schema.get("Conversation")
                ?.addField(
                    "pinned",
                    Boolean::class.java,
                    FieldAttribute.REQUIRED,
                    FieldAttribute.INDEXED
                )

            version++
        }

        if (version == 5L) {
            realm.schema.create("BlockedNumber")
                .addField(
                    "id",
                    Long::class.java,
                    FieldAttribute.PRIMARY_KEY,
                    FieldAttribute.REQUIRED
                )
                .addField("address", String::class.java, FieldAttribute.REQUIRED)

            version++
        }

        if (version == 6L) {
            realm.schema.get("Conversation")
                ?.addField("blockingClient", Integer::class.java)
                ?.addField("blockReason", String::class.java)

            realm.schema.get("MmsPart")
                ?.addField("seq", Integer::class.java, FieldAttribute.REQUIRED)
                ?.addField("name", String::class.java)

            version++
        }

        if (version == 7L) {
            realm.schema.get("Conversation")
                ?.addRealmObjectField("lastMessage", realm.schema.get("Message"))
                ?.removeField("count")
                ?.removeField("date")
                ?.removeField("snippet")
                ?.removeField("read")
                ?.removeField("me")

            val conversations = realm.where("Conversation")
                .findAll()

            val messages = realm.where("Message")
                .sort("date", Sort.DESCENDING)
                .distinct("threadId")
                .findAll()
                .associateBy { message -> message.getLong("threadId") }

            conversations.forEach { conversation ->
                conversation.setObject("lastMessage", messages[conversation.getLong("id")])
            }

            version++
        }

        if (version == 8L) {
            realm.delete("PhoneNumber")

            realm.schema.create("ContactGroup")
                .addField(
                    "id",
                    Long::class.java,
                    FieldAttribute.PRIMARY_KEY,
                    FieldAttribute.REQUIRED
                )
                .addField("title", String::class.java, FieldAttribute.REQUIRED)
                .addRealmListField("contacts", realm.schema.get("Contact"))

            realm.schema.get("PhoneNumber")
                ?.addField(
                    "id",
                    Long::class.java,
                    FieldAttribute.PRIMARY_KEY,
                    FieldAttribute.REQUIRED
                )
                ?.addField("accountType", String::class.java)
                ?.addField("isDefault", Boolean::class.java, FieldAttribute.REQUIRED)

            val phoneNumbers = cursorToContact.getContactsCursor()
                ?.map(cursorToContact::map)
                ?.distinctBy { contact -> contact.numbers.firstOrNull()?.id } // Each row has only one number
                ?.groupBy { contact -> contact.lookupKey }
                ?: mapOf()

            realm.schema.get("Contact")
                ?.addField("starred", Boolean::class.java, FieldAttribute.REQUIRED)
                ?.addField("photoUri", String::class.java)
                ?.transform { realmContact ->
                    val numbers = RealmList<DynamicRealmObject>()
                    phoneNumbers[realmContact.get("lookupKey")]
                        ?.flatMap { contact -> contact.numbers }
                        ?.map { number ->
                            realm.createObject("PhoneNumber", number.id).apply {
                                setString("accountType", number.accountType)
                                setString("address", number.address)
                                setString("type", number.type)
                            }
                        }
                        ?.let(numbers::addAll)

                    val photoUri = phoneNumbers[realmContact.get("lookupKey")]
                        ?.firstOrNull { number -> number.photoUri != null }
                        ?.photoUri

                    realmContact.setList("numbers", numbers)
                    realmContact.setString("photoUri", photoUri)
                }


            val recipients = mutableMapOf<Long, Int>()
            realm.where("Conversation").findAll().forEach { conversation ->
                val pref = prefs.theme(conversation.getLong("id"))
                if (pref.isSet) {
                    conversation.getList("recipients").forEach { recipient ->
                        recipients[recipient.getLong("id")] = pref.get()
                    }

                    pref.delete()
                }
            }

            recipients.forEach { (recipientId, theme) ->
                prefs.theme(recipientId).set(theme)
            }

            version++
        }
        if (version == 9L) {
            realm.schema.get("Conversation")
                ?.addField("ispossible", Boolean::class.java)
                ?.addField("isspam", Boolean::class.java)

            version++
        }

        if (version == 10L) {
            if (!realm.schema.get("Conversation")!!
                    .hasField("month") && !realm.schema.get("Conversation")!!.hasField("datemanual")
            ) {

                realm.schema.get("Conversation")
                    ?.addField("month", String::class.java, FieldAttribute.INDEXED)
                    ?.addField("datemanual", Date::class.java)
            }

            if (!realm.schema.get("Message")!!
                    .hasField("datecompare") && !realm.schema.get("Message")!!
                    .hasField("month") && !realm.schema.get("Message")!!.hasField("year")
            ) {

                realm.schema.get("Message")
                    ?.addField("datecompare", Date::class.java)
                    ?.addField("month", Long::class.java)
                    ?.addField("year", Long::class.java)
            }

            version++
        }
        if (version == 11L) {
            if (!realm.schema.get("Conversation")!!
                    .hasField("month") && !realm.schema.get("Conversation")!!.hasField("datemanual")
            ) {
                realm.schema.get("Conversation")
                    ?.addField("month", String::class.java, FieldAttribute.INDEXED)
                    ?.addField("datemanual", Date::class.java)
            }
            if (!realm.schema.get("Message")!!
                    .hasField("datecompare") && !realm.schema.get("Message")!!
                    .hasField("month") && !realm.schema.get("Message")!!.hasField("year")
            ) {

                realm.schema.get("Message")
                    ?.addField("datecompare", Date::class.java)
                    ?.addField("month", Long::class.java)
                    ?.addField("year", Long::class.java)
            }

            version++
        }
        if (version == 12L) {


            realm.schema.get("Message")
                ?.addRealmListField("group_msg", String::class.java)

            version++
        }
        if (version == 13L) {


            realm.schema.get("Conversation")
                ?.addField("transactional", Boolean::class.java)

            realm.schema.get("Conversation")
                ?.addField("Promotion", Boolean::class.java)

            realm.schema.get("Conversation")
                ?.addField("isunknown", Boolean::class.java)


            version++
        }
        if (version == 14L) {

            realm.schema.get("Conversation")
                ?.addField("blockfromfilter", Boolean::class.java)
            realm.schema.create("AllowNumber")
                .addField(
                    "id",
                    Long::class.java,
                    FieldAttribute.PRIMARY_KEY,
                    FieldAttribute.REQUIRED
                )
                .addField("address", String::class.java, FieldAttribute.REQUIRED)
                .addField("content", Boolean::class.java)
                .addField("sender", Boolean::class.java)

            realm.schema.create("FilterBlockedNumber")
                .addField(
                    "id",
                    Long::class.java,
                    FieldAttribute.PRIMARY_KEY,
                    FieldAttribute.REQUIRED
                )
                .addField("address", String::class.java, FieldAttribute.REQUIRED)
                .addField("content", Boolean::class.java)
                .addField("sender", Boolean::class.java)

            version++
        }

        check(version >= newVersion) { "Migration missing from v$oldVersion to v$newVersion" }
    }


}
