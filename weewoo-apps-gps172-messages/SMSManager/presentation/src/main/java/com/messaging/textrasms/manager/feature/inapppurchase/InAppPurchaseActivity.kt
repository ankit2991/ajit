package com.messaging.textrasms.manager.feature.inapppurchase

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
//import com.android.vending.billing.IInAppBillingService
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.AppUtils
import com.messaging.textrasms.manager.common.util.MaxMainAdsManger
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.common.widget.QkTextView
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.Preferences.Companion.getBoolean
import com.messaging.textrasms.manager.util.Preferences.Companion.setBoolean
import dagger.android.AndroidInjection
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.enums.ProductType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.ProductInfo
import games.moisoni.google_iab.models.PurchaseInfo
import kotlinx.android.synthetic.main.activity_in_app_purchase.*
import org.solovyev.android.checkout.*
import org.solovyev.android.checkout.Inventory.Products
import org.solovyev.android.checkout.ProductTypes.IN_APP
import xyz.teamgravity.checkinternet.CheckInternet
import java.util.*

class InAppPurchaseActivity : QkThemedActivity(), View.OnClickListener {

    private var billingConnector : BillingConnector?= null
//    private val SKUs = listOf("com.messaging.textrasms.manager_29.99","com.messaging.textrasms.manager_9.99")
    private val SKUs = listOf("com.messaging.textrasms.manager_9.99","com.messaging.textrasms.manager_29.99")

//    private val nonConsumableIds = listOf("com.messaging.textrasms.manager_49.99")


//    var mService: IInAppBillingService? = null
    private var isSubscribedToAnyPack = false
    var activity: Activity? = null
    var mIsBound = true
    lateinit var ad_remove_rl_continue: LinearLayout
    var selected: Int = 1
    var currentPurchased: String = ""
    lateinit var ad_remove_tv_month: TextView
    lateinit var price1: TextView
    lateinit var ad_remove_tv_year: TextView
    lateinit var price2: TextView
    lateinit var ad_remove_tv: TextView
    lateinit var price3: TextView
    lateinit var close: ImageView
    lateinit var restore: TextView

    private val SKUS = Arrays.asList(
        "com.messaging.textrasms.manager_29.99",
        "com.messaging.textrasms.manager_9.99"
    //        "p1m","p1y"
    )

    private val SKUS_IN_APP = Arrays.asList("com.messaging.textrasms.manager_49.99")

    private var mCheckout: ActivityCheckout? = null
    private var isPurchaseFlowOpen = false
    var mSkuDetailsList: Products? = null
    var skuItemArrayList = java.util.ArrayList<Sku>()
    var pos = 99

    var serviceConnection: ServiceConnection? = null
    private var appIdslifetime = ""
    private var appIdsmonth = ""
    private var appIdslyear = ""

    override fun onDestroy() {
        super.onDestroy()
        if (billingConnector != null) {

            billingConnector?.release();

        }
//        mCheckout!!.stop()
    }

    override fun onResume() {
        super.onResume()

        isPurchaseFlowOpen = false
    }
/*
    private fun Restore() {
        try {

            if (mService != null) {
                adRemoveSkuDetails
                adRemoveSkuDetailsmonth
                adRemoveSkuDetailsyear
                adRemoveSkuDetailsbefore

                if (!isPurchased()) {
                    checkRemoveAdsPurchasedbefore(this.mService!!)
                }

                if (!isPurchased()) {
                    checkRemoveAdsPurchased(this.mService!!)
                }

                if (!isPurchased()) {
                    checkRemoveAdsPurchasedmonth(this.mService!!)
                }

                if (!isPurchased()) {
                    checkRemoveAdsPurchasedyear(this.mService!!)
                }
            }
        } catch (e: Exception) {

        }
    }*/

    private fun isPurchased(): Boolean {
        return getBoolean(this, Preferences.ADSREMOVED)
    }
//    private fun checkRemoveAdsPurchasedbefore(mService: IInAppBillingService) {
//
//        try {
//            val ownedItems =
//                mService.getPurchases(3, this.packageName, IN_APP, null)
//
//            val ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
//            val ownedSkusArray = JSONArray(ownedSkus)
//            if (ownedSkusArray.length() > 0) {
//                try {
//                    for (i in 0 until ownedSkusArray.length()) {
//
//                        if (ownedSkusArray.getString(i).equals(
//                                this.resources.getString(R.string.in_app_ads),
//                                ignoreCase = true
//                            )
//                        ) {
//                            logDebug("purchaseditemlistpurchase$ownedSkusArray")
//                            setBoolean(this, Preferences.ADSREMOVEDLifetime, true)
//                            updatePrefBoolean(true)
//                            Toast.makeText(
//                                this,
//                                "Purchased Restore successfully !",
//                                Toast.LENGTH_SHORT
//                            ).show()
//
//                            CustomAdLayoutActivity.fromcustom = true
//                            updateUIForProUser(false)
//
//                            break
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                    setBoolean(this, Preferences.ADSREMOVEDLifetime, false)
//                    updatePrefBoolean(false)
//                }
//            } else {
//                setBoolean(this, Preferences.ADSREMOVEDLifetime, false)
//                updatePrefBoolean(false)
//            }
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            setBoolean(this, Preferences.ADSREMOVEDLifetime, false)
//            updatePrefBoolean(false)
//        }
//    }
//    private fun checkRemoveAdsPurchased(mService: IInAppBillingService) {
//
//        try {
//            val ownedItems =
//                mService.getPurchases(3, this.packageName, IN_APP, null)
//
//            val ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
//            val ownedSkusArray = JSONArray(ownedSkus)
//            if (ownedSkusArray.length() > 0) {
//                try {
//                    for (i in 0 until ownedSkusArray.length()) {
//
//                        if (ownedSkusArray.getString(i).equals(
//                                this.resources.getString(R.string.in_app_ads_lifetime),
//                                ignoreCase = true
//                            )
//                        ) {
//                            logDebug("purchaseditemlist" + "purchase" + ownedSkusArray)
//                            setBoolean(this, Preferences.ADSREMOVEDLifetime, true)
//                            updatePrefBoolean(true)
//                            Toast.makeText(
//                                this,
//                                "Lifetime subscription Restore successfully !",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            CustomAdLayoutActivity.fromcustom = true
//
//                            updateUIForProUser(false)
//
//                            break
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                    setBoolean(this, Preferences.ADSREMOVEDLifetime, false)
//                    updatePrefBoolean(false)
//                    // adsLoadingStarts()
//                }
//            } else {
//                // Toast.makeText(this, "No subsciption Found !", Toast.LENGTH_SHORT).show()
//                setBoolean(this, Preferences.ADSREMOVEDLifetime, false)
//                updatePrefBoolean(false)
//                // adsLoadingStarts()
//            }
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            setBoolean(this, Preferences.ADSREMOVEDLifetime, false)
//            updatePrefBoolean(false)
//            // adsLoadingStarts()
//        }
//    }
//    private fun checkRemoveAdsPurchasedmonth(mService: IInAppBillingService) {
//
//        try {
//            val ownedItems = mService.getPurchases(3, this.packageName, SUBSCRIPTION, null)
//
//            val ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
//            val ownedSkusArray = JSONArray(ownedSkus)
//            if (ownedSkusArray.length() > 0) {
//                try {
//                    for (i in 0 until ownedSkusArray.length()) {
//
//                        if (ownedSkusArray.getString(i).equals(
//                                this.resources.getString(R.string.in_app_ads_month),
//                                ignoreCase = true
//                            )
//                        ) {
//                            logDebug("purchaseditemlistpurchase$ownedSkusArray")
//                            setBoolean(this, Preferences.ADSREMOVEDMONTH, true)
//                            updatePrefBoolean(true)
//                            Toast.makeText(
//                                this,
//                                "Monthly subscription Restore successfully !",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            CustomAdLayoutActivity.fromcustom = true
//
//                            updateUIForProUser(false)
//
//
//                            break
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                    setBoolean(this, Preferences.ADSREMOVEDMONTH, false)
//                    updatePrefBoolean(false)
//                    //adsLoadingStarts()
//                }
//            } else {
//                // Toast.makeText(this, "No subsciption Found !", Toast.LENGTH_SHORT).show()
//                setBoolean(this, Preferences.ADSREMOVEDMONTH, false)
//                updatePrefBoolean(false)
//                //adsLoadingStarts()
//            }
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            setBoolean(this, Preferences.ADSREMOVEDMONTH, false)
//            updatePrefBoolean(false)
//            //adsLoadingStarts()
//        }
//    }
//    private fun checkRemoveAdsPurchasedyear(mService: IInAppBillingService) {
//
//        try {
//            val ownedItems =
//                mService.getPurchases(3, this.packageName, SUBSCRIPTION, null)
//
//            val ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
//            val ownedSkusArray = JSONArray(ownedSkus)
//            if (ownedSkusArray.length() > 0) {
//                try {
//                    for (i in 0 until ownedSkusArray.length()) {
//
//                        if (ownedSkusArray.getString(i).equals(
//                                this.resources.getString(R.string.in_app_ads_Year),
//                                ignoreCase = true
//                            )
//                        ) {
//                            logDebug("purchaseditemlistpurchase$ownedSkusArray")
//                            setBoolean(this, Preferences.ADSREMOVEDYear, true)
//                            updatePrefBoolean(true)
//                            Toast.makeText(
//                                this,
//                                "Yearly subscription Restore successfully !",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            CustomAdLayoutActivity.fromcustom = true
//
//                            updateUIForProUser(false)
//
//                        }
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                    setBoolean(this, Preferences.ADSREMOVEDYear, false)
//                    updatePrefBoolean(false)
//                    // adsLoadingStarts()
//                }
//            } else {
//                setBoolean(this, Preferences.ADSREMOVEDYear, false)
//                updatePrefBoolean(false)
//                if (!isFinishing)
//                    Toast.makeText(this, "No subsciption Found !", Toast.LENGTH_SHORT).show()
//                // adsLoadingStarts()
//            }
//        } catch (e: RemoteException) {
//            e.printStackTrace()
//            setBoolean(this, Preferences.ADSREMOVEDYear, false)
//            updatePrefBoolean(false)
//            // adsLoadingStarts()
//        }
//    }


//    private fun init() {
//        var sku: Sku? = null
//        for (sku1 in skuItemArrayList) {
//            if (selected == 0) {
//                if (sku1.id.code.equals(SKUS_IN_APP.get(selected), ignoreCase = true)) {
//                    sku = sku1
//                    break
//                }
//            } else if (sku1.id.code.equals(SKUS.get(selected - 1), ignoreCase = true)) {
//                sku = sku1
//                break
//            }
//        }
//        if (sku != null) {
//            if (!isPurchaseFlowOpen) {
//                isPurchaseFlowOpen = true
//                mCheckout!!.startPurchaseFlow(
//                    sku,
//                    null,
//                    PurchaseListener()
//                )
//            }
//        }
//
//        try {
//            serviceConnection = object : ServiceConnection {
//                override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
//                    mIsBound = true
//                    mService = IInAppBillingService.Stub.asInterface(iBinder)
//                    adRemoveSkuDetailsbefore
//                    adRemoveSkuDetails
//                    adRemoveSkuDetailsmonth
//                    adRemoveSkuDetailsyear
//
//                }
//
//                override fun onServiceDisconnected(componentName: ComponentName) {}
//            }
//            val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
//            serviceIntent.setPackage("com.android.vending")
//            bindService(serviceIntent, serviceConnection!!, Context.BIND_AUTO_CREATE)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private val adRemoveSkuDetailsyear: Unit
//        get() {
//            val thread = Thread(Runnable {
//                try {
//                    val itemList = ArrayList<String>()
//                    itemList.add(resources.getString(R.string.in_app_ads_Year))
//                    val bundle1 = Bundle()
//                    bundle1.putStringArrayList("ITEM_ID_LIST", itemList)
//                    val bundle =
//                        mService!!.getSkuDetails(3, packageName, SUBSCRIPTION, bundle1)
//                    if (bundle.getInt("RESPONSE_CODE") == 0) {
//                        val stringArrayList: ArrayList<*>? =
//                            bundle.getStringArrayList("DETAILS_LIST")
//                        //   Log.d("purchase,", "purchasre" + jSONObject.getString("price"))
//                        tryOrNull {
//                            for (i in stringArrayList!!.indices) {
//                                try {
//                                    val jSONObject = JSONObject(stringArrayList[i] as String)
//                                    Log.d("purchase,", "purchasre" + stringArrayList)
//
//                                    val priceTemp = jSONObject.getString("price").trim { it <= ' ' }
//                                    val price = StringBuilder()
//                                    for (ii in 0 until priceTemp.length) {
//                                        val ch = priceTemp[ii]
//                                        if (ch in '0'..'9') {
//                                            price.append(ch)
//                                        } else {
//                                            if (Character.toString(ch)
//                                                    .equals(".", ignoreCase = true)
//                                            ) price.append(ch)
//                                        }
//                                    }
//                                    val productId = jSONObject.getString("productId")
//                                    val currencyCode = jSONObject.getString("price_currency_code")
//                                    val realprice = jSONObject.getString("price")
//                                    runOnUiThread {
//                                        logDebug("countrycode>>>>" + price)
//
//                                        setBoolean(this, Preferences.Country, true)
//
//                                        if (productId.equals(
//                                                resources.getString(R.string.in_app_ads_Year),
//                                                ignoreCase = true
//                                            )
//                                        ) {
//
//                                            if (isPurchased() && getBoolean(
//                                                    this,
//                                                    Preferences.ADSREMOVEDYear
//                                                )
//                                            ) {
//                                                price2.visibility = View.GONE
//                                            } else {
//                                                price2.text = " $realprice/yr"
//                                                price2.visibility = View.VISIBLE
//                                            }
//                                        }
//                                    }
//                                } catch (e: JSONException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }
//                    } else {
//
//                    }
//                } catch (e: RemoteException) {
//                    e.printStackTrace()
//                }
//            })
//            thread.start()
//        }
//
//    private val adRemoveSkuDetailsmonth: Unit
//        private get() {
//            val thread = Thread(Runnable {
//                try {
//                    val itemList = ArrayList<String>()
//                    itemList.add(resources.getString(R.string.in_app_ads_month))
//                    val bundle1 = Bundle()
//                    bundle1.putStringArrayList("ITEM_ID_LIST", itemList)
//                    val bundle =
//                        mService!!.getSkuDetails(3, packageName, SUBSCRIPTION, bundle1)
//                    if (bundle.getInt("RESPONSE_CODE") == 0) {
//                        val stringArrayList: ArrayList<*>? =
//                            bundle.getStringArrayList("DETAILS_LIST")
//                        //   Log.d("purchase,", "purchasre" + jSONObject.getString("price"))
//                        tryOrNull {
//                            for (i in stringArrayList!!.indices) {
//                                try {
//                                    val jSONObject = JSONObject(stringArrayList[i] as String)
//                                    Log.d("purchase,", "purchasre" + jSONObject)
//                                    val priceTemp = jSONObject.getString("price").trim { it <= ' ' }
//                                    val price = StringBuilder()
//                                    for (ii in 0 until priceTemp.length) {
//                                        val ch = priceTemp[ii]
//                                        if (ch in '0'..'9') {
//                                            price.append(ch)
//                                        } else {
//                                            if (Character.toString(ch)
//                                                    .equals(".", ignoreCase = true)
//                                            ) price.append(ch)
//                                        }
//                                    }
//                                    val productId = jSONObject.getString("productId")
//                                    val currencyCode = jSONObject.getString("price_currency_code")
//                                    val realprice = jSONObject.getString("price")
//                                    runOnUiThread {
//                                        logDebug("countrycode>>>>" + price)
//
//                                        setBoolean(this, Preferences.Country, true)
//                                        if (productId.equals(
//                                                resources.getString(R.string.in_app_ads_month),
//                                                ignoreCase = true
//                                            )
//                                        ) {
//
//                                            if (isPurchased() && getBoolean(
//                                                    this,
//                                                    Preferences.ADSREMOVEDMONTH
//                                                )
//                                            ) {
//                                                price1.visibility = View.GONE
//                                            } else {
//                                                price1.text = " $realprice/mo"
//                                                price1.visibility = View.VISIBLE
//                                            }
//                                        }
//                                    }
//                                } catch (e: JSONException) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }
//                    }
//                } catch (e: RemoteException) {
//                    e.printStackTrace()
//                }
//            })
//            thread.start()
//        }
//
//    private val adRemoveSkuDetails: Unit
//        private get() {
//            val thread = Thread(Runnable {
//                try {
//                    val itemList = ArrayList<String>()
//                    itemList.add(resources.getString(R.string.in_app_ads_lifetime))
//                    val bundle1 = Bundle()
//                    bundle1.putStringArrayList("ITEM_ID_LIST", itemList)
//                    val bundle = mService!!.getSkuDetails(3, packageName, IN_APP, bundle1)
//                    if (bundle.getInt("RESPONSE_CODE") == 0) {
//                        val stringArrayList: ArrayList<*>? =
//                            bundle.getStringArrayList("DETAILS_LIST")
//                        for (i in stringArrayList!!.indices) {
//                            try {
//
//                                val jSONObject = JSONObject(stringArrayList[i] as String)
//                                val priceTemp = jSONObject.getString("price").trim { it <= ' ' }
//                                val price = StringBuilder()
//                                logDebug("countrycode>>>>" + jSONObject.getString("price"))
//                                for (ii in 0 until priceTemp.length) {
//                                    val ch = priceTemp[ii]
//                                    if (ch in '0'..'9') {
//                                        price.append(ch)
//                                    } else {
//                                        if (Character.toString(ch)
//                                                .equals(".", ignoreCase = true)
//                                        ) price.append(ch)
//                                    }
//                                }
//                                val realprice = jSONObject.getString("price")
//                                val productId = jSONObject.getString("productId")
//                                val currencyCode = jSONObject.getString("price_currency_code")
//                                runOnUiThread {
//                                    logDebug("countrycode>>>>" + currencyCode + ">" + price)
//                                    if (productId.equals(
//                                            resources.getString(R.string.in_app_ads_lifetime),
//                                            ignoreCase = true
//                                        )
//                                    ) {
//
//                                        if (isPurchased() && getBoolean(
//                                                this,
//                                                Preferences.ADSREMOVEDLifetime
//                                            )
//                                        ) {
//                                            price3.visibility = View.GONE
//                                        } else {
//                                            price3.text = realprice
//                                            price3.visibility = View.VISIBLE
//                                        }
//
//                                    }
//                                }
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//                } catch (e: RemoteException) {
//                    e.printStackTrace()
//                }
//            })
//            thread.start()
//        }
//
//    private val adRemoveSkuDetailsbefore: Unit
//        private get() {
//            val thread = Thread(Runnable {
//                try {
//                    val itemList = ArrayList<String>()
//                    itemList.add(resources.getString(R.string.in_app_ads))
//                    val bundle1 = Bundle()
//                    bundle1.putStringArrayList("ITEM_ID_LIST", itemList)
//                    val bundle = mService!!.getSkuDetails(3, packageName, IN_APP, bundle1)
//                    if (bundle.getInt("RESPONSE_CODE") == 0) {
//                        val stringArrayList: ArrayList<*>? =
//                            bundle.getStringArrayList("DETAILS_LIST")
//                        for (i in stringArrayList!!.indices) {
//                            try {
//                                val jSONObject = JSONObject(stringArrayList[i] as String)
//                                val priceTemp = jSONObject.getString("price").trim { it <= ' ' }
//                                val price = StringBuilder()
//                                logDebug("countrycode>>>>" + jSONObject.getString("price"))
//                                for (element in priceTemp) {
//                                    val ch = element
//                                    if (ch in '0'..'9') {
//                                        price.append(ch)
//                                    } else {
//                                        if (Character.toString(ch)
//                                                .equals(".", ignoreCase = true)
//                                        ) price.append(ch)
//                                    }
//                                }
//                                val realprice = jSONObject.getString("price")
//                                val productId = jSONObject.getString("productId")
//                                val currencyCode = jSONObject.getString("price_currency_code")
//                                runOnUiThread {
//                                    logDebug("countrycode>>>>" + currencyCode + ">" + price)
//                                    if (productId.equals(
//                                            resources.getString(R.string.in_app_ads_lifetime),
//                                            ignoreCase = true
//                                        )
//                                    ) {
//                                        if (isPurchased() && getBoolean(
//                                                this,
//                                                Preferences.ADSREMOVEDLifetime
//                                            )
//                                        ) {
//                                            price3.visibility = View.GONE
//                                        } else {
//                                            price3.text = realprice
//                                            price3.visibility = View.VISIBLE
//                                        }
//                                    }
//                                }
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//                } catch (e: RemoteException) {
//                    e.printStackTrace()
//                }
//            })
//            thread.start()
//        }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_purchase)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }
        activity = this@InAppPurchaseActivity
        close = findViewById(R.id.close)
        ad_remove_rl_continue = findViewById(R.id.ad_remove_rl_continue)
        val title = findViewById<QkTextView>(R.id.toolbarTitle1)
        ad_remove_tv_month = findViewById(R.id.ad_remove_tv_month)
        price1 = findViewById(R.id.price1)
        ad_remove_tv_year = findViewById(R.id.ad_remove_tv_year)
        price2 = findViewById(R.id.price2)
        ad_remove_tv = findViewById(R.id.ad_remove_tv)
        price3 = findViewById(R.id.price3)
        title.setText(R.string.drawer_removeAds)
        //    title.setTextColor(resources.getColor(R.color.white))
        ad_remove_rl.setOnClickListener(this)
        ad_remove_rl_year.setOnClickListener(this)
        ad_remove_month.setOnClickListener(this)
        ad_remove_rl_cancel.setOnClickListener(this)
        val errorLayout = findViewById<LinearLayout>(R.id.errorLayout)
        val layoutMain = findViewById<RelativeLayout>(R.id.layoutMain)
        if (isConnected) {
            errorLayout.visibility = View.GONE
            layoutMain.visibility = View.VISIBLE
        } else {
            errorLayout.visibility = View.VISIBLE
            layoutMain.visibility = View.GONE
        }
//        init()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        ad_remove_rl_continue.setOnClickListener {

         /*   if (!isPurchaseFlowOpen) {
                isPurchaseFlowOpen = true
                if (skuItemArrayList != null && skuItemArrayList.size != 0) {
                    var sku: Sku? = null

                    for (sku1 in skuItemArrayList) {
                        if (selected == 0) {
                            if (sku1.id.code.equals(SKUS_IN_APP.get(selected))) {
                                sku = sku1
                                break
                            }
                        } else {
                            if (sku1.id.code.equals(SKUS.get(selected - 1))) {
                                sku = sku1
                                break
                            }
                        }
                    }
                    if (sku != null) {
                        mCheckout!!.startPurchaseFlow(sku, null, PurchaseListener())
                    }
                } else {
                    Toast.makeText(
                        this@InAppPurchaseActivity,
                        "Currently there are no subscription plan available!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
       */

            subscribe(selected)

        }
        close.setOnClickListener {
            onBackPressed()
        }
        restore = findViewById(R.id.restore)
        restore.setOnClickListener {

        Toast.makeText(this,"Restore not Available",Toast.LENGTH_SHORT).show()

        /*Restore()*/ }

        privacy?.setOnClickListener {
            showPrivacyPolicy()
        }

//        setUpBilling()

        initBilling()
        updateUIForProUser(true)
    }

//
//    private fun setUpBilling() {
//        val billing: Billing? = (activity!!.application as QKApplication).getBilling()
//        mCheckout = Checkout.forActivity(this, billing!!)
//        mCheckout!!.start()
//        reloadInventory()
//        reloadInventoryInApp()
//    }


    private fun reloadInventory() {
        val request = Inventory.Request.create()
        request.loadPurchases(ProductTypes.SUBSCRIPTION)
        request.loadSkus(
            ProductTypes.SUBSCRIPTION,
            SKUS
        )
        mCheckout!!.loadInventory(request) { products: Products ->
            mSkuDetailsList = products
            val product = products[ProductTypes.SUBSCRIPTION]
            println("SKU LIST : " + products[ProductTypes.SUBSCRIPTION])
            println("SKU LIST : " + product.skus.size)
            for (sku in product.skus) {
                skuItemArrayList.add(sku)
            }
        }
    }



    private fun reloadInventoryInApp() {
        val request = Inventory.Request.create()
        request.loadPurchases(IN_APP)
        request.loadSkus(IN_APP, SKUS_IN_APP)
        mCheckout!!.loadInventory(request) { products: Products ->
            mSkuDetailsList = products
            val product = products[IN_APP]
            println("SKU LIST : " + products[IN_APP])
            println("SKU LIST : " + product.skus.size)
            for (sku in product.skus) {
                skuItemArrayList.add(sku)
            }
        }
    }



    fun showPrivacyPolicy() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.privacy_policy_link))
                )
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.privacy_policy_link))
                    )
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e2: Exception) {
                Toast.makeText(this, "Unable to find ", Toast.LENGTH_LONG).show()
            }
        }
    }




    private val isConnected: Boolean
        get() {
//            val connectivityManager =
//                (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
//            return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected

            var result =false
            CheckInternet().check(){
                    connected ->
                if (connected){
                    result = true
                }else{
                    result = false
                }
            }
            return result
        }


    override fun onBackPressed() {
        finish()
    }

    override fun onClick(v: View) {

        if (v.id == R.id.ad_remove_rl) {
            if (isConnected) {
                selected = 0
                updateUI()
            } else {
                Toast.makeText(this, "No internet connection !", Toast.LENGTH_SHORT).show()
            }
        } else if (v.id == R.id.ad_remove_month) {
            if (isConnected) {
                selected = 1
                updateUI()
            } else {
                Toast.makeText(this, "No internet connection !", Toast.LENGTH_SHORT).show()
            }
        } else if (v.id == R.id.ad_remove_rl_year) {
            if (isConnected) {
                selected = 2
                updateUI()
            } else {
                Toast.makeText(this, "No internet connection !", Toast.LENGTH_SHORT).show()
            }
        } else if (v.id == R.id.ad_remove_rl_cancel) {
            if (isConnected) {
                AppUtils.showSubscriptions(this, SKUS[0])
            } else {
                Toast.makeText(this, "No internet connection !", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun updateUI() {
        ad_remove_rl.setBackgroundResource(R.drawable.ad_remove)
        ad_remove_month.setBackgroundResource(R.drawable.ad_remove)
        ad_remove_year_child.setBackgroundResource(R.drawable.ad_remove)

        ad_remove_tv_month.setTextColor(Color.parseColor("#9E9C9C"))
        price1.setTextColor(Color.parseColor("#9E9C9C"))

        ad_remove_tv_year.setTextColor(Color.parseColor("#9E9C9C"))
        price2.setTextColor(Color.parseColor("#9E9C9C"))

        ad_remove_tv.setTextColor(Color.parseColor("#9E9C9C"))
        price3.setTextColor(Color.parseColor("#9E9C9C"))

        dot1.visibility = View.INVISIBLE
        dot2.visibility = View.INVISIBLE
        dot3.visibility = View.INVISIBLE
        subscription_renew_details.visibility = View.INVISIBLE

        Log.i("TAG", "updateUI: currentPurchased :- $currentPurchased")

        when (selected) {
            0 -> { // lifetime
                dot3.setVisible(true)
                ad_remove_tv.setTextColor(Color.BLACK)
                price3.setTextColor(Color.BLACK)
                ad_remove_rl.setBackgroundResource(R.drawable.ad_selector)
                appIdslifetime = activity!!.resources.getString(R.string.in_app_ads_lifetime)

                if (currentPurchased.trim()
                        .isNotEmpty() && currentPurchased == Preferences.ADSREMOVEDLifetime
                ) {
                    ad_remove_rl_continue.visibility = View.GONE
                    ad_remove_rl_cancel.visibility = View.GONE
                } else {
                    ad_remove_rl_continue.visibility = View.VISIBLE
                    ad_remove_rl_cancel.visibility = View.GONE
                }
            }
            1 -> { // year
                dot1.setVisible(true)
                subscription_renew_details.setVisible(true)
                ad_remove_tv_month.setTextColor(Color.BLACK)
                price1.setTextColor(Color.BLACK)
                ad_remove_month.setBackgroundResource(R.drawable.ad_selector)
                appIdsmonth = activity!!.resources.getString(R.string.in_app_ads_Year)

                if (currentPurchased.trim()
                        .isNotEmpty() && currentPurchased == Preferences.ADSREMOVEDMONTH
                ) {
                    ad_remove_rl_continue.visibility = View.GONE
                    ad_remove_rl_cancel.visibility = View.VISIBLE
                } else {
                    ad_remove_rl_continue.visibility = View.VISIBLE
                    ad_remove_rl_cancel.visibility = View.GONE
                }

            }
            2 -> { // month
                dot2.setVisible(true)
                subscription_renew_details.setVisible(true)

                ad_remove_tv_year.setTextColor(Color.BLACK)
                price2.setTextColor(Color.BLACK)
                ad_remove_year_child.setBackgroundResource(R.drawable.ad_selector)
                appIdslyear = activity!!.resources.getString(R.string.in_app_ads_month)

                if (currentPurchased.trim()
                        .isNotEmpty() && currentPurchased == Preferences.ADSREMOVEDYear
                ) {
                    ad_remove_rl_continue.visibility = View.GONE
                    ad_remove_rl_cancel.visibility = View.VISIBLE
                } else {
                    ad_remove_rl_continue.visibility = View.VISIBLE
                    ad_remove_rl_cancel.visibility = View.GONE
                }
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log("onActivityResult($requestCode,$resultCode,$data")
        mCheckout!!.onActivityResult(requestCode, resultCode, data)

        isPurchaseFlowOpen = false
    }




    private fun log(msg: String) {
    }



    inner class PurchaseListener : EmptyRequestListener<Purchase>() {
        override fun onSuccess(purchase: Purchase) {

            Log.i("TAG", "onSuccess: purchase :- $purchase")
            updatePrefBoolean(true)

            when (purchase.sku) {
                SKUS[0] -> {
                    setBoolean(this@InAppPurchaseActivity, Preferences.ADSREMOVEDMONTH, true)
                }
                SKUS[1] -> {
                    setBoolean(this@InAppPurchaseActivity, Preferences.ADSREMOVEDYear, true)
                }
                SKUS_IN_APP[0] -> {
                    setBoolean(this@InAppPurchaseActivity, Preferences.ADSREMOVEDLifetime, true)
                }
            }

            updateUIForProUser(false)
        }

        override fun onError(response: Int, e: java.lang.Exception) {
            if (response == ResponseCodes.ITEM_ALREADY_OWNED) {
                Toast.makeText(
                    this@InAppPurchaseActivity,
                    "You have already paid for this subscription plan!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@InAppPurchaseActivity,
                    "Failed to purchase! Please try again later!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }




    private fun updateUIForProUser(isFromOnCreate: Boolean) {

        fire.visibility = View.VISIBLE
        if (isPurchased()) {

            remove_txt.setText(R.string.pro_user)

            if (getBoolean(this, Preferences.ADSREMOVEDLifetime)) {
                restore.visibility = View.INVISIBLE
                currentPurchased = Preferences.ADSREMOVEDLifetime

                ad_remove_rl_year.visibility = View.GONE
                ad_remove_month.visibility = View.GONE
                ad_remove_rl.visibility = View.VISIBLE

                ad_remove_rl_continue.visibility = View.GONE
                ad_remove_rl_cancel.visibility = View.GONE
                subscription_renew_details.visibility = View.INVISIBLE

                price3.visibility = View.GONE
                lifeTimePurchased.visibility = View.VISIBLE

                price2.visibility = View.VISIBLE
                oneYearPurchased.visibility = View.GONE

                price1.visibility = View.VISIBLE
                oneMonthPurchased.visibility = View.GONE

                if (isFromOnCreate) {
                    ad_remove_rl.performClick()
                }
            } else if (getBoolean(this, Preferences.ADSREMOVEDYear)) {
                restore.visibility = View.INVISIBLE
                currentPurchased = Preferences.ADSREMOVEDYear

                fire.visibility = View.GONE

                ad_remove_rl_year.visibility = View.VISIBLE
                ad_remove_month.visibility = View.GONE
                ad_remove_rl.visibility = View.GONE

                ad_remove_rl_continue.visibility = View.GONE
                ad_remove_rl_cancel.visibility = View.VISIBLE
                subscription_renew_details.visibility = View.VISIBLE

                price3.visibility = View.VISIBLE
                lifeTimePurchased.visibility = View.GONE

                price2.visibility = View.GONE
                oneYearPurchased.visibility = View.VISIBLE

                price1.visibility = View.VISIBLE
                oneMonthPurchased.visibility = View.GONE

                if (isFromOnCreate) {
                    ad_remove_rl_year.performClick()
                }
            } else if (getBoolean(this, Preferences.ADSREMOVEDMONTH)) {
                restore.visibility = View.INVISIBLE
                currentPurchased = Preferences.ADSREMOVEDMONTH

                ad_remove_rl_year.visibility = View.GONE
                ad_remove_month.visibility = View.VISIBLE
                ad_remove_rl.visibility = View.GONE

                ad_remove_rl_continue.visibility = View.GONE
                ad_remove_rl_cancel.visibility = View.VISIBLE
                subscription_renew_details.visibility = View.VISIBLE

                price3.visibility = View.VISIBLE
                lifeTimePurchased.visibility = View.GONE

                price2.visibility = View.VISIBLE
                oneYearPurchased.visibility = View.GONE

                price1.visibility = View.GONE
                oneMonthPurchased.visibility = View.VISIBLE

                if (isFromOnCreate) {
                    ad_remove_month.performClick()
                }
            } else {

                if (isFromOnCreate) {
                    ad_remove_rl_year.performClick()
                }

                updatePrefBoolean(false)
                userHasNotPurchased()
            }

        } else {

            if (isFromOnCreate) {
                ad_remove_rl_year.performClick()
            }

            userHasNotPurchased()
        }


    }




    private fun userHasNotPurchased() {

//        restore.visibility = View.VISIBLE

        ad_remove_rl_year.visibility = View.VISIBLE
        ad_remove_month.visibility = View.VISIBLE
        ad_remove_rl.visibility = View.VISIBLE

        remove_txt.setText(R.string.unlimited_access_user)

        ad_remove_rl_continue.visibility = View.VISIBLE
        ad_remove_rl_cancel.visibility = View.GONE

        updateUI()

        price3.visibility = View.VISIBLE
        lifeTimePurchased.visibility = View.GONE

        price2.visibility = View.VISIBLE
        oneYearPurchased.visibility = View.GONE

        price1.visibility = View.VISIBLE
        oneMonthPurchased.visibility = View.GONE

        currentPurchased = ""
    }



    private fun updatePrefBoolean(isPurchased: Boolean) {
        setBoolean(activity!!, Preferences.ADSREMOVED, isPurchased)
    }





    private  fun initBilling() {


        billingConnector = BillingConnector(this,getString(R.string.base64key))
//            .setConsumableIds(nonConsumableIds)
            .setNonConsumableIds(SKUS_IN_APP)
            .setSubscriptionIds(SKUs)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()



        billingConnector?.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetails: List<ProductInfo>) {
                /*Provides a list with fetched products*/

                if(skuDetails.isNotEmpty())
                {
                    for (item in skuDetails)
                    {

                        if(item.product.contains("_49.99")){

                            for (priceList in item.subscriptionOfferDetails){
                                for (price in priceList.pricingPhases)
                                     price3.text = price.formattedPrice+" "+price.priceCurrencyCode
                            }

                        }

                        if(item.product.contains("_9.99")){
                            for (priceList in item.subscriptionOfferDetails){
                                for (price in priceList.pricingPhases)
                                    price3.text = price.formattedPrice+" "+price.priceCurrencyCode
                            }
                        }

                        if(item.product.contains("_29.99")){
                            for (priceList in item.subscriptionOfferDetails){
                                for (price in priceList.pricingPhases)
                                    price3.text = price.formattedPrice+" "+price.priceCurrencyCode
                            }
                        }
                        showLogs(item.product)
                    }
                }
            }

            override fun onPurchasedProductsFetched(
                skuType: ProductType,
                purchases: MutableList<PurchaseInfo>
            ) {

                /*Provides a list with fetched purchased products*/

                if(purchases.isEmpty())
                {
                    updatePrefBoolean(false)

                    Log.e("Tag","inappPurchase>>>notpurchase")
                }
                else
                {
                    updatePrefBoolean(true)
                    Log.e("Tag","inappPurchase>>>purchase")


                }

                updateUIForProUser(false)
//                startActivity(Intent(this@InAppPurchaseActivity,ResultActivity::class.java))
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {

                if(purchases.isEmpty())
                {
                    updatePrefBoolean(false)
                }
                else
                {

                    if(purchases[0].product ==SKUS_IN_APP[0]){

                        setBoolean(this@InAppPurchaseActivity,Preferences.ADSREMOVEDLifetime,  true)

                        MaxMainAdsManger.destroyBanner()
                    }
                    if(purchases[0].product ==  SKUs[1]){

                        setBoolean(this@InAppPurchaseActivity, Preferences.ADSREMOVEDYear, true)
                        MaxMainAdsManger.destroyBanner()
                    }
                    if(purchases[0].product ==  SKUs[0]){

                        setBoolean(this@InAppPurchaseActivity, Preferences.ADSREMOVEDMONTH, true)
                        MaxMainAdsManger.destroyBanner()
                    }


                    updatePrefBoolean(true)

                }
//                startActivity(Intent(this@InAppPurchaseActivity,ResultActivity::class.java))

            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {

                /*Callback after a purchase is acknowledged*/

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                /*Callback after a purchase is consumed*/

                /*
                 * CONSUMABLE products entitlement can be granted either here or in onProductsPurchased
                 * */
            }

            override fun onBillingError(
                billingConnector: BillingConnector,
                response: BillingResponse
            ) {

                showLogs(response.debugMessage)
//                Toast.makeText(this@SubscriptionActivity,"${response.debugMessage}",Toast.LENGTH_LONG).show()
                /*Callback after an error occurs*/
                when (response.errorType) {
                    ErrorType.CLIENT_NOT_READY -> {
                    }
                    ErrorType.CLIENT_DISCONNECTED -> {
                    }

                    ErrorType.CONSUME_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_WARNING -> {
                    }
                    ErrorType.FETCH_PURCHASED_PRODUCTS_ERROR -> {
                    }
                    ErrorType.BILLING_ERROR -> {
                    }
                    ErrorType.USER_CANCELED -> {
                    }
                    ErrorType.SERVICE_UNAVAILABLE -> {
                    }
                    ErrorType.BILLING_UNAVAILABLE -> {
                    }
                    ErrorType.ITEM_UNAVAILABLE -> {
                    }
                    ErrorType.DEVELOPER_ERROR -> {
                    }
                    ErrorType.ERROR -> {
                    }
                    ErrorType.ITEM_ALREADY_OWNED -> {
                    }
                    ErrorType.ITEM_NOT_OWNED -> {
                    }

                    else -> {}
                }
            }
        })

    }


    private fun showLogs(msg:String){

        Log.e("billing ","=============================\n")
        Log.e("billing ",msg)
        Log.e("billing ","\n=============================")

    }


    fun subscribe(position:Int) {

        if(position==0){
            billingConnector?.subscribe(this,SKUS_IN_APP.get(0))

        }
        if(position==1){
            billingConnector?.subscribe(this,SKUs.get(1))

        }
        if(position==2){
            billingConnector?.subscribe(this,SKUs.get(0))

        }
    }






}