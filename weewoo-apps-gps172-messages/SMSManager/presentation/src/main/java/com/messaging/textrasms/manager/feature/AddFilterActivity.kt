package com.messaging.textrasms.manager.feature

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.datachsnged
import com.messaging.textrasms.manager.feature.simplenotes.NoteActivity
import com.messaging.textrasms.manager.model.AllowNumber
import com.messaging.textrasms.manager.model.FilterBlockedNumber
import dagger.android.AndroidInjection
import io.realm.Realm
import io.realm.RealmResults
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import java.text.DateFormat
import java.util.*
import javax.inject.Inject


class AddFilterActivity : QkThemedActivity() {

    private var title: String? = ""
    private lateinit var keyword: TextInputEditText
    private lateinit var block: TextView
    private lateinit var allow: TextView
    private lateinit var content: TextView
    private lateinit var sender: TextView
    private lateinit var done: Button
    private lateinit var cancel: Button
    private var position1: Int = 0
    private var position2: Int = 0
    var mRealm: Realm? = null
    private val list = ArrayList<FilterBlockedNumber>()
    private var allowlist = ArrayList<AllowNumber>()
    lateinit var toggle_btn: ToggleSwitch
    lateinit var toggle_btn2: ToggleSwitch
    private lateinit var description: TextView
    private var number: String = ""
    private var contentvalue: Boolean = false
    private var sendervalue: Boolean = false
    private lateinit var from: String
    private var selection: Int = 0

    @Inject
    lateinit var blockingClient: BlockingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_addfilter)

        showBackButton(true)
        mRealm = Realm.getDefaultInstance()
        keyword = findViewById(R.id.keyword)
        block = findViewById(R.id.block)
        allow = findViewById(R.id.allow)
        content = findViewById(R.id.content)
        sender = findViewById(R.id.sender)
        done = findViewById(R.id.done)
        cancel = findViewById(R.id.cancel)
        toggle_btn = findViewById(R.id.toggle_btn)
        toggle_btn2 = findViewById(R.id.toggle_btn2)
        description = findViewById(R.id.description)
        toggle_btn.setCheckedPosition(0)
        toggle_btn2.setCheckedPosition(0)
        if (intent.extras != null) {
            number = intent.getStringExtra("number")!!
            contentvalue = intent.getBooleanExtra("content", false)
            sendervalue = intent.getBooleanExtra("sender", false)
            from = intent.getStringExtra("fromtype")!!
            if (!number.equals("")) {
                keyword.setText(number)
            }
            if (contentvalue.equals(true)) {
                toggle_btn2.setCheckedPosition(0)
                position2 = 0
            }
            if (sendervalue.equals(true)) {
                keyword.inputType = InputType.TYPE_CLASS_PHONE
                toggle_btn2.setCheckedPosition(1)
                position2 = 1

            }
            if (from.equals("Block")) {
                toggle_btn.setCheckedPosition(0)
                position1 = 0
            } else if (from.equals("Allow")) {
                toggle_btn.setCheckedPosition(1)
                position1 = 1
            }
        }

        done.setOnClickListener {
            var keyword1 = keyword.text.toString().trim()
            if (keyword1.length != 0 && !keyword1.equals("")) {
                if (position1 == 0) {
                    if (position2 == 0) {
                        addblock(keyword1, false, true)
                        finish()
                    } else {
                        var num = isvalidNumber(keyword1)
                        if (num) {
                            addblock(keyword1, true, false)
                            finish()
                        } else {
                            keyword.error = "Enter Valid Number."
                            Toast.makeText(this, "Enter Valid Number...", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    if (position1 == 1) {
                        if (position2 == 0) {
                            addAllow(keyword1, false, true)
                            finish()
                        } else {
                            var num = isvalidNumber(keyword1)
                            if (num) {
                                addAllow(keyword1, true, false)
                                finish()
                            } else {
                                keyword.error = "Enter Valid Number."
                                Toast.makeText(this, "Enter Valid Number...", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }


            } else {
                Toast.makeText(this, "add some content...", Toast.LENGTH_SHORT).show()
            }

        }
        cancel.setOnClickListener {
            finish()
        }
        toggle_btn.onChangeListener = object : ToggleSwitch.OnChangeListener {
            override fun onToggleSwitchChanged(position: Int) {
                if (position == 0) {
                    if (position2 == 0) {
                        keyword.hint = "Enter Keyword.."
                        keyword.inputType = InputType.TYPE_CLASS_TEXT
                        description.text = "Block SMS when " + "" + " appears in message Content."
                    }
                    if (position2 == 1) {
                        keyword.hint = "Enter Number.."
                        keyword.inputType = InputType.TYPE_CLASS_PHONE
                        description.text = "Block SMS when " + "" + " appears in message Sender."
                    }
                } else if (position == 1) {
                    if (position2 == 0) {
                        keyword.hint = "Enter Keyword.."
                        keyword.inputType = InputType.TYPE_CLASS_TEXT
                        description.text = "Allow SMS when " + "" + " appears in message Content."
                    }
                    if (position2 == 1) {
                        keyword.hint = "Enter Number.."
                        keyword.inputType = InputType.TYPE_CLASS_PHONE
                        description.text = "Allow SMS when " + "" + " appears in message Sender."
                    }

                }
                position1 = position
            }
        }

        toggle_btn2.onChangeListener = object : ToggleSwitch.OnChangeListener {
            override fun onToggleSwitchChanged(position: Int) {

                position2 = position
                if (position == 0) {
                    keyword.hint = "Enter Keyword.."
                    keyword.inputType = InputType.TYPE_CLASS_TEXT
                    if (position1 == 0) {

                        description.text = "Block SMS when " + "" + " appears in message Content."
                    }
                    if (position1 == 1) {
                        description.text = "Allow SMS when " + "" + " appears in message Content."
                    }
                    //   description.text = "Block SMS when " + "" + " appears in message Sender."
                } else if (position == 1) {
                    keyword.hint = "Enter Number.."
                    keyword.inputType = InputType.TYPE_CLASS_PHONE
                    if (position1 == 0) {
                        description.text = "Block SMS when " + "" + " appears in message Sender."
                    }
                    if (position1 == 1) {

                        description.text = "Allow SMS when " + "" + " appears in message Sender."
                    }

                }
            }
        }


    }

    fun isvalidNumber(PhoneNo: String): Boolean {
        val Regex = "[^\\d]"
        val PhoneDigits = PhoneNo.replace(Regex.toRegex(), "")
        if (!PhoneNo.trim().equals("") && PhoneDigits.length >= 10) {
            // return parse(phone) != null
            return Patterns.PHONE.matcher(PhoneNo).matches()
        }

        return false
    }

    fun GetCountryZipCode(c: String?): String? {
        var CountryID = ""
        var CountryZipCode = c
        val manager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        CountryID = manager.simCountryIso.uppercase(Locale.getDefault())
        val rl = this.resources.getStringArray(R.array.CountryCodes)
        for (i in rl.indices) {
            val g = rl[i].split(",").toTypedArray()
            if (g[1].trim { it <= ' ' } == CountryID.trim { it <= ' ' }) {
                CountryZipCode = g[0]
                break
            }
        }
        return CountryZipCode
    }

    @SuppressLint("AutoDispose")
    fun addblock(toString: String, sender: Boolean, content: Boolean) {
        try {
            getallowdata()
            mRealm?.beginTransaction()
            if (number.equals("")) {
                val employee = FilterBlockedNumber()
                if (sender.equals(true)) {
                    val locale: String = resources.configuration.locale.country
                    GetCountryZipCode(locale)
                    employee.address = "+" + GetCountryZipCode(locale) + toString.trim()
                } else {
                    employee.address = toString
                }
                employee.sender = sender
                employee.content = content
                employee.id = UUID.randomUUID().mostSignificantBits
                val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())

                if (containsName(allowlist, employee.address)) {
                    removeallow(employee.address)
                }
                mRealm?.insertOrUpdate(employee)
                mRealm?.commitTransaction()
                mRealm!!.close()
                if (sender.equals(true)) {
                    var id = conversationRepo.getThreadId(employee.address)
                    if (id != null) {
                        conversationRepo.markBlocked(
                            listOf(id),
                            prefs.blockingManager.get(),
                            "reason"
                        )
                    }
                    blockingClient.block(listOf(employee.address)).subscribe()
                }

                datachsnged = true
            } else {
                val toEdit = mRealm?.where(FilterBlockedNumber::class.java)
                    ?.equalTo("address", number)?.findFirst()
                //realm.beginTransaction()
                if (toEdit == null) {
                    var oldaddress: String
                    oldaddress = number
                    val employee = FilterBlockedNumber()
                    if (sender.equals(true)) {
                        val locale: String = resources.configuration.locale.country
                        GetCountryZipCode(locale)
                        employee.address = "+" + GetCountryZipCode(locale) + toString.trim()
                    } else {
                        employee.address = toString
                    }
                    employee.sender = sender
                    employee.content = content
                    //  employee.setName(s);
                    employee.id = UUID.randomUUID().mostSignificantBits
                    if (containsName(allowlist, employee.address)) {
                        removeallow(toEdit?.address)
                    }
                    mRealm?.insertOrUpdate(employee)
                    mRealm?.commitTransaction()
                    mRealm!!.close()

                    if (sender.equals(true)) {
                        var id = conversationRepo.getThreadId(oldaddress)
                        if (id != null) {
                            conversationRepo.markUnblocked(id)
                        }

                        blockingClient.unblock(listOf(oldaddress)).subscribe()
                        blockingClient.block(listOf(employee.address)).subscribe()
                    }

                } else {
                    var oldaddress: String
                    oldaddress = toEdit.address
                    if (sender.equals(true)) {
                        val locale: String = resources.configuration.locale.country
                        GetCountryZipCode(locale)
                        if (toString.trim().contains("+")) {
                            toEdit.address = toString.trim()
                        } else {
                            toEdit.address = "+" + GetCountryZipCode(locale) + toString.trim()
                        }
                    } else {
                        toEdit.address = toString
                    }

                    toEdit.sender = sender
                    (toEdit.content) = content
                    if (containsName(allowlist, toEdit.address)) {
                        removeallow(toEdit.address)
                    }
                    mRealm?.commitTransaction()
                    mRealm!!.close()
                    if (sender.equals(true)) {
                        var id = conversationRepo.getThreadId(oldaddress)
                        if (id != null) {
                            conversationRepo.markUnblocked(id)
                        }

                        blockingClient.unblock(listOf(oldaddress)).subscribe()
                        blockingClient.block(listOf(toEdit.address)).subscribe()
                    }

                }
                datachsnged = true
            }

        } catch (e: RealmPrimaryKeyConstraintException) {

        }
    }

    private var blocklist = ArrayList<FilterBlockedNumber>()
    fun getblockdata(): RealmResults<FilterBlockedNumber>? {
        blocklist = ArrayList<FilterBlockedNumber>()
        val results = mRealm!!.where(FilterBlockedNumber::class.java).findAll()
        blocklist.addAll(mRealm!!.copyFromRealm(results))
        Log.d("getblockdata", "getblockdata: " + list.size)
        return results
    }

    @SuppressLint("AutoDispose")
    fun addAllow(toString: String, sender: Boolean, content: Boolean) {
        try {
            getblockdata()
            mRealm!!.executeTransaction(Realm.Transaction { realm ->
                if (number.equals("")) {
                    val employee = AllowNumber()

                    if (sender.equals(true)) {
                        val locale: String = resources.configuration.locale.country
                        GetCountryZipCode(locale)
                        employee.address = "+" + GetCountryZipCode(locale) + toString.trim()
                    } else {
                        employee.address = toString
                    }
                    employee.sender = sender
                    employee.content = content
                    employee.id = UUID.randomUUID().mostSignificantBits

                    if (containsNameblock(blocklist, employee.address)) {
                        removeblock(employee.address)
                    }
                    realm.insertOrUpdate(employee)

                } else {
                    val toEdit = realm.where(AllowNumber::class.java)
                        .equalTo("address", number).findFirst()
                    if (toEdit == null) {

                        val employee = AllowNumber()

                        if (sender.equals(true)) {
                            val locale: String =
                                resources.configuration.locale.country
                            GetCountryZipCode(locale)
                            employee.address = "+" + GetCountryZipCode(locale) + toString.trim()
                        } else {
                            employee.address = toString
                        }
                        employee.sender = sender
                        employee.content = content
                        //  employee.setName(s);
                        employee.id = UUID.randomUUID().mostSignificantBits
                        if (containsNameblock(blocklist, employee.address)) {
                            removeblock(employee.address)
                        }
                        realm.insertOrUpdate(employee)


                    } else {
                        var oldaddress: String
                        oldaddress = toEdit.address
                        if (sender.equals(true)) {
                            val locale: String =
                                resources.configuration.locale.country
                            GetCountryZipCode(locale)
                            toEdit.address = "+" + GetCountryZipCode(locale) + toString.trim()
                        } else {
                            toEdit.address = toString
                        }
                        toEdit.sender = sender
                        (toEdit.content) = content
                        realm.copyToRealmOrUpdate(toEdit)

                        if (sender.equals(true)) {
                            var id = conversationRepo.getThreadId(oldaddress)
                            if (id != null) {
                                conversationRepo.markUnblocked(id)
                            }
                            blockingClient.unblock(listOf(oldaddress)).subscribe()
                            blockingClient.block(listOf(toEdit.address)).subscribe()
                        }
                        if (containsNameblock(blocklist, toEdit.address)) {
                            removeblock(toEdit.address)
                        }
                    }
                }
                datachsnged = true

            })
        } catch (e: RealmPrimaryKeyConstraintException) {

        }
    }

    fun removeblock(id: String) {
        try {

            Log.d("removeblock", "removeblock: $id")

            val rows =
                mRealm?.where(FilterBlockedNumber::class.java)!!.equalTo("address", id).findAll()
            rows.deleteAllFromRealm()

        } catch (e: RealmPrimaryKeyConstraintException) {
            Toast.makeText(
                this,
                "Primary Key exists, Press Update instead",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun removeallow(id: String?) {
        try {

            val rows = mRealm!!.where(AllowNumber::class.java).equalTo("address", id).findAll()
            rows.deleteAllFromRealm()

        } catch (e: RealmPrimaryKeyConstraintException) {

        }
    }

    fun containsName(list: List<AllowNumber>, name: String): Boolean {
        return list.stream().filter { o: AllowNumber -> o.address == name }.findFirst().isPresent
    }

    fun containsNameblock(list: List<FilterBlockedNumber>, name: String): Boolean {
        return list.stream().filter { o: FilterBlockedNumber -> o.address == name }
            .findFirst().isPresent
    }

    fun getallowdata(): RealmResults<AllowNumber>? {
        allowlist = ArrayList<AllowNumber>()
        val results = mRealm!!.where(AllowNumber::class.java).findAll()
        allowlist.addAll(mRealm!!.copyFromRealm(results))
        Log.d("getblockdata", "getblockdata: " + allowlist.size)
        return results
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        private const val EXTRA_NOTE_TITLE = "EXTRA_NOTE_TITLE"

        @JvmStatic
        fun getStartIntent(context: Context?, title: String?): Intent {
            val intent = Intent(context, NoteActivity::class.java)
            intent.putExtra(EXTRA_NOTE_TITLE, title)
            return intent
        }
    }

    @SuppressLint("AutoDispose")
    fun unblock(address: String) {
        blockingClient.unblock(listOf(address)).subscribe()
    }
}