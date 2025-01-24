package com.lockerroom.face.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;

import com.lockerroom.face.base.BaseActivity;
import com.lockerroom.face.BuildConfig;
import com.lockerroom.face.R;

public class SettingsActivity extends BaseActivity {

    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        makeFullScreen();
        setContentView(R.layout.setting_layout);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        findViewById(R.id.rate_us).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                } catch (ActivityNotFoundException e) {
                    startActivity(intent);
                }
            }
        });
        findViewById(R.id.share_app).setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.SUBJECT", SettingsActivity.this.getString(R.string.app_name));
            intent.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            SettingsActivity.this.startActivity(Intent.createChooser(intent, "Choose"));
        });
        findViewById(R.id.feedback).setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.putExtra("android.intent.extra.EMAIL", new String[]{getString(R.string.email_feedback)});
            intent.putExtra("android.intent.extra.SUBJECT", "Beauty Feedback: ");
            intent.putExtra("android.intent.extra.TEXT", "");
            intent.setType("message/rfc822");
            SettingsActivity settingsActivity = SettingsActivity.this;
            settingsActivity.startActivity(Intent.createChooser(intent, SettingsActivity.this.getString(R.string.choose_email) + " :"));
        });
        findViewById(R.id.privacy).setOnClickListener(view -> {
            try {
                SettingsActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_url))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        finish();
        return true;
    }
}
