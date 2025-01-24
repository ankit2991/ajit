package com.messaging.textrasms.manager.feature.compose

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import java.io.File

class ImageViewModel : ViewModel() {


    private var imagesLiveData: ArrayList<Uri> = ArrayList()
    fun getImageList(): ArrayList<Uri> {
        return imagesLiveData
    }

    internal fun loadImagesfromSDCard(context: Context): ArrayList<Uri> {
        val uri: Uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val column_index_data: Int
        val column_index_folder_name: Int
        val listOfAllImages = ArrayList<Uri>()
        var absolutePathOfImage: File? = null


        val galleryImageUrls = mutableListOf<Uri>()
        val columns = arrayOf(MediaStore.Images.Media._ID)
        val orderBy = MediaStore.Images.Media.DATE_TAKEN

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
            null, null, "$orderBy DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)

                listOfAllImages.add(
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                )
            }
        }
        return listOfAllImages
    }

    fun getAllImages(context: Context) {

        imagesLiveData = loadImagesfromSDCard(context)


    }
}