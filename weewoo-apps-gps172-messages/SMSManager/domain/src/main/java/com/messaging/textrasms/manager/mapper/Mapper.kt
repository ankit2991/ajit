package com.messaging.textrasms.manager.mapper

interface Mapper<in From, out To> {

    fun map(from: From): To

}