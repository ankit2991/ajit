package com.gpsnavigation.maps.gpsroutefinder.routemap.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.android.gpslocation.utils.UrlOpener
import com.bumptech.glide.Glide
import com.google.android.ump.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.MainScreenAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.MyConstant
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityMainBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.RemoveAdsPopupBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxUtils
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.MainScreenItemModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PromoCompaign
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.XpromoCampaignsItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*
import com.notifications.firebase.services.MessagingService

import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList


class MainActivity : BaseActivity(), View.OnClickListener {
    var flag = false
    val mainActivityViewModel: MainActivityViewModel by viewModel()
    private var subscriptionScreenIntervalOnRFBtn = 0
    var isUserSubscribed = false

    private var alertDialog: AlertDialog? = null
    private var shown: Boolean = false
    private var xpromoCampaignsItem: XpromoCampaignsItem? = null

    var GPS124_current_campaign: String = ""
    lateinit var promoCompaignmodel: PromoCompaign


    var proButtonAnim: Animation? = null
    private val consentForm: ConsentForm? = null
    private var consentInformation: ConsentInformation? = null
    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/
        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()


        setInset()

        (application as AppClass).isSplash = false

//        MobileAds.openAdInspector(this) {
//            Log.d("tagggg", it?.message.toString())
//            // Error will be non-null if ad inspector closed due to an error.
//        }
        if (MaxUtils.isNetworkConnected(this)){
            MessagingService.subscribeToTopic(this)
        }
//        appBillingClient = AppBillingClient()
        try {
            GPS124_current_campaign =
                getRemoteconfig()?.getString("GPS124_current_campaign").toString()
            val jsondata = getRemoteconfig()?.getString("GPS124_xpromo_campaigns").toString()
            if (jsondata.isNotEmpty()) {
                promoCompaignmodel = Gson().fromJson(jsondata, PromoCompaign::class.java)
                if (GPS124_current_campaign.isNotEmpty() && promoCompaignmodel.xpromoCampaigns?.isNotEmpty() == true) {
                    for (campaign in promoCompaignmodel.xpromoCampaigns!!) {
                        if (campaign.campaignName == GPS124_current_campaign) {
                            xpromoCampaignsItem = campaign
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (TinyDB.getInstance(this)
                .getString(SELECTED_LANGUAGE, "") == ""
        ) {
            LocaleHelper1.setAppLocale(this, "en")
        } else {
            LocaleHelper1.setAppLocale(
                this,
                TinyDB.getInstance(this)
                    .getString(SELECTED_LANGUAGE, "")
            )
        }

        binding.toolbar.title.text = getString(R.string.main_activity_title)
        binding.toolbar.navigationButton.setImageResource(R.drawable.ic_settings)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        MaxAdManager.showNativeAds(this,  binding.nativeAdsContainer, FrameLayout.LayoutParams.WRAP_CONTENT)

        setObservers()
        loadDialog()

        proButtonAnim = AnimationUtils.loadAnimation(
            this,
            R.anim.blink
        )



        if ((application as AppClass).onfirstsessionPromo) {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    onShowGeoGuessorDialog()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 2000)
        }

        val adapter =
            MainScreenAdapter(prepareActionsList()) { mainScreenItemModel: MainScreenItemModel ->

                if ((application as AppClass).onfirstsessionPromo) {
                    navigateToNext(mainScreenItemModel)
                } else {
                    if (xpromoCampaignsItem != null) {
                        onButtonShowPopupWindowClick(xpromoCampaignsItem, mainScreenItemModel)
                    } else {
                        navigateToNext(mainScreenItemModel)
                    }
                }
            }

        binding.rvMainScreen.layoutManager = getGridLayoutManager()
        binding.rvMainScreen.addItemDecoration(
            EqualSpacingItemDecoration(
                16.dpToPx,
                20.dpToPx
            )
        )
        binding.rvMainScreen.adapter = adapter
    }




    private fun getGridLayoutManager(): RecyclerView.LayoutManager {
        val glm = GridLayoutManager(this, 6, GridLayoutManager.VERTICAL, false)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(i: Int): Int {
                return if (i in listOf(0, 1)) 3
                else 2
            }
        }
        return glm
    }

    private fun navigateToNext(mainScreenItemModel: MainScreenItemModel) {
            Log.e("clickid",">"+mainScreenItemModel.id)
            when (mainScreenItemModel.id) {


                15L->{
                    openSubscriptionPaywall()
                }

                10L -> {
                    openSubscriptionPaywall()
                }

                3L -> {
                    clickItem( VoiceSearchActivity::class.java)

                    FirebaseAnalytics.getInstance(this).logEvent(VOICE_NAV_CLICKS, null)


                }

                4L -> {
                    clickItem(PopularPlacesActivity::class.java)
                    FirebaseAnalytics.getInstance(this).logEvent(NEARBY_CLICKS, null)

                }

                1L -> {
                    clickItem(RFMainActivity::class.java)
//                        if (isUserSubscribed) {
                    /*if (true) {
                        startActivity(Intent(this@MainActivity, RFMainActivity::class.java))
                    } else {
                        val lastClickTime = TinyDB.getInstance(this).getLong("lastClickTime", 0)
                        if (lastClickTime > 0) {
                            if (subscriptionScreenIntervalOnRFBtn == 0) {
                                startActivity(Intent(this, RFMainActivity::class.java))
                            } else {
                                val currentDate = java.util.Calendar.getInstance().time
                                val millis = currentDate.time - lastClickTime
                                val hours = (millis / (1000 * 60 * 60)).toInt()
//                                    val mins: Int = (millis / (1000 * 60) % 60).toInt()
                                when (subscriptionScreenIntervalOnRFBtn) {
                                    1 -> {
                                        if (hours >= 24)
                                            startRFWithSubscription(currentDate.time)
                                        else
                                            startActivity(
                                                Intent(
                                                    this,
                                                    RFMainActivity::class.java
                                                )
                                            )
                                    }

                                    2 -> {
                                        if (hours >= 12)
                                            startRFWithSubscription(currentDate.time)
                                        else
                                            startActivity(
                                                Intent(
                                                    this,
                                                    RFMainActivity::class.java
                                                )
                                            )
                                    }
                                }
                            }
                        } else {
                            val currentDate = java.util.Calendar.getInstance().time
                            startRFWithSubscription(currentDate.time)
                            // FirebaseAnalytics.getInstance(this).logEvent(ROUTE_FINDER_CLICKS, null)
                        }
                    }*/
                }

                2L -> {
                    //here is the click of Geo Guessing item
                    /* Adjust Event */
                    ////////////////////////////////////////////////////////////////////////////////////
                    val adjustEvent = AdjustEvent("lw9u2v")
                    adjustEvent.addCallbackParameter(GEO_GUESSING_START, "Date_${CurrentDateUtil.getCurrentDate2()}_PlacementScreen_MainMenu")
                    adjustEvent.setCallbackId(GEO_GUESSING_START);
                    Adjust.trackEvent(adjustEvent)
                    Log.e("AdjustEvent","The event Id:lw9u2v Name:$GEO_GUESSING_START Date:${CurrentDateUtil.getCurrentDate2()} PlacementScreen:MainMenu has been sent to Adjust")

                    ////////////////////////////////////////////////////////////////////////////////////

                    //firebase event
                    FirebaseAnalytics.getInstance(this).logEvent(GEO_GUESSING_START, null)
//            Log.e("comet", "GEO_GUESSING_START->");
                    /////////////
                    clickItem(GeoGussingActivity::class.java)
                }

                6L -> {
                    clickItem(SatelliteViewActivity::class.java)

                    FirebaseAnalytics.getInstance(this).logEvent(SATELLITE_CLICKS, null)

                }

                5L -> {
                    clickItem(LiveTrafficActivity::class.java)

                    FirebaseAnalytics.getInstance(this).logEvent(LIVE_TRAFFIC_CLICKS, null)
                }

                7L -> {
                    FirebaseAnalytics.getInstance(this).logEvent(LOCAL_TRANSPORT, null)
                    openNearBy(this, "Local Transport")
                }

                13L -> {
                    clickItem( MyLocationActivity::class.java)
                    FirebaseAnalytics.getInstance(this).logEvent(MY_LOCATION_CLICKS, null)

                }
            }
    }

    fun onButtonShowPopupWindowClick(
        campaign: XpromoCampaignsItem?,
        mainScreenItemModel: MainScreenItemModel
    ) {
        flag = false
        // inflate the layout of the popup window
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.promo_dialog, null)
        val img_steps = popupView.findViewById<ImageView>(R.id.img_promo)
        val reletiveLayout = popupView.findViewById<RelativeLayout>(R.id.reletiveLayout)
        val btnContinue = popupView.findViewById<Button>(R.id.btnContinue)
        Glide.with(this).load(campaign?.promoImageUrl ?: "").into(img_steps)
        btnContinue.text = campaign?.buttonText ?: ""

        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        //        View v= popupWindow.getContentView();
        dimBehind(popupWindow)
        Handler(Looper.getMainLooper()).postDelayed({
            // dismiss the popup window when touched
            popupView.setOnTouchListener { v, event ->
                flag = true
                popupWindow.dismiss()
                navigateToNext(mainScreenItemModel)
                true
            }
        }, 4000)
        reletiveLayout.setOnClickListener {
            UrlOpener.browser(
                this@MainActivity,
                campaign?.storeUrl ?: ""
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!flag) {
                popupWindow.dismiss()
                navigateToNext(mainScreenItemModel)
            }
        }, 7000)


        btnContinue.setOnClickListener {
            UrlOpener.browser(
                this@MainActivity,
                campaign?.storeUrl ?: ""
            )
        }
    }


    fun onShowGeoGuessorDialog(
    ) {
        // inflate the layout of the popup window
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.geo_guessor_dialog, null)
        val img_steps = popupView.findViewById<ImageView>(R.id.img_promo)
        val img_cross = popupView.findViewById<ImageView>(R.id.img_cross)
        val reletiveLayout = popupView.findViewById<RelativeLayout>(R.id.reletiveLayout)
        val btnContinue = popupView.findViewById<Button>(R.id.btnContinue)
        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
        //        View v= popupWindow.getContentView();
        dimBehind(popupWindow)
        (application as AppClass).onfirstsessionPromo = false
        img_cross.setOnClickListener {
            popupWindow.dismiss()
        }


        btnContinue.setOnClickListener {
            popupWindow.dismiss()
            //here is the click of Geo Guessing item
            /* Adjust Event */
            ////////////////////////////////////////////////////////////////////////////////////
            val adjustEvent = AdjustEvent("lw9u2v")
            adjustEvent.addCallbackParameter(GEO_GUESSING_START, "Date_${CurrentDateUtil.getCurrentDate2()}_PlacementScreen_popup")
            adjustEvent.setCallbackId(GEO_GUESSING_START);
            Adjust.trackEvent(adjustEvent)
            Log.e("AdjustEvent","The event Id:lw9u2v Name:$GEO_GUESSING_START Date:${CurrentDateUtil.getCurrentDate2()} PlacementScreen:popup has been sent to Adjust")
            ////////////////////////////////////////////////////////////////////////////////////

            //firebase event
            FirebaseAnalytics.getInstance(this).logEvent(GEO_GUESSING_START, null)
//            Log.e("comet", "GEO_GUESSING_START->");
            /////////////
            clickItem(GeoGussingActivity::class.java)
        }
        img_steps.setOnClickListener {
            popupWindow.dismiss()
            //here is the click of Geo Guessing item
            /* Adjust Event */
            ////////////////////////////////////////////////////////////////////////////////////
            val adjustEvent = AdjustEvent("lw9u2v")
            adjustEvent.addCallbackParameter(GEO_GUESSING_START, "Date_${CurrentDateUtil.getCurrentDate2()}_PlacementScreen_popup")
            adjustEvent.setCallbackId(GEO_GUESSING_START);
            Adjust.trackEvent(adjustEvent)
            Log.e("AdjustEvent","The event Id:lw9u2v Name:$GEO_GUESSING_START Date:${CurrentDateUtil.getCurrentDate2()} PlacementScreen:popup has been sent to Adjust")
            ////////////////////////////////////////////////////////////////////////////////////

            //firebase event
            FirebaseAnalytics.getInstance(this).logEvent(GEO_GUESSING_START, null)
//            Log.e("comet", "GEO_GUESSING_START->");
            /////////////
            clickItem(GeoGussingActivity::class.java)
        }
    }


    private fun dimBehind(popupWindow: PopupWindow) {
        val container: View
        container = if (popupWindow.background == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                popupWindow.contentView.parent as View
            } else {
                popupWindow.contentView
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                popupWindow.contentView.parent.parent as View
            } else {
                popupWindow.contentView.parent as View
            }
        }
        val context = popupWindow.contentView.context
        val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.7f
        wm.updateViewLayout(container, p)
    }

    private fun startRFWithSubscription(time: Long) {
        TinyDB.getInstance(this).putLong("lastClickTime", time)
//        val it = Intent(this@MainActivity, NewPremiumActivity::class.java)
//        it.putExtra("from", true)

        val intent = Intent(this,PaywallUi::class.java)
        intent.putExtra("paywallType","Default")

        startActivities(
            arrayOf(
                Intent(
                    this@MainActivity,
                    RFMainActivity::class.java
                ),
//                it
                intent
            )
        )
    }

    private fun setObservers() {
        mainActivityViewModel.isUserSubscribed().observe(this) {
            isUserSubscribed = it
            if (isUserSubscribed) {
                proButtonAnim?.cancel()

                binding.nativeAdsContainer.isVisible = false
                binding.nativeAdsContainer.tag = null

            } else {
                proButtonAnim?.start()

                MaxAdManager.showNativeAds(this, binding.nativeAdsContainer, FrameLayout.LayoutParams.WRAP_CONTENT)
            }
            binding.toolbar.premiumButton.isInvisible = isUserSubscribed
        }

        mainActivityViewModel.subscription_screen_interval_on_rf_button()
            .observe(this, androidx.lifecycle.Observer {
                subscriptionScreenIntervalOnRFBtn = it
            })
    }

    private fun showRemoveAdsDialog() {
        val removeAdsBinding:RemoveAdsPopupBinding = RemoveAdsPopupBinding.inflate(layoutInflater)
        val mDialogView = removeAdsBinding.root
//        val mDialogView = LayoutInflater.from(this).inflate(R.layout.remove_ads_popup, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle(null)
        val mAlertDialog = mBuilder.show()
        TinyDB.getInstance(this)
            .setRemoveAdsPopupCount(TinyDB.getInstance(this).getRemoveAdsPopupCount() + 1)
        shown = true;
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        removeAdsBinding.continueBtn.setOnClickListener {
//            removeAdsSkuDetail?.let {
//                appBillingClient?.purchaseSkuItem(this, it)
//            }
            openSubscriptionPaywall()
            mAlertDialog.dismiss()

        }
    }


    private fun prepareActionsList(): ArrayList<MainScreenItemModel> =
        arrayListOf(
            MainScreenItemModel(
                1,
                getString(R.string.main_activity_navigation_item),
                R.drawable.ic_navigation_new,
                ContextCompat.getDrawable(this, R.drawable.bg_navigation_item)!!
            ),
            MainScreenItemModel(
                4,
                getString(R.string.main_activity_popular_places_item),
                R.drawable.ic_popular_places_new,
                ContextCompat.getDrawable(this, R.drawable.bg_popular_places_item)!!
            ),
            MainScreenItemModel(
                2,
                getString(R.string.main_activity_geo_game_item),
                R.drawable.ic_geo_game_new,
                ColorDrawable(Color.parseColor("#44E9DC"))
            ),
            MainScreenItemModel(
                13,
                getString(R.string.my_location),
                R.drawable.ic_my_location_new,
                ColorDrawable(Color.parseColor("#ADEE5A"))
            ),
            MainScreenItemModel(
                5,
                getString(R.string.live_traffic),
                R.drawable.ic_live_traffic_new,
                ColorDrawable(Color.parseColor("#FFD958"))
            ),
            MainScreenItemModel(
                6,
                getString(R.string.satellite_view),
                R.drawable.ic_satellite_view_new,
                ColorDrawable(Color.parseColor("#0044B0"))
            ),
            MainScreenItemModel(
                3,
                getString(R.string.main_activity_voice_gps_item),
                R.drawable.ic_voice_gps_new,
                ColorDrawable(Color.parseColor("#8000FF"))
            ),
            MainScreenItemModel(
                7,
                getString(R.string.local_transport),
                R.drawable.ic_local_transport_new,
                ColorDrawable(Color.parseColor("#FE8ED8"))
            ),

        )


    fun getDefaultRouteName(): String {
        return "My Route " + java.util.Calendar.getInstance()
            .get(java.util.Calendar.DATE) + "." + (java.util.Calendar.getInstance().get(
            java.util.Calendar.MONTH
        ) + 1) + "." + java.util.Calendar.getInstance().get(
            java.util.Calendar.YEAR
        )
    }

    private fun openNearBy(context: Context, query: String) {
        MyConstant.IS_APPOPEN_BG_IMPLICIT = true
        val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri.toString()))
        intent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(intent)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(context, "Please install Google maps first.", Toast.LENGTH_LONG).show()
        }

    }

    private fun clickItem(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_privacy_policy -> {

                //  UrlUtils.openUrl(this, "https://zedlatino.info/privacy-policy-apps.html")
            }

            R.id.action_list -> {
                // appBillingClient?.purchaseSkuItem(this,OneTimePurchaseItem)
                /* if (bp?.isInitialized!! && bp?.isOneTimePurchaseSupported!!) {
                     bp?.purchase(this, Constants.AD_PRODUCT_ID)
                 } else {
                     Toast.makeText(
                         this,
                         "Service initialization failed.. Please try again!",
                         Toast.LENGTH_SHORT
                     ).show()
                 }*/

            }
        }
        return true
    }

    fun isValidContext(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (context is Activity) {
            val activity = context
            if (activity.isDestroyed || activity.isFinishing) {
                return false
            }
        }
        return true
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        alertDialog?.let {
            it.show()
            it.getButton(DialogInterface.BUTTON_NEGATIVE).text = getString(R.string.no)
            it.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            it.getButton(DialogInterface.BUTTON_POSITIVE).text = getString(R.string.exit)
            it.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        }
    }

    override fun onDestroy() {
         super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }

    private fun loadDialog() {

        if (isFinishing)
            return
        val builder = AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
        builder.setNegativeButton("No") { dialog, _ -> dialog.cancel() }

        builder.setPositiveButton("Exit") { dialog, _ ->
            dialog.cancel()
            finish()
        }
        builder.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish()
                return@OnKeyListener true
            }
            false
        })

        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_back_dialog, null)

        val btn_1 = dialogView.findViewById<RatingBar>(R.id.rating_bar)
        val banner_ad = dialogView.findViewById<LinearLayout>(R.id.fb_banner)
        val rate_us_txt = dialogView.findViewById<TextView>(R.id.rate_us_title_txt)

        if (TinyDB.getInstance(this).getBoolean(IS_RATE_US_SHOW)) {
            btn_1.visibility = View.GONE
            rate_us_txt.visibility = View.GONE
        }
        builder.setView(dialogView)
        alertDialog = builder.create()



        btn_1.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, v, b ->
            if (ratingBar.rating <= 3) {
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", "Pixammatics@gmail.com", null)
                )
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feed back For GPS Navigation")
                startActivity(Intent.createChooser(emailIntent, "Send Email..."))
                alertDialog?.dismiss()
            } else {
                TinyDB.getInstance(this).putBoolean(IS_RATE_US_SHOW, true)
                /* val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                 val intent = Intent(Intent.ACTION_VIEW, uri)
                 startActivity(intent)*/
                alertDialog?.dismiss()

                showRatingDialog()
            }
        }
    }


    override fun onResume() {
        if (Constants.weeklyPrice.isEmpty() && Constants.yearlyPrice.isEmpty()) {
            Constants.weeklyPrice = TinyDB.getInstance(this)
                .getString(Constants.WEEKLY_VALUE)
            Constants.yearlyPrice = TinyDB.getInstance(this)
                .getString(Constants.YEARLY_VALUE)
        }
        if (TinyDB.getInstance(this).removeAdsPopupCount < 2 && TinyDB.getInstance(this)
                .interstitialCount >= 2 && !shown
        ) {
            showRemoveAdsDialog()
        }
        super.onResume()

        binding.toolbar.premiumButton.isInvisible =
            if (TinyDB.getInstance(this).isPremium) {
                binding.nativeAdsContainer.isVisible = false
                binding.nativeAdsContainer.tag = null
                true
            } else {
                MaxAdManager.showNativeAds(this,  binding.nativeAdsContainer, FrameLayout.LayoutParams.WRAP_CONTENT)
                false
            }

        //hideStatusBar()
    }

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this)
    }

    private fun setInset() {
        binding.nativeAdsContainer.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)
            (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = systemBarsInsets.bottom
            }

            insets
        }
    }

    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> clickItem(SettingsActivity::class.java)
            R.id.premium_button -> openSubscriptionPaywall()
        }
    }
}
