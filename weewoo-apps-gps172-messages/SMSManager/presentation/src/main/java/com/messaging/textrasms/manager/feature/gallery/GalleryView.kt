package com.messaging.textrasms.manager.feature.gallery

import com.messaging.textrasms.manager.common.base.QkView
import com.messaging.textrasms.manager.model.MmsPart
import io.reactivex.Observable

interface GalleryView : QkView<GalleryState> {

    fun optionsItemSelected(): Observable<Int>
    fun screenTouched(): Observable<*>
    fun pageChanged(): Observable<MmsPart>

    fun requestStoragePermission()

}