package com.translate.languagetranslator.freetranslation.activities.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ListPopupWindow
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.clipboard.adapter.CustomAdapter
import com.translate.languagetranslator.freetranslation.activities.clipboard.viewModel.ClipBoardViewModel
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import kotlinx.android.synthetic.main.activity_copy_to_clip.*
import java.util.*

class CopyToClipActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    private var copiedData: String? = null
    private var origin: String? = null


    private var leftLangName: String = "English"
    private var leftLangCodeSupport: String = "en"
    private var leftLangMeaning: String = "English"
    private var leftLangCode: String = "en-US"
    private var translation: TranslationTable? = null
    private var clipboardService: Any? = null
    private var clipboardManager: ClipboardManager? = null

    val viewModel: ClipBoardViewModel by lazy {
        ViewModelProviders.of(this).get(ClipBoardViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        putPrefBoolean("is_feedback", true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_copy_to_clip)
        window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#60000000")))
        getCopiedData()
        setLangsData()
        setClickListeners()
        clipboardService = getSystemService(CLIPBOARD_SERVICE)
        clipboardManager = clipboardService as ClipboardManager

    }

    private fun getCopiedData() {
        val intent = intent
        val bundle = intent.extras!!
        origin = bundle.getString(Constants.KEY_ORIGIN)
        kotlin.runCatching {
            origin?.let {
                copiedData = bundle.getString(Constants.INTENT_KEY_CLIP_BOARD_DATA)
                tv_clip_input.text = copiedData
//                callTranslation()
            } ?: run {
                val charSequenceExtra =
                    intent.getCharSequenceExtra("android.intent.extra.PROCESS_TEXT")
                if (charSequenceExtra != null) {
                    copiedData = charSequenceExtra.toString()
                    tv_clip_input.text = copiedData
//                    callTranslation()
                }
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun callTranslation() {
        viewModel.translateWord(copiedData, leftLangCode, clipRightSupport) {
            progress_clip.visibility = View.GONE
            tv_clip_output.visibility = View.VISIBLE
            tv_clip_output.text = it

        }
    }

    private fun setClickListeners() {
        iv_cross_clip.setOnClickListener {
            finish()
        }
        iv_copy_clip_translation.setOnClickListener {
            copyData()
        }
        tv_clip_clear.setOnClickListener {
            finish()
        }
        iv_more_clip.setOnClickListener {
            showDetailView()
        }
    }

    private fun showDetailView() {
        val destLangCode = clipRightLangCode
        val destLangMeaning = clipRightLangMeaning
        val destLangSupportCode = clipRightSupport
        val destLangName = clipRightLangName

        val inputWord = tv_clip_input.text.toString().trim()
        val translatedWord = tv_clip_output.text.toString().trim()
        val uniqueId = leftLangCodeSupport + inputWord + destLangSupportCode
        val isFavorite = viewModel.getFavorite(uniqueId)

        val translatedModel = TranslationTable(
            leftLangCodeSupport,
            destLangSupportCode,
            inputWord,
            translatedWord,
            leftLangCode,
            destLangCode,
            uniqueId,
            isFavorite
        )
        openMainActivity(translatedModel,"clip_activity")
        finish()

    }

    private fun copyData() {
        val strinBuilder = StringBuilder()
        val inputWord = tv_clip_input.text.toString().trim()
        val translatedWord = tv_clip_output.text.toString().trim()
        strinBuilder.append("input:", "\n")
        strinBuilder.append(inputWord, " \n")
        strinBuilder.append("translated", "\n")
        strinBuilder.append(translatedWord, "\n")
        putPrefBoolean("is_from_app", true)
        val clipData = ClipData.newPlainText("Source Text", strinBuilder.toString())
        clipboardManager?.setPrimaryClip(clipData)
        showToast(this, resources.getString(R.string.text_copied))
    }

    private fun setLangsData() {
        clipRightLangName = viewModel.getClipRightLangName()
        clipRightLangCode = viewModel.getClipRightLangCode()
        clipRightLangMeaning = viewModel.getClipRightLangMeaning()
        clipRightSupport = viewModel.getClipSupportLangCode()

        var allLangs = viewModel.fetchAllLanguages()

        val countries: MutableList<String> = ArrayList()
        for (languageModel in allLangs) {
            countries.add(languageModel.supportedLangCode)
        }
        val selectedId = countries.indexOf(clipRightSupport)
        val adapter = CustomAdapter(this, allLangs)

        try {
            val popup = Spinner::class.java.getDeclaredField(
                resources.getString(R.string.mpopUp)
            )
            popup.isAccessible = true
            val popupWindow = popup[spinner_lang] as ListPopupWindow
            val spinerHeight: Int = getScreenHeight() / 2
            assert(popupWindow != null)
            popupWindow.height = spinerHeight
        } catch (e: NoClassDefFoundError) {
            // silently fail...
        } catch (e: ClassCastException) {
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        }

        spinner_lang.adapter = adapter
        spinner_lang.setSelection(selectedId)
        spinner_lang.onItemSelectedListener = this

    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    private var clipRightLangName: String? = null
    private var clipRightLangCode: String? = null
    private var clipRightLangMeaning: String? = null
    private var clipRightSupport: String? = null

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        clipRightLangCode = viewModel.fetchAllLanguages()[position].languageCode
        clipRightLangName = viewModel.fetchAllLanguages()[position].languageName
        clipRightLangMeaning = viewModel.fetchAllLanguages()[position].languageMeaning
        clipRightSupport = viewModel.fetchAllLanguages()[position].supportedLangCode

        viewModel.setClipLangInTinyDb(
            rightName = clipRightLangName,
            rightCode = clipRightLangCode,
            rightMeaning = clipRightLangMeaning,
            support = clipRightSupport
        )

        progress_clip.visibility = View.VISIBLE
        tv_clip_output.visibility = View.GONE
        if (NetworkUtils.isNetworkConnected(this)) {
            callTranslation()
        } else {
            progress_clip.visibility = View.GONE
            tv_clip_output.visibility = View.VISIBLE
            tv_clip_output.text = getString(R.string.check_network)


        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}