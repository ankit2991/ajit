package macro.hd.wallpapers.Interface.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.BlurImage;
import macro.hd.wallpapers.Utilily.CommonFunctions;


public class Transferactivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CommonFunctions.setOverlayAction(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ad_popup);
        CommonFunctions.updateWindow(Transferactivity.this);
        TextView txt_msg=findViewById(R.id.txt_msg);

        try {
            txt_msg.setText(WallpapersApplication.getApplication().getSettings().getTransfer().replace("\\n","\n"));
        } catch (Exception e) {
            e.printStackTrace();
            txt_msg.setText(getString(R.string.label_nolongeravail)+"\n\n"+getString(R.string.label_installnew));
        }

        Button img_ad = findViewById(R.id.btn_rate_reviews);

        img_ad.setOnClickListener(this);

        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.transfer);
        try {
            if (bitmap != null) {
                final ImageView img_blurr=findViewById(R.id.img_blurr);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    BlurImage.with(getApplicationContext()).load(bitmap).intensity(25).Async(true).into(img_blurr);
                }else {
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try {
                                if( !isFinishing()){
                                    final Bitmap temp = CommonFunctions.fastblur1(bitmap, 25, Transferactivity.this);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Drawable d = new BitmapDrawable(getResources(), temp);
                                                img_blurr.setBackground(d);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }.start();

                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_rate_reviews:

                String appPackageName = getPackageName(); // getPackageName()
                try {
                    if (WallpapersApplication.getApplication().getSettings()!=null && !TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getPackage())){
                        appPackageName= WallpapersApplication.getApplication().getSettings().getPackage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
//                    Logger.e("Transfer","market://details?id="+appPackageName);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("market://details?id="+appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    try {
                        startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id="+appPackageName)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
