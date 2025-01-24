package com.translate.languagetranslator.freetranslation.activities.camera.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.getPrefString
import com.translate.languagetranslator.freetranslation.appUtils.putPrefString
import com.translate.languagetranslator.freetranslation.appUtils.showToast
import com.translate.languagetranslator.freetranslation.database.DataRepository
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.jvm.Throws

class CameraViewModel(var dataRepository: DataRepository) : ViewModel() {


    fun isAutoAdsRemoved() = dataRepository.isAdsRemoved()

    fun camera_back_interstitial() = dataRepository.camera_back_interstitial

    fun ocr_translate_btn_interstitial() = dataRepository.ocr_translate_btn_interstitial

    private var isCanceled=false
    private var translateJob: Job? = null


    fun getOcrLeftLangName(context: Context): String {
        context?.apply {

        }
        var srcLangName = context.getPrefString(Constants.SOURCE_LANG_NAME_OCR)
        if (srcLangName == null || srcLangName == "") {
            srcLangName = Constants.NAME_SOURCE_LANG
            context.putPrefString(Constants.SOURCE_LANG_NAME_OCR, srcLangName)
        }
        return srcLangName
    }

    fun getOcrLeftLangCode(context: Context): String {

        var srcLangCode = context.getPrefString(Constants.SOURCE_LANG_CODE_OCR)
        if (srcLangCode == null || srcLangCode == "") {
            srcLangCode = "en"
            context.putPrefString(Constants.SOURCE_LANG_CODE_OCR, srcLangCode)
        }
        return srcLangCode
    }

    fun getOcrLeftLangPair(context: Context): String {
        var srcLangPairCode = context.getPrefString(Constants.SOURCE_LANG_CODE_PAIR_OCR)
        if (srcLangPairCode == null || srcLangPairCode == "") {
            srcLangPairCode = "en-GB"
            context.putPrefString(Constants.SOURCE_LANG_CODE_PAIR_OCR, srcLangPairCode)
        }
        return srcLangPairCode
    }


    fun getOcrRightLangName(context: Context): String {

        var srcLangName = context.getPrefString(Constants.TARGET_LANG_NAME_OCR)
        if (srcLangName == null || srcLangName == "") {
            srcLangName = Constants.NAME_TARGET_LANG
            context.putPrefString(Constants.TARGET_LANG_NAME_OCR, srcLangName)
        }
        return srcLangName
    }

    fun getOcrRightLangCode(context: Context): String {

        var srcLangCode = context.getPrefString(Constants.TARGET_LANG_CODE_OCR)
        if (srcLangCode == null || srcLangCode == "") {
            srcLangCode = "fr"
            context.putPrefString(Constants.TARGET_LANG_CODE_OCR, srcLangCode)
        }
        return srcLangCode
    }

    fun getOcrRightLangPair(context: Context): String {
        var srcLangPairCode = context.getPrefString(Constants.TARGET_LANG_CODE_PAIR_OCR)
        if (srcLangPairCode == null || srcLangPairCode == "") {
            srcLangPairCode = "fr-FR"
            context.putPrefString(Constants.TARGET_LANG_CODE_PAIR_OCR, srcLangPairCode)
        }
        return srcLangPairCode
    }


    fun decodeBitMap(bitmap: Bitmap? = null, resultBitmap: (Bitmap?) -> Unit) {
        var finalBitmap: Bitmap? = null
        CoroutineScope(Dispatchers.IO).launch {
            finalBitmap = convertBitmap(bitmap)
            withContext(Dispatchers.Main) {
                resultBitmap.invoke(finalBitmap)
            }

        }

    }

    fun convertBitmap(bitmap: Bitmap?): Bitmap? {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            if (byteArrayOutputStream.toByteArray().size / 1024 > 1024) {
                byteArrayOutputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            }
            val byteArrayInputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(byteArrayInputStream, null, options)
            options.inJustDecodeBounds = false
            val i: Int = options.outWidth
            val i2: Int = options.outHeight
            // int i3 = (i <= i2 || ((float) i) <= 2100.0f) ? (i >= i2 || ((float) i2) <= 2100.0f) ? 1 : ((int) (((float) options.outHeight) / 2100.0f)) + 1 : ((int) (((float) options.outWidth) / 2100.0f)) + 1;
            var i3 =
                if (i <= i2 || i.toFloat() <= 2100.0f) if (i >= i2 || i2.toFloat() <= 2100.0f) 1 else (options.outHeight.toFloat() / 2100.0f).toInt() + 1 else (options.outWidth.toFloat() / 2100.0f).toInt() + 1
            if (i3 <= 0) {
                i3 = 1
            }
            options.inSampleSize = i3
            val byteArrayInputStream2 = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            val decodeStream: Bitmap =
                BitmapFactory.decodeStream(byteArrayInputStream2, null, options)!!
            runCatching {
                byteArrayOutputStream.flush()
                byteArrayOutputStream.close()
                byteArrayInputStream2.close()
            }.onFailure {
                null
            }
            decoder(decodeStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decoder(bitmap: Bitmap): Bitmap? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var i = 90
        while (byteArrayOutputStream.toByteArray().size / 1024 > 1024) {
            byteArrayOutputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream)
            i -= 10
        }
        val decodeStream = BitmapFactory.decodeStream(
            ByteArrayInputStream(byteArrayOutputStream.toByteArray()),
            null,
            null
        )
        try {
            byteArrayOutputStream.flush()
            byteArrayOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return decodeStream
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

    fun extractText(
        activity: Activity,
        bitmap: Bitmap?,
        inputLang: String?,
        resultSuccess: (String?) -> Unit
    ) {
        kotlin.runCatching {

            val image = InputImage.fromBitmap(bitmap!!, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).addOnSuccessListener { success ->

                var languageDetected = ""
                for (blocks in success.textBlocks) {
                    languageDetected = blocks.lines[0].recognizedLanguage
                }
                success?.let {
                    if (languageDetected == inputLang) {
                        resultSuccess.invoke(it.text)
                    } else {

                        showToast(
                            activity,
                            activity.resources.getString(R.string.lang_not_detected)
                        )
                    }
                } ?: kotlin.run {

                    showToast(
                        activity,
                        activity.resources.getString(R.string.capture_clear_image)
                    )
                }

            }
                .addOnFailureListener {
                    showToast(activity, it.localizedMessage)
                }
        }.onFailure {
            showToast(activity, it.localizedMessage)
        }

    }

    fun translateWord(
        inputWord: String?,
        leftCode: String?,
        rightCode: String?,
        result: (String?) -> Unit
    ) {
        translateJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val translatedString = callUrlAndParseResult(leftCode!!, rightCode!!, inputWord)
                    withContext(Dispatchers.Main) {
                        result.invoke(translatedString)
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }

        }


    }

    @Throws(java.lang.Exception::class)
    private fun callUrlAndParseResult(
        langFrom: String, langTo: String,
        word: String?
    ): String? {
        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&" +
                "sl=" + langFrom +
                "&tl=" + langTo +
                "&dt=t&q=" + URLEncoder.encode(word, "UTF-8")
        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection
        con.setRequestProperty("User-Agent", "Mozilla/5.0")
        val `in` = BufferedReader(
            InputStreamReader(con.inputStream)
        )
        var inputLine: String?
        val response = StringBuffer()
        while (`in`.readLine().also { inputLine = it } != null) {
            if (!isCanceled)
                response.append(inputLine) else return null
        }
        `in`.close()
        return parseResult(response.toString(), word)
    }

    @Throws(Exception::class)
    private fun parseResult(inputJson: String, word: String?): String? {
        val jsonArray = JSONArray(inputJson)
        val jsonArray2 = jsonArray[0] as JSONArray
        var outputWord: String? = ""
        for (i in 0 until jsonArray2.length()) {
            if (!isCanceled) {
                val parseResult = jsonArray2[i] as JSONArray
                val result = parseResult[0].toString()
                if (result != null && !result.contains("null")) {
                    outputWord += result
                }
            } else {
                return null
            }
        }
        return outputWord
    }

    fun stopTask() {
        isCanceled = true
    }

}