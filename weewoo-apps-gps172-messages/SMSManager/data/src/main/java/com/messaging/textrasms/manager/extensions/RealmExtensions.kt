package com.messaging.textrasms.manager.extensions

import io.reactivex.Observable
import io.realm.*

fun RealmModel.insertOrUpdate() {
    val realm = Realm.getDefaultInstance()
    realm.executeTransaction { realm.insertOrUpdate(this) }
    realm.close()
}

fun <T : RealmModel> Collection<T>.insertOrUpdate() {
    val realm = Realm.getDefaultInstance()
    realm.executeTransaction { realm.insertOrUpdate(this) }
    realm.close()
}

fun <T : RealmObject> T.asObservable(): Observable<T> {
    return asFlowable<T>().toObservable()
}

fun <T : RealmObject> RealmResults<T>.asObservable(): Observable<RealmResults<T>> {
    return asFlowable().toObservable()
}

fun <T : RealmObject> RealmQuery<T>.anyOf(fieldName: String, values: LongArray): RealmQuery<T> {
    return when (values.isEmpty()) {
        true -> equalTo(fieldName, -1L)
        false -> `in`(fieldName, values.toTypedArray())
    }
}