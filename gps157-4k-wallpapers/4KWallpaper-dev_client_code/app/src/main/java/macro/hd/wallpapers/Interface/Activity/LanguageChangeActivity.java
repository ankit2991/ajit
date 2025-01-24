package macro.hd.wallpapers.Interface.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import macro.hd.wallpapers.R;

public class LanguageChangeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setContentView(R.layout.activity_language_change);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LanguageChangeActivity.this, MainNavigationActivity.class));
                LanguageChangeActivity.this.overridePendingTransition(0, 0);
                finish();
            }
        },800);

    }
}