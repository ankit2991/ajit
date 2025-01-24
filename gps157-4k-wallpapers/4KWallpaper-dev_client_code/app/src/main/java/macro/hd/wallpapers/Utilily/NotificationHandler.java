package macro.hd.wallpapers.Utilily;

/**
 * Created by hungam on 26/7/18.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.widget.Toast;

import macro.hd.wallpapers.Interface.Activity.NotificationAlertActivity;
import macro.hd.wallpapers.R;

import java.util.ArrayList;

public class NotificationHandler {
    // Notification handler singleton
    private static NotificationHandler nHandler;
    private static NotificationManager mNotificationManager;
    private int NOTIFICATION_ID=10;
    private int NOTIFICATION_EXPANDED_ID=11;
    NotificationCompat.Builder mBuilder;

    private NotificationHandler () {}


    /**
     * Singleton pattern implementation
     * @return
     */
    public static  NotificationHandler getInstance(Context context) {
        if(nHandler == null) {
            nHandler = new NotificationHandler();
            mNotificationManager =
                    (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return nHandler;
    }


    /**
     * Shows a simple notification
     * @param context aplication context
     */
    public void createSimpleNotification(Context context,String text) {

//        Notification notification = new Notification.InboxStyle(mBuilder)
//                .addLine("First Message")
//                .addLine("Second Message")
//                .addLine("Third Message")
//                .addLine("Fourth Message")
//                .setBigContentTitle("Here Your Messages")
//                .setSummaryText("+3 more")
//                .build();

        if(mBuilder!=null){
            //Second time
            mBuilder.setContentTitle(context.getResources().getString(R.string.app_name));
            mBuilder.setContentText(text);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }else{
            // Creates an explicit intent for an Activity
            Intent resultIntent = new Intent(context, NotificationAlertActivity.class);

            // Creating a artifical activity stack for the notification activity
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(NotificationAlertActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            // Pending intent to the notification manager
            PendingIntent resultPending = stackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            // Building the notification
            mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                    .setContentTitle( context.getResources().getString(R.string.label_simple_notification)) // main title of the notification
                    .setContentText( context.getResources().getString(R.string.label_simple_notification_text)) // notification text
                    .setContentIntent(resultPending); // notification intent

            // mId allows you to update the notification later on.
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    NotificationCompat.Builder nBuilderExpan;
    NotificationCompat.InboxStyle inboxStyle;

    public void createExpandableNotification (Context context, ArrayList<String> msgds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Building the expandable content

            if(nBuilderExpan==null){

                Intent resultIntent = new Intent(context, NotificationAlertActivity.class);

                // Creating a artifical activity stack for the notification activity
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(NotificationAlertActivity.class);
                stackBuilder.addNextIntent(resultIntent);

                // Pending intent to the notification manager
                PendingIntent resultPending = stackBuilder
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                inboxStyle = new NotificationCompat.InboxStyle();

                inboxStyle.setBigContentTitle(context.getResources().getString(R.string.app_name));

                // Building the notification
                nBuilderExpan = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                        .setContentTitle(context.getResources().getString(R.string.app_name)) // title of notification
                        .setStyle(inboxStyle).setContentIntent(resultPending); // adds the expandable content to the notification

                if(msgds!=null && msgds.size()==1)
                    nBuilderExpan.setContentText(msgds.get(0));

                    for (String line : msgds) {
                        Logger.e("noti line", "" + line);
                        inboxStyle.addLine(line);
                    }


            }else{
                for (String line : msgds) {
                    inboxStyle.addLine(line);
                }
//                nBuilderExpan.setStyle(inboxStyle);
            }


            mNotificationManager.notify(NOTIFICATION_EXPANDED_ID, nBuilderExpan.build());

        } else {
            Toast.makeText(context, context.getResources().getString(R.string.label_cannot_show), Toast.LENGTH_LONG).show();
        }
    }


//    /**
//     * Show a determinate and undeterminate progress notification
//     * @param context, activity context
//     */
//    public void createProgressNotification (final Context context) {
//
//        // used to update the progress notification
//        final int progresID = new Random().nextInt(1000);
//
//        // building the notification
//        final NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.refresh)
//                .setContentTitle("Progres notification")
//                .setContentText("Now waiting")
//                .setTicker("Progress notification created")
//                .setUsesChronometer(true)
//                .setProgress(100, 0, true);
//
//
//
//        AsyncTask<Integer, Integer, Integer> downloadTask = new AsyncTask<Integer, Integer, Integer>() {
//            @Override
//            protected void onPreExecute () {
//                super.onPreExecute();
//                mNotificationManager.notify(progresID, nBuilder.build());
//            }
//
//            @Override
//            protected Integer doInBackground (Integer... params) {
//                try {
//                    // Sleeps 2 seconds to show the undeterminated progress
//                    Thread.sleep(5000);
//
//                    // update the progress
//                    for (int i = 0; i < 101; i+=5) {
//                        nBuilder
//                                .setContentTitle("Progress running...")
//                                .setContentText("Now running...")
//                                .setProgress(100, i, false)
//                                .setSmallIcon(R.drawable.download)
//                                .setContentInfo(i + " %");
//
//                        // use the same id for update instead created another one
//                        mNotificationManager.notify(progresID, nBuilder.build());
//                        Thread.sleep(500);
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                return null;
//            }
//
//
//            @Override
//            protected void onPostExecute (Integer integer) {
//                super.onPostExecute(integer);
//
//                nBuilder.setContentText("Progress finished :D")
//                        .setContentTitle("Progress finished !!")
//                        .setTicker("Progress finished !!!")
//                        .setSmallIcon(R.drawable.accept)
//                        .setUsesChronometer(false);
//
//                mNotificationManager.notify(progresID, nBuilder.build());
//            }
//        };
//
//        // Executes the progress task
//        downloadTask.execute();
//    }


//    public void createButtonNotification (Context context) {
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            // Prepare intent which is triggered if the  notification button is pressed
//            Intent intent = new Intent(context, AlertActivity.class);
//            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
//            // Building the notifcation
//            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
//                    .setSmallIcon(R.drawable.ic_launcher) // notification icon
//                    .setContentTitle("Button notification") // notification title
//                    .setContentText("Expand to show the buttons...") // content text
//                    .setTicker("Showing button notification") // status bar message
//                    .addAction(R.drawable.accept, "Accept", pIntent) // accept notification button
//                    .addAction(R.drawable.cancel, "Cancel", pIntent); // cancel notification button
//
//            mNotificationManager.notify(1001, nBuilder.build());
//
//        } else {
//            Toast.makeText(context, "You need a higher version", Toast.LENGTH_LONG).show();
//        }
//    }
}
