package com.translate.languagetranslator.freetranslation.activities.camera.viewModel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class OcrViewModel(val app: Application) : AndroidViewModel(app) {



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

}