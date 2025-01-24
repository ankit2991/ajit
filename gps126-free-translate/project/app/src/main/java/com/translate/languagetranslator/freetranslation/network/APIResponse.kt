package com.translate.languagetranslator.freetranslation.network

interface APIResponse {
    var failMessage:String?
    var isSuccessfull:Boolean
    var responseData:ResponseData?
}