package macro.hd.wallpapers.Interface.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;

public class Termsactivity extends BaseActivity {

    WebView wvTerms;
    boolean isPrivacyPlicy,isHelp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_termsactivity);
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            isPrivacyPlicy=bundle.getBoolean("privacy");
            isHelp=bundle.getBoolean("help");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.menu_policy));

        if(isPrivacyPlicy)
            getSupportActionBar().setTitle(getString(R.string.menu_policy));
        else
            getSupportActionBar().setTitle(getString(R.string.termofuse));

        if(isHelp){
            getSupportActionBar().setTitle(getString(R.string.menu_wallpaper_not_working));
        }

        wvTerms=findViewById(R.id.wvTerms);

        showDialogue();
        wvTerms.getSettings().setJavaScriptEnabled(true);
        wvTerms.setWebViewClient(new MyWebViewClient());
        openURL();

    }

    public void showDialogue() {
        findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
    }

    public void dismissDialogue() {
        findViewById(R.id.rl_progress).setVisibility(View.GONE);
    }


    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            dismissDialogue();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showDialogue();
        }
    }



    @Override
    public void onBackPressed() {
        if(wvTerms!=null && wvTerms.canGoBack())
            wvTerms.goBack();
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openURL() {
        if(isHelp){
            wvTerms.loadUrl(CommonFunctions.getDomain()+ AppConstant.HELP_URL);
        }else {
            if (isPrivacyPlicy)
                wvTerms.loadUrl(CommonFunctions.getDomain() + AppConstant.TERMS_URL);
            else
                wvTerms.loadUrl(CommonFunctions.getDomain() + AppConstant.TERMS_URL);
        }
//        wvTerms.loadUrl(Common.getDomain()+ AppConstant.TERMS_URL);
        wvTerms.requestFocus();
    }
}
