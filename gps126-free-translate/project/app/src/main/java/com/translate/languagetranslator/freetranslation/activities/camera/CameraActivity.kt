package com.translate.languagetranslator.freetranslation.activities.camera

//import com.weewoo.sdkproject.events.EventHelper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.hardware.Camera
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import com.code4rox.adsmanager.MaxAdManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.tabs.TabLayout
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.theartofdev.edmodo.cropper.CropImageView
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.camera.utils.CameraSource
import com.translate.languagetranslator.freetranslation.activities.camera.utils.GraphicOverlay
import com.translate.languagetranslator.freetranslation.activities.camera.viewModel.CameraViewModel
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_RATEUS
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_translate.*
import kotlinx.android.synthetic.main.layout_ocr_image_preview.*
import kotlinx.android.synthetic.main.layout_ocr_text_preview.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.util.*


class CameraActivity : AppCompatActivity(), CropImageView.OnCropImageCompleteListener {


    private var isImageCaptured = false
    var isDialogVisibile = false
    val viewModel: CameraViewModel by viewModel()
    private val REQUEST_CODE_STORAGE_PERMISSION = 133
    var isPermissionGranted = ""
    /*var backInterstitialAd: Any? = null
    var transBtnInterstitial: Any? = null*/
    var showTransBtnInterstitial = false
    var showBackInterstitial = false
    private var camera: Camera? = null
    private var camParams: Camera.Parameters? = null
    private var cameraDisplay: Display? = null
    private var isGalleryOpened = false
    private var isDetailAvailable = false
    var strBuilder: StringBuilder? = null

    private var srcLangName: String? = null
    private var srcLangCode: String? = null
    private var srcLangPairCode: String? = null

    private lateinit var adsUtill: AdsUtill
    private var tarLangName: String? = null
    private var tarLangCode: String? = null
    private var tarLangPairCode: String? = null
    private var isFlashLightOn = false
    private var isTextTab = false

    private var mCameraSource: CameraSource? = null
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null
    private var mGraphicOverlay: GraphicOverlay<OcrGraphic>? = null
    var translationTable: TranslationTable? = null
    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings
    private var maxInterstitialSession: Int = 3

    /*private var interAdPriority: String? = null
    private var interAdShow = false*/
    private var interAdCounter: Int = 0
    private var localAdCounter: Int = 1
    private var nativeAdShow = false

    //private var interAdPair: InterAdPair? = null
    private var ocrDetectorProcess: OcrDetectorProcessor? = null


    //  private val eventHelper = EventHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        gestureDetector = GestureDetector(
            this,
            CaptureGestureListener()
        )
        (application as AppBase).isOnMainMenu=true
        adsUtill = AdsUtill(this)

        loadBannerAd()
        Logging.adjustEvent("v5gv4z", Logging.currentTime(),"CameraTranslation")
        scaleGestureDetector = ScaleGestureDetector(
            this,
            ScaleListener()
        )
        mGraphicOverlay = findViewById<GraphicOverlay<OcrGraphic>>(R.id.graphicOverlay)
        initRemoteConfig()
        (application as AppBase).thisActivity = true;
        /*Handler().postDelayed({
            if (!isPremium()) {
                Log.d("Skype", "postDelayed")
                if (IronSource.isInterstitialReady()) {
                    if ((application as AppBase).interstitialSessionCount < maxInterstitialSession) {
                        (application as AppBase).interstitialSessionCount++
                        IronSource.showInterstitial()
                    }
                }
            }
        }, 3000)*/
//        showInterstitial()

           /* if (showInstratial){
                if (!isPremium()) {
                    if (IronSource.isInterstitialReady()) {
                        IronSource.showInterstitial()
                        (application as AppBase).addInterstitialSessionCount=0
                    }
                }
            }*/

        //initRemoteConfig()
        viewModel.ocr_translate_btn_interstitial().observe(this, androidx.lifecycle.Observer {
            if (it.show && !viewModel.isAutoAdsRemoved()) {
                //Replaced by ironsource
                Log.d("Skype", "viewModel.ocr_translate_btn_interstitial()")
                showTransBtnInterstitial = true
                /*getInterstitialAdObject(
                    getString(R.string.am_ocr_translate_btn_interstitial),
                    getString(R.string.fb_ocr_translate_btn_interstitial),
                    it.priority,
                    { adObject ->
                        adObject?.let { ad ->
                            transBtnInterstitial = ad
                        }
                    },
                    {
                        transBtnInterstitial?.let {
                            if (it is InterstitialAd)
                                it.loadAd(AdRequest.Builder().build())
                            else if (it is com.facebook.ads.InterstitialAd)
                                it.loadAd()
                        }
                        extractTextFromImage()
                    })*/
            }
        })

        viewModel.camera_back_interstitial().observe(this, androidx.lifecycle.Observer {
            Log.d("Skype", "viewModel.camera_back_interstitial()")
            showBackInterstitial = it.show && !viewModel.isAutoAdsRemoved()
            //Replaced by ironsource
            showBackInterstitial = true


            /*if (it.show && !viewModel.isAutoAdsRemoved()) {
                getInterstitialAdObject(
                    getString(R.string.am_ocr_backpress_intertitial),
                    getString(R.string.fb_ocr_backpress_intertitial),
                    it.priority,
                    { adObject ->
                        adObject?.let { ad ->
                            backInterstitialAd = ad
                        }
                    },
                    {
                        onBackPressed()
                    })
            }*/
        })
        setTabs()
        initLangData()
        openCamPreview()
        setClickListeners()
        if (!isPremium()) {
            initAds()
        }

    }


    private fun initAds() {
        if (nativeAdShow) {
            loadNativeAds()
        }
        /*if (interAdShow) {
            loadInterAds()
        }*/
    }


    private fun loadNativeAds() {

//        TODO()4.5.0   we might call native ads to show from here old code has it
    }

    private fun loadNativeAdAdmob(adsPriority: AdsPriority) {




    }

    private fun loadNativeAdFacebook(adsPriority: AdsPriority) {

    }

    /* private fun loadInterAds() {
         val interAdsPriority = getAdsPriority(interAdPriority)
         when (interAdsPriority) {
             AdsPriority.ADMOB, AdsPriority.ADMOB_FACEBOOK -> {
                 loadInterAdAdmob(interAdsPriority)
             }
             AdsPriority.FACEBOOK, AdsPriority.FACEBOOK_ADMOB -> {
                 loadInterAdFacebook(interAdsPriority)
             }
         }

     }*/

    //private var interAdAdmob: AdmobUtils? = null
    //private var interAdFacebook: FacebookAdsUtils? = null

    /* private fun loadInterAdAdmob(adsPriority: AdsPriority) {

         interAdAdmob = AdmobUtils(this)
         interAdAdmob = AdmobUtils(this, object : AdmobUtils.AdmobInterstitialListener {
             override fun onInterstitialAdClose() {

             }

             override fun onInterstitialAdLoaded() {
                 interAdPair = InterAdPair(interAM = interAdAdmob?.adMobInterAd)

             }

             override fun onInterstitialAdFailed() {
                 if (adsPriority == AdsPriority.FACEBOOK || adsPriority == AdsPriority.FACEBOOK_ADMOB) {
                     loadInterAdFacebook(adsPriority)
                 }

             }

             override fun onAdOpened() {

             }
         }, InterAdsIdType.INTER_AM_CAMERA, true)

     }

     private fun loadInterAdFacebook(adsPriority: AdsPriority) {
         interAdFacebook = FacebookAdsUtils(this)
         interAdFacebook =
             FacebookAdsUtils(this, object : FacebookAdsUtils.FacebookInterstitialListner {
                 override fun onFbInterstitialAdClose() {

                 }

                 override fun onFbInterstitialAdLoaded() {
                     interAdPair = InterAdPair(interFB = interAdFacebook?.facebookInterstitialAd)

                 }

                 override fun onFbInterstitialAdFailed() {

                     if (adsPriority == AdsPriority.ADMOB || adsPriority == AdsPriority.ADMOB_FACEBOOK) {
                         loadInterAdAdmob(adsPriority)
                     }

                 }
             }, InterAdsIdType.INTER_FB_CAMERA, true)

     }*/

    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(200).build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)

        maxInterstitialSession =
            mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.MAX_INTERSTITIAL_SESSION).toInt()


    }


    private fun setClickListeners() {
        iv_flash_ocr_toggle.setOnClickListener {
            toggleFlashLight()
        }
        iv_camera_ocr.setOnClickListener {
            captureImage()
        }
        layout_translate_btn.setOnClickListener {
           /* if (!isPremium()) {
                //if (showTransBtnInterstitial) {
                    IronSource.setInterstitialListener(getInterstitialListener({
                        //onAddLoaded
                        if (it) {
                            if ((application as AppBase).thisActivity) {
                                if ((application as AppBase).interstitialSessionCount < maxInterstitialSession) {
                                    showBackInterstitial = false
                                    (application as AppBase).interstitialSessionCount++
                                    IronSource.showInterstitial()
                                }
                            }
                        } else
                            extractTextFromImage()
                    }, {
                        //onAdClosed
                    }))
                    extractTextFromImage()
                    IronSource.loadInterstitial()
                //}
            } else*/
                extractTextFromImage()
            /*transBtnInterstitial.let {
                if (it != null) {
                    if (it is InterstitialAd && it.isLoaded)
                        it.show()
                    else if (it is com.facebook.ads.InterstitialAd && it.isAdLoaded)
                        it.show()
                    else extractTextFromImage()
                } else
                    extractTextFromImage()
            }*/
        }
        iv_clear_ocr.setOnClickListener {

            hideDetailView()

//            if (!isTextTab) {
//                resetAll()
//            } else {
//                hideDetailView()
//                startCameraSource()
//            }
        }
        layout_lang_ocr_src.setOnClickListener {
            when {

                isImageCaptured -> {
                    isImageCaptured = false
                    resetAll()

                }
                isDialogVisibile -> {
                    hideDetailView()
                }
                else -> startLanguageActivity(this, Constants.LANG_FROM, Constants.LANG_TYPE_OCR)
            }
        }
        layout_lang_ocr_tar.setOnClickListener {
            when {

                isImageCaptured -> {
                    isImageCaptured = false
                    resetAll()

                }
                isDialogVisibile -> {
                    hideDetailView()
                }
                else -> {
                    startLanguageActivity(this, Constants.LANG_TO, Constants.LANG_TYPE_NORMAL)
                }
            }
        }

        iv_gallery_ocr.setOnClickListener {
            openGallery()
        }
        layout_ocr_detection_type.setOnClickListener {
            openDetectionType(it)
        }
        iv_arrow_ocr_more.setOnClickListener {
            translationTable?.let {
//                openHistoryDetailActivity(this, it, DETAIL_SOURCE_CAM)
                val intent = Intent()
                intent.putExtra("origin", "ocr")
                intent.putExtra("detail_object", it)
                setResult(Constants.REQUEST_CODE_OCR_BACK, intent)
                finish()

            }
        }
        iv_capture_ocr.setOnClickListener {
            getAllDetectedBlockItems()
        }
        iv_remove_ocr.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getAllDetectedBlockItems() {
        try {
            val detectedTextBuilder = StringBuilder()
            ocrDetectorProcess?.apply {
                val allDetectedBlocks: Detector.Detections<TextBlock> = getAllDetectedTextBlocks()
                val items = allDetectedBlocks.detectedItems
                for (i in 0 until items.size()) {
                    val item = items.valueAt(i)
                    item?.apply {
                        value?.apply {
                            detectedTextBuilder.append(this).append(" ")
                        }
                    }
                    //                detectedTextBuilder.append(item.value).append(" ")
                }

                detectedTextBuilder.apply {

                    isDialogVisibile = true
                    val extractedWord = this.toString()
                    layout_ocr_result_view.visibility = View.VISIBLE
                    tv_ocr_input.text = extractedWord
                    tv_ocr_result_src_lang.text = srcLangName
                    isDetailAvailable = true

                    if (cam_preview_text != null) {
                        cam_preview_text.stop()
                    }
                    viewModel.translateWord(extractedWord, srcLangCode, tarLangCode) {
                        it?.let {
                            setOutputData(extractedWord, it, srcLangCode!!, tarLangCode!!)
                        }
                    }


                }


            }
        } catch (e: NullPointerException) {
        }
    }

    private fun openDetectionType(view: View) {
        val popup = PopupMenu(view.context, view)
        try {
            val fields = popup.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field[popup]!!
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod(
                        "setForceShowIcon",
                        Boolean::class.javaPrimitiveType
                    )
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        popup.menuInflater.inflate(R.menu.menu_camera, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_detect_word -> setDetectionType(0, getString(R.string.menu_detect_word))
                R.id.action_detect_para -> setDetectionType(
                    1,
                    getString(R.string.menu_detect_paragraph)
                )
            }
            true
        }
        popup.show()
    }

    private fun setDetectionType(type: Int, detectionType: String) {
        tv_type_ocr_detection.text = detectionType
        TinyDB.getInstance(this).putInt("ocr_detection", type)

    }
    private val permissionToCheck = Manifest.permission.READ_MEDIA_IMAGES

    private fun openGallery() {
        putPrefBoolean("app_killed", true)

        if (Build.VERSION.SDK_INT >= 33) {
            if (!isGalleryPermissionGranted()) {
                if (isPermissionGranted.contains("notGranted")) {
                    goToPermission()
                }else{

                    Dexter.withContext(this)
                        .withPermission(Manifest.permission.READ_MEDIA_IMAGES)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(response: PermissionGrantedResponse?) { /* ... */
                                isPermissionGranted = "granted"
                                openGalleryForImage()
                            }

                            override fun onPermissionDenied(response: PermissionDeniedResponse?) { /* ... */
                                isPermissionGranted = "notGranted"
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                p0: com.karumi.dexter.listener.PermissionRequest?,
                                p1: PermissionToken?
                            ) {
//                                TODO("Not yet implemented")
                                isPermissionGranted = "notGranted"
                            }

                        }).check()

                }

            }else{
                openGalleryForImage()
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionAlreadyGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                goToPermission()
            } else {
                openGalleryForImage()
            }
        } else {
            openGalleryForImage()
        }
    }

    private fun openGalleryForImage() {
        if (isDialogVisibile) {
            hideDetailView()
            callHandlerForPref()
        } else {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            val chooserIntent = Intent.createChooser(galleryIntent, "Select Image")
            startActivityForResult(chooserIntent, Constants.REQUEST_CODE_IMAGE_SELECTOR)
        }
    }

    private fun goToPermission() {
        Permissions.check(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    openGalleryForImage()
                }

                override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
                    super.onDenied(context, deniedPermissions)
                    this@CameraActivity.callHandlerForPref()
                }
            })


    }

    private fun extractTextFromImage() {
        image_holder.setOnCropImageCompleteListener(this)
        image_holder.getCroppedImageAsync()
    }

    private fun initLangData() {
        srcLangName = this.getPrefString(Constants.SOURCE_LANG_NAME_OCR)
        if (srcLangName == null || srcLangName == "") {
            srcLangName = Constants.NAME_SOURCE_LANG
            this.putPrefString(Constants.SOURCE_LANG_NAME_OCR, srcLangName!!)
        }
        srcLangCode = this.getPrefString(Constants.SOURCE_LANG_CODE_OCR)
        if (srcLangCode == null || srcLangCode == "") {
            srcLangCode = "en"
            this.putPrefString(Constants.SOURCE_LANG_CODE_OCR, srcLangCode!!)
        }
        srcLangPairCode = this.getPrefString(Constants.SOURCE_LANG_CODE_PAIR_OCR)
        if (srcLangPairCode == null || srcLangPairCode == "") {
            srcLangPairCode = "en-GB"
            this.putPrefString(Constants.SOURCE_LANG_CODE_PAIR_OCR, srcLangPairCode!!)
        }


        tarLangName = this.getPrefString(Constants.TARGET_LANG_NAME_OCR)
        if (tarLangName == null || tarLangName == "") {
            tarLangName = Constants.NAME_TARGET_LANG
            this.putPrefString(Constants.TARGET_LANG_NAME_OCR, tarLangName!!)
        }
        tarLangCode = this.getPrefString(Constants.TARGET_LANG_CODE_OCR)
        if (tarLangCode == null || tarLangCode == "") {
            tarLangCode = "fr"
            this.putPrefString(Constants.TARGET_LANG_CODE_OCR, tarLangCode!!)
        }
        tarLangPairCode = this.getPrefString(Constants.TARGET_LANG_CODE_PAIR_OCR)
        if (tarLangPairCode == null || tarLangPairCode == "") {
            tarLangPairCode = "fr-FR"
            this.putPrefString(Constants.TARGET_LANG_CODE_PAIR_OCR, tarLangPairCode!!)
        }
        tv_lang_src_ocr.text = srcLangName
        tv_lang_tar_ocr.text = tarLangName
    }

    private fun setTabs() {
        tab_ocr_type.run {
            tab_ocr_type.addTab(tab_ocr_type.newTab().setText("Image"))
            tab_ocr_type.addTab(tab_ocr_type.newTab().setText("Text"))


            tab_ocr_type.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab!!.position == 0) {
                        openCamPreview()
                        pauseLiveView()
                        isTextTab = false
                    } else {
                        if (isImageCaptured) {
                            layout_translate_btn.visibility = View.GONE
                            layout_image_captured.visibility = View.GONE
                            tab_ocr_type.visibility = View.GONE
                            isImageCaptured = false
                        }
                        openLiveView()
                        pauseCamPreview()
                        isTextTab = true

                    }
                    if (isDialogVisibile)
                        hideDetailView()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            })

            val tab: TabLayout.Tab = tab_ocr_type.getTabAt(0)!!
            tab.select()
        }

    }

    private fun pauseCamPreview() {
        kotlin.runCatching {
            if (isFlashLightOn) {
                turnOffFlash()
            }
            layout_ocr_image.visibility = View.GONE
            camera?.let { cam ->
                cam.cancelAutoFocus()
                cam.setPreviewCallback(null)
                cam.stopPreview()
            }
        }


    }

    private fun openLiveView() {
        layout_ocr_text.visibility = View.VISIBLE

        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        ocrDetectorProcess = OcrDetectorProcessor(mGraphicOverlay, this)
        val textRecognizer = TextRecognizer.Builder(this).build()
        textRecognizer.setProcessor(ocrDetectorProcess)

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setRequestedFps(2.0f)
            .setFlashMode(null)
            .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            .build()

        startCameraSource()

    }

    private fun pauseLiveView() {
        layout_ocr_text.visibility = View.GONE

    }

    private fun openCamPreview(isFromOnResume: Boolean = false) {
        kotlin.runCatching {
            if (!isFromOnResume) {
                layout_ocr_image.visibility = View.VISIBLE
                layout_ocr_image_bottom.visibility = View.VISIBLE
            }
            camera = Camera.open()
            val customCamView = CustomCamView(this, camera)
            camera_layout.removeAllViews()
            camera_layout.addView(customCamView)
            camera?.let { cam ->
                val parameters: Camera.Parameters = cam.parameters
                parameters.pictureFormat = 256
                cameraDisplay = windowManager?.defaultDisplay
                cam.parameters = parameters
            }
        }
        handler.sendEmptyMessageDelayed(0, 500)

    }

    private var handler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            when (message.what) {
                0 -> {
                    setCamParams()
                    return
                }
                1 -> {
                    return
                }
                else -> {
                }
            }
        }
    }

    fun setCamParams() {
        try {
            val parameters = camera!!.parameters
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            camera!!.parameters = parameters
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleFlashLight() {
        if (isFlashLightOn) {
            turnOffFlash()
        } else {
            turnOnFlash()
        }
    }

    private fun turnOffFlash() {
        kotlin.runCatching {
            camera?.let { cam ->
                camParams = cam.parameters
                camParams?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera!!.parameters = camParams
                isFlashLightOn = false
                iv_flash_ocr_toggle.setImageResource(R.drawable.ic_flash_off)
            }
        }
    }

    private fun turnOnFlash() {
        camera?.let { cam ->
            try {
                camParams = cam.parameters
                camParams?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                camera!!.parameters = camParams
                isFlashLightOn = true
                iv_flash_ocr_toggle.setImageResource(R.drawable.ic_flash_ocr_on)
            } catch (e: RuntimeException) {
            }


        }
    }

    private fun captureImage() {
        if (isFlashLightOn) {
            turnOffFlash()
        }
        kotlin.runCatching {
            camera?.let { cam ->
                cam.takePicture(null, null,
                    { bytes: ByteArray, camera: Camera? ->
                        Thread {
                            val imageBitMap =
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (imageBitMap != null) {
                                convertImageToBitmap(imageBitMap)
                            } else {
                                runOnUiThread {
                                    showToast(this, "Try Again")
                                    resetAll()
                                }
                            }
                        }.start()
                    })
            }


        }
    }

    private fun convertImageToBitmap(imageBitMap: Bitmap) {
        viewModel.decodeBitMap(imageBitMap) { bitmap1 ->
            fetchImageFromSource(bitmap1)
            null
        }

    }

    private fun fetchImageFromSource(bitmap: Bitmap?) {
        var imageBitMap = bitmap
        if (cameraDisplay != null) {
            val rotation = cameraDisplay!!.rotation
            if (rotation != 0) {
                when (rotation) {
                    Surface.ROTATION_180 -> imageBitMap = viewModel.getImageBitMap(imageBitMap, 270)
                    Surface.ROTATION_270 -> imageBitMap = viewModel.getImageBitMap(imageBitMap, 180)
                }
            } else {
                imageBitMap = viewModel.getImageBitMap(imageBitMap, 90)
            }
        }
        val finalImageBitMap = imageBitMap
        processCapturedImage(finalImageBitMap)
    }

    private fun processCapturedImage(finalImageBitMap: Bitmap?) {
        try {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val width: Int = size.x
            val height: Int = size.y
            stopCameraPreview()
            image_holder.setImageBitmap(finalImageBitMap)
//        image_holder.setMaxCropResultSize(width, height)
            layout_image_captured.visibility = View.VISIBLE

            layout_ocr_image_bottom.visibility = View.GONE
            layout_translate_btn.visibility = View.VISIBLE
            tab_ocr_type.visibility = View.GONE
            isImageCaptured = true
        } catch (e: IllegalStateException) {
        }
    }

    private fun stopCameraPreview() {
        try {
            camera?.let { cam ->
                if (hasAutoFocusFeature())
                    cam.cancelAutoFocus()
                cam.setPreviewCallback(null)
                cam.stopPreview()
            }
        } catch (ee: java.lang.Exception) {

        }
    }

    private fun hasAutoFocusFeature(): Boolean {
        if (camera != null) {
            val p: Camera.Parameters = camera!!.parameters
            val focusModes = p.supportedFocusModes

            return focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)
        } else return false
    }

    private fun resetAll() {
        hideDetailView()
        layout_image_captured.visibility = View.GONE
        layout_ocr_image_bottom.visibility = View.VISIBLE
        layout_translate_btn.visibility = View.GONE
        tab_ocr_type.visibility = View.VISIBLE
        openCamPreview()

    }

    private fun hideDetailView() {
        layout_ocr_result_view.visibility = View.GONE
        ocr_result_container_output.visibility = View.GONE
        progress_ocr.visibility = View.VISIBLE
        tv_ocr_input.text = ""
        tv_ocr_result_src_lang.text = ""
        tv_ocr_result_tar_lang.text = ""
        tv_ocr_output.text = ""
        iv_arrow_ocr_more.visibility = View.GONE
        isDialogVisibile = false
        if (isTextTab) {
            startCameraSource()
        }
    }

    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult?) {
        kotlin.runCatching {
            val croppedImage = result?.bitmap

            viewModel.extractText(this, croppedImage, srcLangCode) { extractedWord ->
                isDialogVisibile = true
//                layout_image_captured.visibility = View.GONE
                layout_ocr_result_view.visibility = View.VISIBLE
                tv_ocr_input.text = extractedWord
                tv_ocr_result_src_lang.text = srcLangName
                isDetailAvailable = true
               /* if (localAdCounter >= interAdCounter) {
                    if (!isPremium()) {
                        // replace by ironsource
                        IronSource.setInterstitialListener(getInterstitialListener({
                            //onAddLoaded
                            if (it) {
                                if ((application as AppBase).thisActivity) {
                                    if ((application as AppBase).interstitialSessionCount < maxInterstitialSession) {
                                        (application as AppBase).interstitialSessionCount++
                                        IronSource.showInterstitial()
                                    }
                                }
                            }
                        }, {
                            //onAdClosed

                        }))
                        IronSource.loadInterstitial();
                    }

                } else {
                    localAdCounter++
                }*/


                viewModel.translateWord(extractedWord!!, srcLangCode, tarLangCode) {
                    it?.let {
                        setOutputData(extractedWord, it, srcLangCode!!, tarLangCode!!)
                    }
                }
            }


        }
    }

    private fun setOutputData(
        extractedWord: String,
        translatedWord: String,
        leftLangCode: String,
        rightLangCode: String
    ) {
        kotlin.runCatching {

            val uniqueId = srcLangCode + extractedWord + tarLangCode
            val translationDb = TranslationDb.getInstance(this)
            val isFavorite = translationDb.translationTblDao().isFavorite(uniqueId)
            translationTable = TranslationTable(
                srcLangCode,
                tarLangCode,
                extractedWord,
                translatedWord,
                srcLangPairCode,
                tarLangPairCode,
                uniqueId,
                isFavorite
            )
            translationDb.translationTblDao().insert(translationTable)

            progress_ocr.visibility = View.GONE
            ocr_result_container_output.visibility = View.VISIBLE
            tv_ocr_result_tar_lang.text = tarLangName
            tv_ocr_output.text = translatedWord
            iv_arrow_ocr_more.visibility = View.VISIBLE


        }

    }

    override fun onBackPressed() {
        when {
            isDialogVisibile -> {
                hideDetailView()
            }
            isImageCaptured -> {
                isImageCaptured = false
                resetAll()
            }
            else -> {
                try {
                    //        TODO()4.5.0  banner was being destroyed here
                    //adsUtill.destroyBanner()
                    HomeActivity.showBanner=true
                    super.onBackPressed()
                    setResult(REQ_CODE_RATEUS,Intent())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
               /* if (!isPremium()) {
                    if (showBackInterstitial) {
                        IronSource.setInterstitialListener(getInterstitialListener({
                            //onResult
                            if (it) {
                                if ((application as AppBase).thisActivity) {
                                    if ((application as AppBase).interstitialSessionCount < maxInterstitialSession) {
                                        showBackInterstitial = false
                                        (application as AppBase).interstitialSessionCount++
                                        IronSource.showInterstitial()
                                    }
                                }
                            } else
                                try {
                                    super.onBackPressed()
                                    setResult(REQ_CODE_RATEUS,Intent())
                                } catch (e: Exception) {
                                }
                        }, {
                            //onClose
                            try {
                                super.onBackPressed()
                                setResult(REQ_CODE_RATEUS,Intent())
                            } catch (e: Exception) {
                            }
                        }))
                        super.onBackPressed()
                        setResult(REQ_CODE_RATEUS,Intent())
                        IronSource.loadInterstitial()
                    }
                } else {
                    try {
                        super.onBackPressed()
                        setResult(REQ_CODE_RATEUS,Intent())
                    } catch (e: Exception) {
                    }
                }*/
            }
        }
    }


    private val RC_HANDLE_GMS = 9001

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(
                this,
                code,
                RC_HANDLE_GMS
            )
            dlg!!.show()
        }
        if (mCameraSource != null) {
            try {
                cam_preview_text?.start(mCameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e("error", "Unable to start camera source.", e)
                mCameraSource?.release()
                mCameraSource = null
            } catch (e: RuntimeException) {
                Log.e("error", "Unable to start camera source.", e)
                mCameraSource?.release()
                mCameraSource = null
            } catch (e: NullPointerException) {
                Log.e("error", "Unable to start camera source.", e)
                mCameraSource?.release()
                mCameraSource = null
            }
        }
    }

    inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {
        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return false
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         *
         * Once a scale has ended, [ScaleGestureDetector.getFocusX]
         * and [ScaleGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         */
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            mCameraSource?.doZoom(detector.scaleFactor)
        }
    }

    inner class CaptureGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

    private fun onTap(rawX: Float, rawY: Float): Boolean {
        kotlin.runCatching {
            val graphic: OcrGraphic = mGraphicOverlay!!.getGraphicAtLocation(rawX, rawY)
            var text: TextBlock? = null
            if (graphic != null) {
                text = graphic.textBlock
                if (text != null && text.value != null) {
                    isDialogVisibile = true
                    val extractedWord = text.value
                    layout_ocr_result_view.visibility = View.VISIBLE
                    tv_ocr_input.text = extractedWord
                    tv_ocr_result_src_lang.text = srcLangName
                    isDetailAvailable = true

                    if (cam_preview_text != null) {
                        cam_preview_text.stop()
                    }
                    viewModel.translateWord(extractedWord!!, srcLangCode, tarLangCode) {
                        it?.let {
                            setOutputData(extractedWord, it, srcLangCode!!, tarLangCode!!)
                        }
                    }

                } else {
                    Log.d("ocr", "text data is null")
                }
            } else {
                Log.d("ocr", "no text detected")
            }
            return text != null
        }.onFailure {
            return false
        }
        return false

    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val b = scaleGestureDetector!!.onTouchEvent(e!!)
        val c = gestureDetector!!.onTouchEvent(e!!)
        return b || c || super.onTouchEvent(e!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQ_CODE_LANGUAGE_SELECTION) {
            if (resultCode != Activity.RESULT_CANCELED) {

                val source = data!!.getStringExtra("origin")
                val langName = data.getStringExtra(Constants.INTENT_KEY_LANG_NAME)
                val langCode = data.getStringExtra(Constants.INTENT_KEY_LANG_CODE)
                val languageSupport = data.getStringExtra(Constants.INTENT_KEY_LANG_SUPPORT)
                val languageMeaning = data.getStringExtra(Constants.INTENT_KEY_LANG_MEANING)
                assert(source != null)
                if (source != Constants.LANG_FROM) {
                    tarLangName = langName
                    tv_lang_tar_ocr.text = tarLangName
                    tarLangCode = languageSupport
                    tarLangPairCode = langCode
                    this.putPrefString(
                        Constants.TARGET_LANG_CODE_PAIR_OCR,
                        tarLangPairCode!!
                    )
                    this.putPrefString(
                        Constants.TARGET_LANG_NAME_OCR,
                        tarLangName!!
                    )
                    this.putPrefString(
                        Constants.TARGET_LANG_CODE_OCR,
                        tarLangCode!!
                    )
                } else {
                    srcLangName = langName
                    srcLangCode = languageSupport
                    srcLangPairCode = langCode
                    tv_lang_src_ocr.setText(srcLangName)
                    this.putPrefString(
                        Constants.SOURCE_LANG_NAME_OCR,
                        srcLangName!!
                    )
                    this.putPrefString(
                        Constants.SOURCE_LANG_CODE_OCR,
                        srcLangCode!!
                    )
                    this.putPrefString(
                        Constants.SOURCE_LANG_CODE_PAIR_OCR,
                        srcLangPairCode!!
                    )
                }
            }

        } else if (requestCode == Constants.REQUEST_CODE_IMAGE_SELECTOR) {
            if (resultCode != Activity.RESULT_CANCELED) {
                isGalleryOpened = true
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    getImageFromGallery(selectedImageUri)
                } else {
                    showToast(this, "Try Again")
                }
                if (isFlashLightOn) {
                    turnOffFlash()
                }
                callHandlerForPref()
            }
        }
    }

    private fun getImageFromGallery(selectedImageUri: Uri) {
        kotlin.runCatching {
            val bitmap = uriToBitmap(selectedImageUri)
            if (bitmap != null) {
                convertGalleryBitmap(bitmap)
            } else {
                showToast(this, "Select Another Image")
            }
        }

    }

    fun convertGalleryBitmap(bitmap: Bitmap?) {
        viewModel.decodeBitMap(bitmap) { bitmap1: Bitmap? ->
            fetchImageFromGallery(bitmap1)
            null
        }
    }

    fun fetchImageFromGallery(imageBitMap: Bitmap?) {
        var imageBitMap = imageBitMap
        imageBitMap = getImageBitMap(imageBitMap, 0)
        val finalImageBitMap = imageBitMap
        runOnUiThread { processCapturedImage(finalImageBitMap) }
    }

    fun getImageBitMap(bitmap: Bitmap?, i: Int): Bitmap? {
        return if (bitmap == null) {
            null
        } else try {
            val matrix = Matrix()
            matrix.postRotate(i.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onPause() {
        if (isFlashLightOn) {
            turnOffFlash()
        }
        camera?.let {
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (isTextTab)
            openLiveView()
        else {
            openCamPreview(true)
        }
    }

    private fun loadBannerAd() {

        if (!isPremium()) {
            try {
                MaxAdManager.createBannerAd(this,native_banner_container_home_screen)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }






    private fun isGalleryPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }



}