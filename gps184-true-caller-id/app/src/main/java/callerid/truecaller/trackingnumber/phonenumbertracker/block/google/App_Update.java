package callerid.truecaller.trackingnumber.phonenumbertracker.block.google;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.objects.Update;
import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.BuildConfig;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;


public class App_Update extends Activity {

    public static void UpdateApp(final Activity activity, final int logo_update) {
        AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(activity)
                .withListener(new AppUpdaterUtils.UpdateListener() {
                    @Override
                    public void onSuccess(Update update, Boolean isUpdateAvailable) {
                  /*      String versionName = BuildConfig.VERSION_NAME;
                        if (isUpdateAvailable) {
                            showForceUpdateDialog(activity, logo_update);
                        }
                        if (Utils.App_Update.equals("yes")) {
                            UpdatePage(activity);
                        }*/
                    }

                    @Override
                    public void onFailed(AppUpdaterError error) {
                    }
                });
        appUpdaterUtils.start();
    }

    public static void showForceUpdateDialog(final Activity ctx, final int logo_update) {

        final Dialog updateDialog = new Dialog(ctx, R.style.UserDialog);
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDialog.setCancelable(true);
        updateDialog.setContentView(R.layout.update_layout);

        ImageView cancel = (ImageView) updateDialog.findViewById(R.id.cancel);
        ImageView logo = (ImageView) updateDialog.findViewById(R.id.logo);
        RelativeLayout update = (RelativeLayout) updateDialog.findViewById(R.id.update);

        logo.setImageResource(logo_update);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.dismiss();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ctx.getPackageName())));
            }
        });

        updateDialog.show();


    }

    public static void UpdatePage(final Activity ctx) {

        final Dialog updateDialog = new Dialog(ctx, R.style.saveDialog);
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDialog.setCancelable(false);
        updateDialog.setContentView(R.layout.update_page);

        ImageView app_update_img = (ImageView) updateDialog.findViewById(R.id.app_update_img);
        ImageView update_now = (ImageView) updateDialog.findViewById(R.id.update_now);

        Picasso.get().load(Utils.App_Update_image).into(app_update_img);
        update_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("" + Utils.App_Update_link);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    ctx.startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("" + Utils.App_Update_link)));
                }
            }
        });

        updateDialog.show();


    }
}
