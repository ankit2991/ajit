package com.messaging.textrasms.manager

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.messaging.textrasms.manager.databinding.MainCallsLayoutBinding
import com.messaging.textrasms.manager.feature.AddFilterActivity
import com.messaging.textrasms.manager.util.resolveThemeColor
import io.realm.Realm

class BlockMainFragment : Fragment() {
    private lateinit var binding: MainCallsLayoutBinding
    var adapter: CallLogViewPagerAdapter? = null
    var fragment1: BlockFragment? = null
    var mFragment: AllowFragment? = null

    var addialog: ProgressDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.main_calls_layout,
            container,
            false
        )
        addialog = ProgressDialog(activity)
        addialog!!.setMessage("Ad Showing....")
        addialog!!.setCancelable(false)
        initComponents()

        mRealm = Realm.getDefaultInstance()

        return binding.getRoot()
    }

    public fun setUpViewPager() {
        adapter = CallLogViewPagerAdapter(childFragmentManager)


        fragment1 = BlockFragment(this)
        mFragment = AllowFragment()
        adapter!!.addFragment("Block", fragment1!!)
        adapter!!.addFragment("Allow", mFragment!!)
        binding.contentView.viewpager.adapter = adapter
        binding!!.tabs.setupWithViewPager(binding.contentView.viewpager)
        val headerView = LayoutInflater.from(activity).inflate(
            R.layout.filter_tab, null
        )
        val linearLayoutOne: LinearLayout = headerView.findViewById(R.id.ll)
        val linearLayout2: LinearLayout = headerView.findViewById(R.id.ll2)
        val tvtab1: TextView = headerView.findViewById(R.id.tvtab1)
        val tvtab2: TextView = headerView.findViewById(R.id.tvtab2)
        tvtab1.setTextColor(Color.parseColor("#0074FE"))
        binding.tabs.getTabAt(0)!!.customView = linearLayoutOne
        binding.tabs.getTabAt(1)!!.customView = linearLayout2
        binding.tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    tvtab1.setTextColor(Color.parseColor("#0074FE"))
                    tvtab1.setBackgroundResource(R.drawable.filter_bg)
                } else if (tab.position == 1) {
                    tvtab2.setTextColor(Color.parseColor("#0074FE"))
                    tvtab2.setBackgroundResource(R.drawable.filter_bg)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    tvtab1.setTextColor(activity!!.resolveThemeColor(R.attr.tabcolor))
                    tvtab1.setBackgroundResource(R.drawable.filter_bg_unselect)
                } else if (tab.position == 1) {
                    tvtab2.setTextColor(activity!!.resolveThemeColor(R.attr.tabcolor))
                    tvtab2.setBackgroundResource(R.drawable.filter_bg_unselect)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding!!.contentView.viewpager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position == 0) {
                    fromblock = true
                } else {
                    fromblock = false
                }
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding!!.fragmentFab.setOnClickListener {
            var selection = "Block"
            if (fromblock) {
                selection = "Block"
            } else {
                selection = "Allow"
            }
            startActivityForResult(
                Intent(activity, AddFilterActivity::class.java).putExtra("fromtype", "Block")
                    .putExtra("number", "")
                    .putExtra("content", "")
                    .putExtra("selection", 2)
                    .putExtra("sender", "")
                    .putExtra("fromtype", selection), 35
            )

        }
    }

    private fun initComponents() {
        setUpViewPager()
    }

    inner class CallLogViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(title: String, fragment: Fragment) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    var mRealm: Realm? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val locale = activity!!.resources.configuration.locale.country
        Log.d("onActivityResult", "setUserVisibleHint: $requestCode")
        if (requestCode == 35) {
            setUpViewPager()
        }
        if (requestCode == 20) {
            if (data != null) {
                val number = data.getStringExtra("Number")
                val name = data.getStringExtra("Name")
                val formated: String?
                formated = if (getcountrycode(number) == null || getcountrycode(number) == "") {
                    number
                } else {
                    deleteCountry(number)
                }
                if (fromblock) {
                    if (number != null && number != "") {
                        if (name != null && name != "") fragment1!!.addblock(
                            formated,
                            name
                        ) else fragment1!!.addblock(formated, formated)
                    }
                } else {
                    if (number != null && number != "") {
                        if (name != null && name != "") mFragment!!.addAllow(
                            formated,
                            name
                        ) else mFragment!!.addAllow(formated, formated)
                    }
                }
            }
        }
    }

    fun deleteCountry(phone: String?): String? {
        val phoneInstance = PhoneNumberUtil.getInstance()
        try {
            val phoneNumber = phoneInstance.parse(phone, null)
            return phoneNumber.nationalNumber.toString()
        } catch (e: Exception) {
        }
        Log.d("deleteCountry", "deleteCountry: $phone")
        return phone
    }

    fun getcountrycode(phone: String?): String? {
        val phoneInstance = PhoneNumberUtil.getInstance()
        try {
            val phoneNumber = phoneInstance.parse(phone, null)
            Log.d("deleteCountry", "deleteCountry: " + phoneNumber.countryCode.toString())
            return phoneNumber.countryCode.toString()
        } catch (e: Exception) {
        }
        return phone
    }

    companion object {
        var fromblock = true
    }

    @SuppressLint("AutoDispose")
    fun unblock(address: String) {

    }
}