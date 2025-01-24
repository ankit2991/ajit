package callerid.truecaller.trackingnumber.phonenumbertracker.block.util.calldorado

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calldorado.Calldorado

/**
 * Created by Hamza Chaudhary
 * Sr. Software Engineer Android
 * Created on 11 Apr,2023 12:11
 * Copyright (c) All rights reserved.
 * @see "<a href="https://www.linkedin.com/in/iamhco/">Linkedin Profile</a>"
 */


class SetupFragmentReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "com.calldorado.android.intent.SEARCH" ||
            intent.action == "android.intent.action.PHONE_STATE"
        ) {

            Calldorado.setAftercallCustomView(context, AfterCallCustomView(context))
        }
    }
}