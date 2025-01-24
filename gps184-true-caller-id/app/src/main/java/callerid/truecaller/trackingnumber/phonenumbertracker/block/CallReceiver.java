package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {
    Context context;
    private SharedPreferences f12996c0;
    private boolean on = true;

    @Override
    protected void onIncomingCallStarted(final Context ctx, String number, Date start) {
        super.onIncomingCallStarted(ctx, number, start);
        context = ctx;
        f12996c0 = ctx.getSharedPreferences("call_setings", 0);
        on = f12996c0.getBoolean("in_call_value", true);

        if (on) {
            final Intent intent = new Intent(context, MyCustomDialog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("phone_no", number);
            context.startActivity(intent);
        }

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        super.onIncomingCallEnded(ctx, number, start, end);
        f12996c0 = ctx.getSharedPreferences("call_setings", 0);
        on = f12996c0.getBoolean("in_call_value", true);

        if (on) {
            final Intent intent = new Intent(ctx, MyCustomDialog1.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("phone_no", number);
            if (MyCustomDialog.a != null) {
                MyCustomDialog.a.finish();
            }
            ctx.startActivity(intent);
        }
    }

}