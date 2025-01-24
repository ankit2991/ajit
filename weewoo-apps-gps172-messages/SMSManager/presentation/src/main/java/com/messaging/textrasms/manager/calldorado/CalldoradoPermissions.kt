package com.messaging.textrasms.manager.calldorado

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.emoji.widget.EmojiAppCompatTextView
import com.calldorado.Calldorado
import com.calldorado.Calldorado.setCustomColors
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.util.Preferences



object CalldoradoPermissions {

    var IS_CALLDORADO_SCREEN = false

    private val permissionRequestCode = 358

    private var calldoradoInit = true

    var phonePermissionGranted = ""
    public fun checkPermissions(activity: Activity, permissionL:RelativeLayout, home_layout:RelativeLayout, btn_continue: Button, overlayPermissionManager:OverlayPermissionManager, terms_policy: EmojiAppCompatTextView){


        if (!Preferences.getBoolean(activity,Preferences.ISCONSENT)){
            permissionL.visibility = View.VISIBLE
            home_layout.visibility = View.GONE

        }else{
            permissionL.visibility = View.GONE
            home_layout.visibility = View.VISIBLE

//            ConsentManger.lookUpForAdsConsentForm(activity)
        }

        btn_continue.setOnClickListener {
            cdoConditions(activity,overlayPermissionManager,permissionL,home_layout)
        }


        if (permissionL.visibility == View.GONE && Preferences.getBoolean(activity,Preferences.ISCONSENT)){
            home_layout.visibility = View.VISIBLE
            onBoardingSuccess(activity,overlayPermissionManager,permissionL,home_layout)
            if (!overlayPermissionManager!!.isGranted){
                overlayPermissionManager!!.requestOverlay()
            }
        }


        termsAndPolices(terms_policy,activity)


    }

     fun requestCdoPermissions(activity: Activity) {
        val permissionList = ArrayList<String>()
        //Essential for Calldorado to work -One request (Phone)
        permissionList.add(Manifest.permission.READ_PHONE_STATE)
        // Optimal (Phone)
        permissionList.add(Manifest.permission.CALL_PHONE)
        permissionList.add(Manifest.permission.ANSWER_PHONE_CALLS)
        // Optimal -Needed to read phone number on Pie+ devices. Requires permission from Google to use from early 2019
//        permissionList.add(Manifest.permission.READ_CALL_LOG)
        // TODO this permission needs permission from Google to use (no permission -> exclude)
        ActivityCompat.requestPermissions(
            activity,
            permissionList.toTypedArray(),
            permissionRequestCode
        )
    }



//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == permissionRequestCode){
//
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                if (!overlayPermissionManager!!.isGranted){
//                    overlayPermissionManager!!.requestOverlay()
//                }
//
//                isPermissionGranted = "granted"
////                permission_layout.visibility = View.GONE
////                home_layout.visibility = View.VISIBLE
//            }else{
//                isPermissionGranted = "notGranted"
//            }
//        }
//    }


//    private fun requestCdoPermissions() {
//        // List of permissions you want to check
//        val permissionList: MutableList<String> = ArrayList()
//        permissionList.add(Manifest.permission.READ_PHONE_STATE)
////        permissionList.add(Manifest.permission.CALL_PHONE)
////        permissionList.add(Manifest.permission.ANSWER_PHONE_CALLS)
//        permissionList.add(Manifest.permission.READ_CALL_LOG)
//
//// Check each permission in the list
//        for (permission in permissionList) {
//            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
//            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                // Permission is granted
//            } else {
//                // Permission is not granted
//                isPermissionGranted = false
//            }
//        }
//
//        if (!isPermissionGranted){
//            startActivity(Intent(this@HomeActivity, PermissionActivity::class.java))
//        }
//    }



     fun cdoConditions(activity: Activity,overlayPermissionManager: OverlayPermissionManager,permission_layout:RelativeLayout,home_layout: RelativeLayout) {
        eulaAccepted(activity)

        val cdoConditions = Calldorado.getAcceptedConditions(activity)
        if (cdoConditions.containsKey(Calldorado.Condition.EULA)) {
            if (cdoConditions[Calldorado.Condition.EULA]!!) {
                // TODO User has already accepted conditions. No need for you to show Optin
                onBoardingSuccess(activity,overlayPermissionManager,permission_layout,home_layout)
                Log.e("calldorado","EULAAcceptAleady")
//                requestCdoPermissions()
            } else {
            }
            // TODO show your means of boarding the user
            if (calldoradoInit) {
                calldoradoInit = false
                // Step 2:
                Log.e("calldorado","EULAAccept")
                val conditionsMap = HashMap<Calldorado.Condition, Boolean>()
                conditionsMap[Calldorado.Condition.EULA] = true
                conditionsMap[Calldorado.Condition.PRIVACY_POLICY] = true
                Calldorado.acceptConditions(activity, conditionsMap)
                // Step 5
                onBoardingSuccess(activity,overlayPermissionManager,permission_layout,home_layout)
//                requestCdoPermissions()
            }
        }
    }

     fun onBoardingSuccess(activity: Activity,overlayPermissionManager: OverlayPermissionManager,permission_layout:RelativeLayout,home_layout: RelativeLayout) {
        // Step 3:
//        Calldorado.start(this)
        // Step 4:

        if (isReadPhoneStatePermissionGranted(activity)){
            if (!overlayPermissionManager!!.isGranted){
                overlayPermissionManager!!.requestOverlay()
            }
        }else{

            if (phonePermissionGranted.contains("granted")){
                if (!overlayPermissionManager!!.isGranted){
                    overlayPermissionManager!!.requestOverlay()
                }
            }else if (phonePermissionGranted.contains("notGranted")){
                requestPermission(activity)
            }else{
                requestCdoPermissions(activity)
            }
        }

        if (isReadPhoneStatePermissionGranted(activity) && overlayPermissionManager!!.isGranted){
            permission_layout.visibility = View.GONE
            home_layout.visibility = View.VISIBLE
        }
    }

     fun eulaAccepted(activity: Activity) {
        val conditionsMap = HashMap<Calldorado.Condition, Boolean>()
        conditionsMap[Calldorado.Condition.EULA] = true
        conditionsMap[Calldorado.Condition.PRIVACY_POLICY] = true
        Calldorado.acceptConditions(activity, conditionsMap)
    }


    //calldorado setting
    fun cdoSettingsActivity(mActivity: Activity) {

        Calldorado.createSettingsActivity(mActivity)
    }


    fun termsAndPolices(terms_policy:TextView,activity: Activity){
        val termsOfUse = "terms of use"
        val privacyPolicy = "privacy policy"
        val fullText = "By continuing, you accept and approve the $termsOfUse and the $privacyPolicy."

        val spannableString = SpannableString(fullText)

        val termsOfUseClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                openUrl(activity.getString(R.string.terms_link),activity)
            }
        }

        val privacyPolicyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                // Handle Privacy Policy click
                openUrl(activity.getString(R.string.privacy_policy_link),activity)
            }
        }

        val termsOfUseStart = fullText.indexOf(termsOfUse)
        val privacyPolicyStart = fullText.indexOf(privacyPolicy)

        spannableString.setSpan(termsOfUseClickableSpan, termsOfUseStart, termsOfUseStart + termsOfUse.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(privacyPolicyClickableSpan, privacyPolicyStart, privacyPolicyStart + privacyPolicy.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply black color to the clickable spans
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK), termsOfUseStart, termsOfUseStart + termsOfUse.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK), privacyPolicyStart, privacyPolicyStart + privacyPolicy.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        terms_policy.text = spannableString
        terms_policy.movementMethod = LinkMovementMethod.getInstance()


//        policies.setOnClickListener {
//            openUrl("https://www.sotec.es/Mobile_Apps.html")
//        }
//
//        terms.setOnClickListener {
//            openUrl("https://www.sotec.es/Terms&C_Apps.html")
//        }
    }

    private fun openUrl(url: String,activity: Activity) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)
    }

    @SuppressLint("NewApi")
     fun requestPermission(activity: Activity) {
        // Check if the permission is granted
        if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
            // User has denied permission previously, show a rationale and request again
            // ...
//            openAppSettings(activity)
            showGrantPermissionDialog(activity)
        } else {
            // Permission has been denied and "Don't ask again" is checked
            // Direct the user to the app's settings to manually enable the permission
//            openAppSettings(activity)
            showGrantPermissionDialog(activity)
        }
    }

     fun isReadPhoneStatePermissionGranted(activity: Activity): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
     fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, permissionRequestCode)
    }

    fun showGrantPermissionDialog(context: Activity){
        val alertDialog = AlertDialog.Builder(context, R.style.BlackAlertDialog) //set icon
//            .setIcon(R.drawable.ic_dialog_alert) //set title
            .setTitle(context.getString(R.string.permission_required)) //set message
            .setMessage(context.getString(R.string.permission_dialog_body)) //set positive button
            .setPositiveButton("GRANT") { dialogInterface, i -> //set what would happen when positive button is clicked
               dialogInterface.dismiss()
                openAppSettings(context)
            } //set negative button
            .setNegativeButton("CANCEL") { dialogInterface, i -> //set what should happen when negative button is clicked

                dialogInterface.dismiss()
            }
            .show()


    }

    ////////////////setting calldorado colors////////////////////////////

    enum class ColorElement {
        MainColor, // A1: WIC, AfterCall background
        ToolbarColor, // A2: Tool bar
        FeatureBgColor, // A3: Background
        MainTextColor, // B: Text
        NavigationColor, // C: Navigation
        AccentColor, // C2: Primary/CTA/Accent
        TabIconButtonTextColor, // D1: Feature Icons
        SelectedTabIcon, // D2: Selected: Feature Icons & Button text color
        FeatureViewCloseColor, // D3: Close
        NativeFieldToolbar, // A4: Tool bar
        NativeFieldClose, // D4: Close
        FeatureButtonColor // E: Feature buttons Icon & text color
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun changeAfterScreenColor(context: Activity){
        val colorMap = HashMap<Calldorado.ColorElement, Int>()
        colorMap[Calldorado.ColorElement.MainColor] = Color.LTGRAY
        colorMap[Calldorado.ColorElement.ToolbarColor] = Color.parseColor("#0A1172")
        colorMap[Calldorado.ColorElement.NavigationColor] = Color.parseColor("#0A1172")
        colorMap[Calldorado.ColorElement.FeatureBgColor] = Color.parseColor("#FFFFFF")
        colorMap[Calldorado.ColorElement.SelectedTabIconColor] = Color.parseColor("#0A1172")
        colorMap[Calldorado.ColorElement.TabIconButtonTextColor] = Color.parseColor("#FFFFFF")

//        setCustomColors(context, colorMap)
    }
}