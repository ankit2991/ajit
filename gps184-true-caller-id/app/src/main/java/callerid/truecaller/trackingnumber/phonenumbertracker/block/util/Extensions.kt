package callerid.truecaller.trackingnumber.phonenumbertracker.block.util

import android.os.Build
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils

/**
 * Created by Hamza Chaudhary
 * Sr. Software Engineer Android
 * Created on 11 Apr,2023 12:11
 * Copyright (c) All rights reserved.
 * @see "<a href="https://www.linkedin.com/in/iamhco/">Linkedin Profile</a>"
 */


fun TextView.formatSpanColor(string: String) {

    // this is the text we'll be operating on
    val text = SpannableString(string)
    val clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            //  Toast.makeText(activity, "dolor", Toast.LENGTH_LONG).show();
        }
    }
    val clickableSpan2: ClickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            //  Toast.makeText(activity, "dolor", Toast.LENGTH_LONG).show();
        }
    }

    val startText1 = string.indexOf("terms")
    val endText1 = string.indexOf("and the")
    val startText2 = string.indexOf("privacy")
    val endText2 = string.length - 1
    val foregroundColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.resources.getColor(R.color.white, null)
    } else {
        @Suppress("DEPRECATION")
        context.resources.getColor(R.color.white)
    }


    text.setSpan(URLSpan(Utils.Terms), startText1, endText1, 0)
    text.setSpan(clickableSpan, startText1, endText1, 0)
    text.setSpan(
        ForegroundColorSpan(foregroundColor),
        startText1,
        endText1,
        0
    )

    text.setSpan(URLSpan(Utils.Privacy), startText2, endText2, 0)
    text.setSpan(clickableSpan2, startText2, endText2, 0)
    text.setSpan(
        ForegroundColorSpan(foregroundColor),
        startText2,
        endText2,
        0
    )

    movementMethod = LinkMovementMethod.getInstance()

    setText(text, TextView.BufferType.SPANNABLE)

}