package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

public abstract class PhonecallReceiver extends BroadcastReceiver {
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    public static String nnn;
    private String number1;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (extras != null) {
            number1 = extras.getString("incoming_number");
            if (number1 == null) {
                number1 = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            }
            if (number1 == null) {
                number1 = "";
            }
        } else {
            number1 = "";
        }
        try {
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                onCallStateChanged(context, state, number1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
    }

    public void onCallStateChanged(Context context, int state, String number) {
        nnn = number;
        if (lastState == state) {
            if (number == null) {
                return;
            }
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                if (number != null) {
                    onIncomingCallStarted(context, number, callStartTime);
                }
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (isIncoming) {
                    if (number != null) {
                        onIncomingCallEnded(context, number, callStartTime, new Date());
                    }
                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                if (isIncoming) {
                    if (number != null) {
                        onIncomingCallEnded(context, number, callStartTime, new Date());
                    }
                }
                break;
        }
        lastState = state;
    }

}