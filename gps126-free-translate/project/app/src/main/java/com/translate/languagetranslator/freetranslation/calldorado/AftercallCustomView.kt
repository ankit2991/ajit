package com.translate.languagetranslator.freetranslation.calldorado

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.calldorado.ui.aftercall.CallerIdActivity
import com.calldorado.ui.views.custom.CalldoradoCustomView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.camera.CameraActivity
import com.translate.languagetranslator.freetranslation.activities.conversation.ConversationActivity
import com.translate.languagetranslator.freetranslation.activities.dictionary.DictionaryActivity
import com.translate.languagetranslator.freetranslation.activities.fictionalLanguage.FictionalLangActivity
import com.translate.languagetranslator.freetranslation.activities.mainScreen.MainActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.openCameraActivity
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.phrasebook.PhrasebookActivity


class AftercallCustomView(context: Context?) : CalldoradoCustomView(context) {

        var translateBtn:RelativeLayout? = null
        var cameraTranslateBtn:RelativeLayout? = null
        var conversationBtn:RelativeLayout? = null
        var dictionaryBtn:RelativeLayout? = null
        var fictionalLangBtn:RelativeLayout? = null
        var phrasebookBtn:RelativeLayout? = null


    init {
        Log.e("calldorado>>","view after call")


    }


    override fun getRootView(): View {
        val rl =
            View.inflate(context, R.layout.home_items_container, linearViewGroup) as LinearLayout


        translateBtn = rl.findViewById(R.id.holder_translate)
        cameraTranslateBtn = rl.findViewById(R.id.holder_camera)
        conversationBtn = rl.findViewById(R.id.holder_conversation)
        dictionaryBtn = rl.findViewById(R.id.holder_dictionary)
        fictionalLangBtn = rl.findViewById(R.id.holder_fictional_lang)
        phrasebookBtn = rl.findViewById(R.id.holder_phrasebook)

        loadData()
       /* val adFrame: FrameLayout = rl.findViewById(R.id.adFrame)
        val advertiseHandler = AdvertiseHandler.getInstance(context)
        advertiseHandler.loadBannerAds(
            context,
            UtilsData.banner_ad_unit_id,
            adFrame, object : AdListener() {

            })
*/


        return rl
    }

    private fun loadData() {

        /*When we add layout to calldorado then only limited item will be shown
       * so add logic in which only first 10 item is shown on screen
       *
       * */

        translateBtn!!.setOnClickListener {
//            val mainIntent = Intent(context, MainActivity::class.java)
//            startActivity(mainIntent)

//            var pm = context.packageManager
//            var intent = pm.getLaunchIntentForPackage(context.packageName)
//            intent!!.putExtra("launch","transferScreen")
//            startActivity(intent);

            openMainActivity(null,"activity",context)
            if (getCalldoradoContext() is CallerIdActivity) {
                (calldoradoContext as CallerIdActivity).finish()
            }
        }

        cameraTranslateBtn!!.setOnClickListener {
            openCameraActivity(context)

            if (getCalldoradoContext() is CallerIdActivity) {
                (calldoradoContext as CallerIdActivity).finish()
            }
        }
        conversationBtn!!.setOnClickListener {
            val intent = Intent(context, DictionaryActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            if (getCalldoradoContext() is CallerIdActivity) {
                (calldoradoContext as CallerIdActivity).finish()
            }
        }
        dictionaryBtn!!.setOnClickListener {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(
                Constants.INTENT_KEY_CONVERSATION_ORIGIN,
                Constants.CONVERSATION_ORIGIN_MAIN
            )
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            if (getCalldoradoContext() is CallerIdActivity) {
                (calldoradoContext as CallerIdActivity).finish()
            }
        }
        phrasebookBtn!!.setOnClickListener {
            val mainIntent = Intent(context, PhrasebookActivity::class.java)
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(mainIntent)

            if (getCalldoradoContext() is CallerIdActivity) {
                (calldoradoContext as CallerIdActivity).finish()
            }
        }
        fictionalLangBtn!!.setOnClickListener {
            openFictionalActivity(null,"activity")

            if (getCalldoradoContext() is CallerIdActivity) {
                (calldoradoContext as CallerIdActivity).finish()
            }
        }
    }


    fun openMainActivity(translationModel: TranslationTable?, source: String,context: Context?) {
        val mainIntent = Intent(context, MainActivity::class.java)
        mainIntent.putExtra("controlled", source)
        translationModel?.let {
            mainIntent.putExtra("opened", "with_data")
            mainIntent.putExtra("detail_object", translationModel)

        } ?: run {
            mainIntent.putExtra("opened", "without_data")

        }
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(mainIntent)
    }

    fun openCameraActivity(context: Context?) {
        var pm = context!!.packageManager
            var intent = pm.getLaunchIntentForPackage(context.packageName)
//            intent!!.putExtra("launch","transferScreen")
            startActivity(intent);
    }

    fun openFictionalActivity(translationModel: TranslationTable?, source: String) {
        val mainIntent = Intent(context, FictionalLangActivity::class.java)
        mainIntent.putExtra("controlled", source)
        translationModel?.let {
            mainIntent.putExtra("opened", "with_data")
            mainIntent.putExtra("detail_object", translationModel)

        } ?: run {
            mainIntent.putExtra("opened", "without_data")

        }
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(mainIntent)
    }
}