package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.app.Dialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdConstants;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdInterstitialListener;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

public class BaseActivity extends AppCompatActivity {
    protected void loadInterstitial(Dialog dialog, Class failed, Class closed) {
        if (Utils.getRemoteConfig().getBoolean("GPS119_Interstitial_flag")) {

            MaxAdManager.INSTANCE.createInterstitialAd(
                    this,
                    MaxAdConstants.MAX_AD_INTERSTITIAL_ID,
                    new MaxAdInterstitialListener() {
                        @Override
                        public void onAdLoaded(boolean adLoad) {
                            dialog.dismiss();
                            if (!adLoad) startActivity(new Intent(getApplicationContext(), failed));
                        }

                        @Override
                        public void onAdShowed(boolean adShow) {
                            if (adShow) {
                                SFun sfun = new SFun(getApplicationContext());

                                int Get_Current_Time_in_Second = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                                sfun.setTimeVar("mytime", Get_Current_Time_in_Second);
                                startActivity(new Intent(getApplicationContext(), closed));
                            } else {
                                startActivity(new Intent(getApplicationContext(), failed));
                            }
                        }
                    }

            );
        } else {
            dialog.dismiss();
            SFun sfun = new SFun(getApplicationContext());
            int Get_Current_Time_in_Second = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            sfun.setTimeVar("mytime", Get_Current_Time_in_Second);
            startActivity(new Intent(getApplicationContext(), closed));
        }

    }
}
