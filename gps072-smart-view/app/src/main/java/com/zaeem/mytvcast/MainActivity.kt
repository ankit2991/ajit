package com.zaeem.mytvcast

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ironsource.mediationsdk.IronSource
import com.vincent.filepicker.Constant
import com.vincent.filepicker.activity.AudioPickActivity
import com.vincent.filepicker.activity.ImagePickActivity
import com.vincent.filepicker.activity.VideoPickActivity
import com.zaeem.mytvcast.Utils.*
import com.zaeem.mytvcast.databinding.ActivityMainLayoutBinding
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.PurchaseInfo
import games.moisoni.google_iab.models.SkuInfo
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val LICENSE_KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg+Fwtfyeb6yG7+LGYNMwaWIHK/rNksb5c/s2VJ1VfT8ugbCjaIL/FfmBUfBMAxfuo0fiVje0uSLfKXiLYee+Hsn2iAZpvpSrOf5qtHvSTAS1JFGhyq0qL6FUz1nc41Lzg+kCdUj4dtMfxfSbJ6DPcxUhQf3hCKQF6EW4RuGo/cc9sRsvzn//tramOYpSR9qil8A9FHTBNsq465psFDAPVaWGV4wAKl7A7LZJlgE42KuiGpZQYUKGgQiwZxTsrF34iBA5G/IIQuGn1/auSCWRQZFbrLL6iYO5Loo54KKkAUpDX+ChpVKnCipKBT8pxbT7akiBku1kgapdlc6bK8KvfQIDAQAB"

    private val SKUs = listOf(BuildConfig.APPLICATION_ID + ".premium")
    private var billingConnector: BillingConnector? = null

    private lateinit var binding: ActivityMainLayoutBinding

    private var server: StreamingWebServer? = null
    private var myIp: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdsManager.showBanner(this,binding.bannerContainer)


        initIAB()

        setClickListeners()

        startWebServer()
        if (StreamingManager.getInstance(this).device == null || !StreamingManager.getInstance(this).device.isConnected)
            startActivity(
                Intent(this, ConnectDeviceActivity::class.java)
            )
        registerReceiver(
            streamBroadcast,
            IntentFilter(com.zaeem.mytvcast.Utils.Constant.STREAM_NEW_CONTENT)
        )


    }


    private fun initIAB() {
        billingConnector = BillingConnector(this, LICENSE_KEY)
//            .setConsumableIds(consumableIds)
//            .setNonConsumableIds(SKUs)
            .setSubscriptionIds(SKUs)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()

        billingConnector?.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetails: List<SkuInfo>) {
                /*Provides a list with fetched products*/
            }

            override fun onPurchasedProductsFetched(purchases: List<PurchaseInfo>) {
                /*Provides a list with fetched purchased products*/

                if(purchases.isEmpty())
                {
                    TinyDB.getInstance(this@MainActivity).savePremium(this@MainActivity,false)
                }
                else
                {
                    TinyDB.getInstance(this@MainActivity).savePremium(this@MainActivity,true)

                }
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {

                if(purchases.isEmpty())
                {
                    TinyDB.getInstance(this@MainActivity).savePremium(this@MainActivity,false)
                }
                else
                {
                    TinyDB.getInstance(this@MainActivity).savePremium(this@MainActivity,true)

                }            }

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
                /*Callback after an error occurs*/
                when (response.errorType) {
                    ErrorType.CLIENT_NOT_READY -> {
                    }
                    ErrorType.CLIENT_DISCONNECTED -> {
                    }
                    ErrorType.SKU_NOT_EXIST -> {
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
                }
            }
        })

    }

    private fun setClickListeners() {


      val isPremium=  TinyDB.getInstance(this@MainActivity).isPremium(this)

        binding.apply {

            castIV.setOnClickListener {
                if (TinyDB.getInstance(this@MainActivity).isPremium(this@MainActivity)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivity(Intent(this@MainActivity, MirroringActivity::class.java))
                    } else {
                        notSupported()
                    }
                } else {
                    showPremium()
                }
            }



            searchIV.setOnClickListener {
                if (searchEt.text.toString().isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please write anything!", Toast.LENGTH_LONG)
                        .show()
                } else {

                    WebviewActivity.openWebsite(searchEt.text.toString(), this@MainActivity)
                }
            }

            facebookIV.setOnClickListener {
                if (isPremium)
                {
                    WebviewActivity.openWebsite(WebviewActivity.FACEBOOK, this@MainActivity)

                }
                else{
                    startActivityDefault(PremiumActivity::class.java)
                }

            }
            tedIV.setOnClickListener {
                if (isPremium)
                {
                    WebviewActivity.openWebsite(WebviewActivity.TED, this@MainActivity)

                }
                else{
                    startActivityDefault(PremiumActivity::class.java)
                }

            }
            youtubeIV.setOnClickListener {
                if (isPremium)
                {
                    startActivityDefault(YoutubeActivity::class.java)

                }
                else{
                    startActivityDefault(PremiumActivity::class.java)
                }

            }
            photoCV.setOnClickListener {
                AdsManager.showInter(this@MainActivity,{
                    showImagePick()

                },{
                    showImagePick()

                })

            }
            imageSearchCV.setOnClickListener {
                AdsManager.showInter(this@MainActivity,{
                    startActivityDefault(ImageActivity::class.java)

                },{
                    startActivityDefault(ImageActivity::class.java)

                })

            }
            musicCV.setOnClickListener {
                AdsManager.showInter(this@MainActivity,{
                    showAudioPick()

                },{
                    showAudioPick()

                })
            }
            videoCV.setOnClickListener {
                if (isPremium)
                {
                    showVideoPick()

                }
                else{
                    startActivityDefault(PremiumActivity::class.java)
                }
            }
            dropboxCV.setOnClickListener {
                startActivityDefault(DropBoxActivity::class.java)

            }
            driveCV.setOnClickListener {

                startActivityDefault(GoogleDriveActivity::class.java)
            }
        }

    }

    fun showAudioPick() {
        val intent1 = Intent(this@MainActivity, AudioPickActivity::class.java)
        intent1.putExtra("IsNeedRecorder", true)
        intent1.putExtra(Constant.MAX_NUMBER, 1)
        intent1.putExtra("isNeedFolderList", true)
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_AUDIO)
    }

    fun showVideoPick() {
        val intent1 = Intent(this@MainActivity, VideoPickActivity::class.java)
        intent1.putExtra("IsNeedCamera", true)
        intent1.putExtra(Constant.MAX_NUMBER, 1)
        intent1.putExtra("isNeedFolderList", true)
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_VIDEO)
    }

    fun showImagePick() {

        val intent1 = Intent(this@MainActivity, ImagePickActivity::class.java)
        intent1.putExtra("IsNeedCamera", true)
        intent1.putExtra(Constant.MAX_NUMBER, 1)
        intent1.putExtra("isNeedFolderList", true)
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_IMAGE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(streamBroadcast)
    }

    var streamBroadcast: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!StreamingManager.getInstance(this@MainActivity).isDeviceConnected) {
                startActivity(Intent(this@MainActivity, ConnectDeviceActivity::class.java))
                return
            }
            if (intent.extras!!.getString("fileType") == "image") {
                val path = intent.extras!!.getString("fileURL")
                val filename = path!!.substring(path.lastIndexOf("/") + 1)
                server!!.addFileForPath(filename, path)
                StreamingManager.getInstance(this@MainActivity).showContent(
                    intent.extras!!.getString("fileName"),
                    StreamingWebServer.getMimeType(path),
                    "http://$myIp:8080/$filename"
                )
            } else if (intent.extras!!.getString("fileType") == "video") {
                val path = intent.extras!!.getString("fileURL")
                val filename = path!!.substring(path.lastIndexOf("/") + 1)
                server!!.addFileForPath(filename, path)
                StreamingManager.getInstance(this@MainActivity).playMedia(
                    intent.extras!!.getString("fileName"),
                    StreamingWebServer.getMimeType(path),
                    "http://$myIp:8080/$filename"
                )
            } else if (intent.extras!!.getString("fileType") == "audio") {
                val path = intent.extras!!.getString("fileURL")
                val filename = path!!.substring(path.lastIndexOf("/") + 1)
                server!!.addFileForPath(filename, path)
                StreamingManager.getInstance(this@MainActivity).playAudio(
                    intent.extras!!.getString("fileName"),
                    StreamingWebServer.getMimeType(path),
                    "http://$myIp:8080/$filename"
                )
            }
        }
    }

    fun startWebServer() {
        try {
            myIp = Utils.getIPAddress(true)
            server = StreamingWebServer()
            server!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopWebServer() {
        try {
            if (server != null && server!!.isAlive) server!!.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        showBackDialog()
    }

    private fun openURL(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun shareAction() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "My TV Cast allows you to mirror phone screen to your Chromecast. https://play.google.com/store/apps/details?id=com.sensustech.mytvcast"
        )
        val intent = Intent.createChooser(shareIntent, "Share")
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.screen_mirroring -> {
                if (StreamingManager.getInstance(this).device == null || !StreamingManager.getInstance(
                        this
                    ).device.isConnected
                ) {
                    startActivity(Intent(this, ConnectDeviceActivity::class.java))
                } else {
                    confirmDisconnect()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        castButton = menu.findItem(R.id.screen_mirroring);
        if (StreamingManager.getInstance(this).getDevice() == null || !StreamingManager.getInstance(this).getDevice().isConnected()) {
            castButton.setIcon(R.drawable.ic_cast);
        } else {
            castButton.setIcon(R.drawable.ic_cast2);
        }
        return super.onPrepareOptionsMenu(menu);
    }*/
    fun confirmDisconnect() {
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
            .setTitle("Disconnect")
            .setMessage(
                "Are you sure you want to disconnect from " + StreamingManager.getInstance(
                    this
                ).device.friendlyName + "?"
            )
            .setPositiveButton("Yes") { dialog, which ->
                StreamingManager.getInstance(this@MainActivity).disconnect()
                startActivity(Intent(this@MainActivity, ConnectDeviceActivity::class.java))
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun notSupported() {
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
            .setMessage("Sorry this feature isn't supported by your device.")
            .setNegativeButton("OK", null)
            .show()
    }


    fun showPremium() {
        startActivity(Intent(this, PremiumActivity::class.java))
    }

    fun showBackDialog() {
        /*try {
            val backDialog =
                AlertDialog.Builder(ContextThemeWrapper(this, R.style.AlertDialogTheme))
                    .setPositiveButton("Yes") { dialog, which ->
                        if (StreamingManager.getInstance(this@MainActivity).device != null && StreamingManager.getInstance(
                                this@MainActivity
                            ).device.isConnected
                        ) {
                            StreamingManager.getInstance(this@MainActivity).releaseDevice()
                        }
                        stopWebServer()
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .setCancelable(false)
            if (!TinyDB.getInstance(this).isPremium(this)) {
                val backDialogLayout =
                    layoutInflater.inflate(R.layout.back_dialog, null) as RelativeLayout
                backDialog.setView(backDialogLayout)
            } else {
                backDialog.setMessage("Are you sure that you want to leave the app?")
            }
            backDialog.create()
            backDialog.show()
        } catch (e: Exception) {
            finish()
        }*/



        MaterialAlertDialogBuilder(this)
            .setTitle("Leaving App?")
            .setMessage("Are you sure that you want to leave the app?")
            .setNegativeButton("No") { dialog, which ->
                // Respond to negative button press
            }
            .setPositiveButton("Yes") { dialog, which ->
                // Respond to positive button press
                if (StreamingManager.getInstance(this@MainActivity).device != null && StreamingManager.getInstance(
                        this@MainActivity
                    ).device.isConnected
                ) {
                    StreamingManager.getInstance(this@MainActivity).releaseDevice()
                }
                stopWebServer()
                finish()
            }.setCancelable(false)
            .show()


    }


    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }
    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)


        binding.apply {
            searchEt.setText("")
        }

    }


}