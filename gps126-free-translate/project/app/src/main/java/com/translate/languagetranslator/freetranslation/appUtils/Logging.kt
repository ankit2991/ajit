package com.translate.languagetranslator.freetranslation.appUtils

import android.util.Log
import android.widget.Toast
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import java.util.Calendar;
import java.util.Date;

object Logging {

    fun currentTime():String{
        val currentTime: Date = Calendar.getInstance().getTime()

        return currentTime.toString()
    }

    fun adjustEvent(token:String,transactionId: String,screen:String){

        val transc = screen+"_"+transactionId
        Log.e("adjustToken",">>"+token+transc)
        val adjustEvent = AdjustEvent(token)
        adjustEvent.setOrderId(transc);
        Adjust.trackEvent(adjustEvent)
    }
}