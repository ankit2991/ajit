package com.messaging.textrasms.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.feature.simplenotes.NoteActivity
import com.messaging.textrasms.manager.model.AllowNumber
import com.messaging.textrasms.manager.model.FilterBlockedNumber
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.utils.Constants
import dagger.android.AndroidInjection
import io.realm.Realm
import io.realm.exceptions.RealmPrimaryKeyConstraintException
import kotlinx.android.synthetic.main.archive_activity.nativeBanner
import kotlinx.android.synthetic.main.layout_big_native_ad.maxAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.nativeAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.textViewRectangleNative
import kotlinx.android.synthetic.main.layout_filter.nativeAd
import java.util.*

class FilterActivity : QkThemedActivity() {

    private var title: String? = ""

    lateinit var blockMainFragment: BlockMainFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_filter)
        showBackButton(true)

        blockMainFragment = BlockMainFragment()
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.your_placeholder, blockMainFragment)
        ft.commit()

        if (!Preferences.getBoolean(this, Preferences.SetFilter)) {

            val blockedNumberList: MutableList<FilterBlockedNumber> = ArrayList()
            blockedNumberList.add(getEmployee("Claim Your Reward", sender = false, content = true))
            blockedNumberList.add(
                getEmployee(
                    "Send Credit Card Details",
                    sender = false,
                    content = true
                )
            )
            blockedNumberList.add(getEmployee("Buy Now!", false, content = true))
            blockedNumberList.add(getEmployee("9377332220", sender = true, content = false))
            blockedNumberList.add(getEmployee("405030003", sender = true, content = false))

            val allowNumberList: MutableList<AllowNumber> = ArrayList()
            allowNumberList.add(getAllowNumber("Show tickets", false, content = true))
            allowNumberList.add(getAllowNumber("Rember yesterday?", sender = false, content = true))
            allowNumberList.add(getAllowNumber("Did you call me?", sender = false, content = true))
            allowNumberList.add(getAllowNumber("Good Morning:)", sender = false, content = true))
            allowNumberList.add(getAllowNumber("4459994", sender = true, content = false))
            allowNumberList.add(getAllowNumber("678966788", sender = true, content = false))

            val thread = Thread {
                val mRealm: Realm? = Realm.getDefaultInstance()
                try {
                    mRealm!!.executeTransaction { realm ->
                        realm.insertOrUpdate(blockedNumberList)
                    }
                } catch (e: RealmPrimaryKeyConstraintException) {
                    Log.i("TAG", "onCreate: error :- " + e.message)
                }

                try {
                    mRealm!!.executeTransaction { realm ->
                        realm.insertOrUpdate(allowNumberList)
                    }
                } catch (e: RealmPrimaryKeyConstraintException) {
                    Log.i("TAG", "onCreate: error 1 :- " + e.message)
                }

                Log.i("TAG", "onCreate: thread :- ")
                Preferences.setBoolean(this, Preferences.SetFilter, true)
            }
            thread.start()
        }

    }

    private fun getAllowNumber(address: String, sender: Boolean, content: Boolean): AllowNumber {
        val employee = AllowNumber()
        employee.address = address
        employee.sender = sender
        employee.content = content
        employee.id = UUID.randomUUID().mostSignificantBits
        return employee
    }

    private fun getEmployee(
        address: String,
        sender: Boolean,
        content: Boolean
    ): FilterBlockedNumber {
        val employee = FilterBlockedNumber()
        employee.address = address
        employee.sender = sender
        employee.content = content
        employee.id = UUID.randomUUID().mostSignificantBits
        return employee
    }


    public override fun onPause() {
        super.onPause()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        Constants.IS_FROM_ACTIVITY = true
        super.onBackPressed()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("onActivityResult", "onActivityResult2222: " + requestCode)
        if (requestCode == 35) {
            try {
                blockMainFragment.setUpViewPager()
            } catch (e: Exception) {

            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun isPurchased(): Boolean {
        val purchase = Preferences.getBoolean(this, Preferences.ADSREMOVED)
        return purchase
    }
    private fun showNativeAd() {
        if (!isPurchased()) {

            MaxAdManager.createNativeAd(this,maxAdContainer,nativeAdContainer,textViewRectangleNative,{
                nativeAd.visibility = View.GONE
            },{
                nativeAd.visibility = View.VISIBLE
            })

        } else {
            nativeAd.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        showNativeAd()
    }
}