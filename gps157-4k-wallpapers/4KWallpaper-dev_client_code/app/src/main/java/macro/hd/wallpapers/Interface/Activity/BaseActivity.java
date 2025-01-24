package macro.hd.wallpapers.Interface.Activity;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.WallpapersApplication;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setTheme(){
        SettingStore settingStore = SettingStore.getInstance(this);
        if (settingStore.getTheme()==0) {
            setTheme(R.style.AppTheme);
        }else if (settingStore.getTheme()==1) {
            setTheme(R.style.AppTheme1);
        }
    }

    public void setLocale() {
        String languageCode="";
        SettingStore settingStore = SettingStore.getInstance(this);
        if (settingStore.getLanguage()==0) {
           languageCode="en";
        }else if (settingStore.getLanguage()==1) {
            languageCode="hi";
        }

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            createConfigurationContext(config);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        } else {
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }


    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogMsg;

    public CircularProgressIndicator getProgress() {
        return mProgress;
    }

//    private ProgressBar mProgress;
    private CircularProgressIndicator mProgress;
    private TextView tv;
    public void showLoadingDialog(String msg){

        try {
            if (progressDialog == null) {
                progressDialog = CommonFunctions.createProgressDialog(this);

                mProgress=progressDialog.findViewById(R.id.circularProgressbar);

                if(mProgress!=null) {
                    mProgress.setProgress(0,100);   // Main Progress
    //                mProgress.setSecondaryProgress(100); // Secondary Progress
    //                mProgress.setMax(100); // Maximum Progress
                }

                if(!isFinishing()) {
                    progressDialog.show();
                }

            } else if(!progressDialog.isShowing()) {
                if(!isFinishing()) {
                    progressDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void hideLoadingDialog(){
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        if(progressDialogMsg!=null && progressDialogMsg.isShowing()){
            progressDialogMsg.dismiss();
        }
    }

    public TextView getTv() {
        return tv;
    }

    public void showLoadingDialogDownload(String msg){
        try {
            showLoadingDialog(msg);

            progressDialog.findViewById(R.id.progress).setVisibility(View.GONE);
            progressDialog.findViewById(R.id.rl_download).setVisibility(View.VISIBLE);
            mProgress=progressDialog.findViewById(R.id.circularProgressbar);
            tv=progressDialog.findViewById(R.id.tv);

            if(mProgress!=null) {
                mProgress.setProgress(0,100);   // Main Progress
//                mProgress.setSecondaryProgress(100); // Secondary Progress
//                mProgress.setMax(100); // Maximum Progress
                //                mProgress.setProgressDrawable(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoadingDialogMsg(String msg){

        try {
            if (progressDialogMsg == null) {
                progressDialogMsg = CommonFunctions.createProgressDialogMsg(this,msg);
                if(!isFinishing()) {
                    progressDialogMsg.show();
                }

            } else if(!progressDialogMsg.isShowing()) {
                if(!isFinishing()) {
                    progressDialogMsg.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoadingProgress(){
        showLoadingDialog("");//"Please Wait"
//        findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
    }

    public void dismissLoadingProgress(){
        hideLoadingDialog();
//        findViewById(R.id.rl_progress).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        progressDialog=null;
        progressDialogMsg=null;

        mProgress=null;
        tv=null;
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onPause(this);
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        try {
            if (WallpapersApplication.getApplication() != null) {
                WallpapersApplication.getApplication().onResume(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
