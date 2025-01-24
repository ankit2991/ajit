package com.messaging.textrasms.manager.filter

abstract class Filter<in T> {

    abstract fun filter(item: T, query: CharSequence): Boolean

}