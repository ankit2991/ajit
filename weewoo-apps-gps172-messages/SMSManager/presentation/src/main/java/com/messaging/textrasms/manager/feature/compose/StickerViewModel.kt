package com.messaging.textrasms.manager.feature.compose

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import kotlin.coroutines.CoroutineContext


class StickerViewModel : ViewModel(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    private var imagesLiveData: MutableLiveData<ArrayList<File>> = MutableLiveData()
    private lateinit var imagesLiveDataobservable: LiveData<ArrayList<File>>
    fun getImageList(): MutableLiveData<ArrayList<File>> {
        return imagesLiveData
    }


    internal fun imageReaderNew(root: File): ArrayList<File> {
        val listOfAllImages = ArrayList<File>()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.size > 0) {
            for (currentFile in listAllFiles) {
                if (currentFile.name.endsWith(".png")) {

                    Log.e("downloadFilePath", currentFile.absolutePath)
                    // File Name
                    Log.e("downloadFileName", currentFile.name)
                    listOfAllImages.add(currentFile.absoluteFile)
                }

            }
            Log.w("fileList", "" + listOfAllImages.size)
        }
        return listOfAllImages

    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

}