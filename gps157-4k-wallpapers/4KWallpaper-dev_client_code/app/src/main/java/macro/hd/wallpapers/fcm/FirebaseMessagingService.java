package macro.hd.wallpapers.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import macro.hd.wallpapers.Interface.Activity.NotificationAlertActivity;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Model.Category;
import macro.hd.wallpapers.Model.Wallpapers;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.RetryPolicy;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private String[] sampleText;

    private static final String TAG = "MyFirebaseMsgService";
    String msg_display,title_display,temp_text_display;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Logger.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
//        String message=remoteMessage.getNotification().getBody();
        String message=remoteMessage.getData().toString();
        Logger.d(TAG, "From: " + remoteMessage.getFrom());

        sampleText=new String[]{WallpapersApplication.sContext.getResources().getString(R.string.check_out),
                WallpapersApplication.sContext.getResources().getString(R.string.awe_wallpaper),
                WallpapersApplication.sContext.getResources().getString(R.string.awe_Added),
                "\uD83E\uDD70 Lovely wallpapers waiting for you. Just Check them out! \uD83E\uDD70",
                WallpapersApplication.sContext.getResources().getString(R.string.add_Wall),
                "Set Live Wallpaper and Make your phone to smart \uD83D\uDCF1",
                "Cool Wallpapers Added. Let's Check Them Out ❤",
                "\uD83D\uDC49 You have new Live wallpapers. Click to check them","Download amazing collection of wallpaper"};

        if(TextUtils.isEmpty(message))
            return;
        try {
            message=remoteMessage.getData().get("body");
            Logger.d(TAG, "message: " + message);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "Exception: " + e.getMessage());
        }
//        Logger.d(TAG, "From data : " + remoteMessage.getData().get("icon"));
//        Logger.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Wallpapers post = null;
        Category category = null;
        String type="";
        String temp_img_display="";
        try {
            if(TextUtils.isEmpty(message))
                return;

            try {
                JSONObject jsonObject=new JSONObject(message);
                type=jsonObject.optString("type");
                msg_display=jsonObject.optString("msg");
                title_display=jsonObject.optString("title_display");
                temp_text_display=jsonObject.optString("temp_text_display");
                temp_img_display=jsonObject.optString("temp_img_display");
                if(!TextUtils.isEmpty(type) && type.equalsIgnoreCase("1")){
                    String domain=jsonObject.optString("domain");
                    SettingStore settingStore=SettingStore.getInstance(this);
                    if(!TextUtils.isEmpty(domain)) {
                        settingStore.setDomain(domain);
                        Logger.e("change domain","change domain");
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(!TextUtils.isEmpty(type) && type.equalsIgnoreCase("5")){
                Gson gson = new Gson();
                category = gson.fromJson(message, Category.class);
            }else {
                Gson gson = new Gson();
                post = gson.fromJson(message, Wallpapers.class);
            }
//            JSONObject object=new JSONObject(message);
//            type=object.getString("type");
//            msg=object.getString("message");
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e) {
            e.printStackTrace();
        }
        try {

            SettingStore settingStore=SettingStore.getInstance(this);
            if(!TextUtils.isEmpty(type) && type.equalsIgnoreCase("3")){
                if(settingStore.isPushEnable()) {
                    EventManager.sendEvent(EventManager.LBL_PUSH, EventManager.ATR_KEY_PUSH, EventManager.ATR_VALUE_DISPLAY);
                    displayNotification(this, null, null, null, msg_display);
                }
            }else if(!TextUtils.isEmpty(type) && type.equalsIgnoreCase("4")){
                startSplashDownload(post);
            }else if(!TextUtils.isEmpty(type) && type.equalsIgnoreCase("5")){
                sendNotification(post,category,temp_img_display);
            }else if(!TextUtils.isEmpty(type) && type.equalsIgnoreCase("6")){
                // normal image with text notification
                if(settingStore.isPushEnable()) {
                    sendNotification(post, category, temp_img_display);
                }
            }else {
                if(settingStore.isPushEnable()) {
                    sendNotification(post, category,temp_img_display);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = 0x454544;
            notificationBuilder.setColor(color);
            return R.mipmap.ic_launcher_small;

        } else {
            return R.mipmap.ic_launcher;
        }
    }



    private void displayNotification(Context context, Wallpapers post, Category category, Bitmap bitmapImage, String msg){
        int notificationId= 0;
        try {
            notificationId = Integer.parseInt(post.getPostId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            notificationId = Integer.parseInt(category.getCat_id());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
            String CHANNEL_ID = "4k_wallpaper_04";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        context.getResources().getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationBuilder = new NotificationCompat.Builder(context,CHANNEL_ID);
                channel.setDescription(context. getResources().getString(R.string.app_name));
                notificationManager.createNotificationChannel(channel);
            }

            Intent intent = new Intent(context, NotificationAlertActivity.class);
            if(post!=null && bitmapImage!=null){
                Bundle bundle = new Bundle();
                bundle.putSerializable("post", post);
                intent.putExtra("bundle", bundle);
            }else if(category!=null){
                Bundle bundle = new Bundle();
                bundle.putSerializable("category", category);
                intent.putExtra("bundle", bundle);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_launcher);

            String title= CommonFunctions.capitalizeSentence(context.getResources().getString(R.string.app_name));
            if(!TextUtils.isEmpty(title_display))
                title=title_display;

            if(TextUtils.isEmpty(msg_display))
                msg="Click to download and explore more 4K Wallpaper";

            try {
                if(bitmapImage==null){
                    if(!TextUtils.isEmpty(temp_text_display)){
                        msg=temp_text_display;
                    }else {
                        int position = getRandomNumber();
                        msg = sampleText[position];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            notificationBuilder.setSmallIcon(getNotificationIcon(notificationBuilder))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setLargeIcon(bitmap)
                    //                .setSmallIcon(getNotificationIcon())
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    //                .setColor(getColor(R.color.placeholder_grey_20))
                    //                .setVibrate(new long[] { 1000, 1000, 1000 })
                    .setDefaults(RingtoneManager.TYPE_NOTIFICATION)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            if(bitmapImage!=null)
                notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmapImage).setSummaryText(msg));

            notificationManager.notify(notificationId, notificationBuilder.build());

        } catch (Exception e) {
            if(category!=null)
                EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH_CATEGORY,EventManager.ATR_VALUE_NOT_DISPLAY);
            else if(post!=null)
                EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_IMAGE_PUSH,EventManager.ATR_VALUE_NOT_DISPLAY);
            else
                EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH,EventManager.ATR_VALUE_NOT_DISPLAY);
            e.printStackTrace();
        }catch (Error e) {
            if(category!=null)
                EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH_CATEGORY,EventManager.ATR_VALUE_NOT_DISPLAY);
            else if(post!=null)
                EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_IMAGE_PUSH,EventManager.ATR_VALUE_NOT_DISPLAY);
            else
                EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH,EventManager.ATR_VALUE_NOT_DISPLAY);
            e.printStackTrace();
        }
    }

    private void sendNotification(final Wallpapers post, final Category category, final String temp_img_display) {

        class sendNotificationTask extends AsyncTask<String,Void,Bitmap> {
            Context context;
            private sendNotificationTask(Context c) {
                this.context=c;
            }
            @Override
            protected Bitmap doInBackground(String... strings) {
                Bitmap bitmapImage = null;
                try {
                    String path="";

                    if(category!=null) {
                        path = CommonFunctions.getDomainImages() + "category/new/" + category.getImage_new();
                        if (category.getImage_new().startsWith("http")) {
                            path = category.getImage_new();
                        }
                    }else if(!TextUtils.isEmpty(temp_img_display)){
                        path = CommonFunctions.getDomainImages() + "img_noti/" + temp_img_display;
                        if (temp_img_display.startsWith("http")) {
                            path = temp_img_display;
                        }
                    }else{
                        path = CommonFunctions.getDomainImages() + "small/" + post.getImg();
                        if (post.getImg().startsWith("http")) {
                            path = post.getImg();
                        }
                    }

                    bitmapImage = Glide
                            .with(FirebaseMessagingService.this)
                            .asBitmap()
                            .load(path)
                            .submit()
                            .get();

                    if(category!=null)
                        EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH_CATEGORY,EventManager.ATR_VALUE_SUCCESS);
                    else
                        EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_IMAGE_PUSH,EventManager.ATR_VALUE_SUCCESS);
                } catch (InterruptedException e) {
                    if(category!=null)
                        EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH_CATEGORY,EventManager.ATR_VALUE_FAIL);
                    else
                        EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_IMAGE_PUSH,EventManager.ATR_VALUE_FAIL);
                    e.printStackTrace();
                } catch (Exception e) {
                    if(category!=null)
                        EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH_CATEGORY,EventManager.ATR_VALUE_FAIL);
                    else
                        EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_IMAGE_PUSH,EventManager.ATR_VALUE_FAIL);
                    e.printStackTrace();
                }
                return bitmapImage;
            }

            @Override
            protected void onPostExecute(Bitmap bitmapImage) {
                super.onPostExecute(bitmapImage);
                if(category!=null)
                    EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH_CATEGORY,EventManager.ATR_VALUE_DISPLAY);
                else
                    EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_IMAGE_PUSH,EventManager.ATR_VALUE_DISPLAY);

                displayNotification(FirebaseMessagingService.this,post,category,bitmapImage,msg_display);
            }
        }
        new sendNotificationTask(this).execute();
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        final SettingStore settings = SettingStore.getInstance(FirebaseMessagingService.this);
        settings.setIsFcmSend(false);
        settings.setFCM(token);

        FirebaseInstanceIDService.sendRegistrationToServer(FirebaseMessagingService.this);
    }

    private void startSplashDownload(Wallpapers post) {

            String path = CommonFunctions.getDomainImages()+"splash/" + post.getImg();
            if (post.getImg().startsWith("http")) {
                path = post.getImg();
            }
            String dst;
            dst = CommonFunctions.createEventDirCache() +"/"+ post.getImg();
            File file=new File(dst);
            if(file!=null && file.exists())
                return;
            initializeDownload(dst, path);
    }

    private ThinDownloadManager downloadManagerThin;
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 1;
    private void initializeDownload(String pathToDownload,String downloadURL){

        try {
            downloadManagerThin = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);

            RetryPolicy retryPolicy = new DefaultRetryPolicy();

            Uri downloadUri = Uri.parse(downloadURL);
            Uri destinationUri = Uri.parse(pathToDownload);
            final DownloadRequest downloadRequest1 = new DownloadRequest(downloadUri)
                    .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                    .setRetryPolicy(retryPolicy)
                    .setDownloadContext("Download1")
                    .setStatusListener(new DownloadStatusListenerV1() {
                        @Override
                        public void onDownloadComplete(DownloadRequest downloadRequest) {

                        }

                        @Override
                        public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {

                        }

                        @Override
                        public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {

                        }
                    });

            int downloadId1=0;
            if (downloadManagerThin.query(downloadId1) == com.thin.downloadmanager.DownloadManager.STATUS_NOT_FOUND) {
                downloadId1 = downloadManagerThin.add(downloadRequest1);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private int getRandomNumber(){
        final int min = 0;
        final int max = sampleText.length;
        final int random = new Random().nextInt((max - min) + 1) + min;
        return random;
    }
}
