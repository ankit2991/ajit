package macro.hd.wallpapers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;

import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.NotifierFactory;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by hungamad on 14/8/17.
 */

public class FileDownloadReceiver extends BroadcastReceiver {
    private static final String TAG = "DownloaderBroadcastReceiver";
    DownloadManager downloadManager;

    @Override
    public void onReceive(final Context context, Intent intent) {
        downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        Logger.i("DownloadService", " End referenceId ::::::: " + referenceId);
        if (!CheckDwnloadStatus(referenceId, context)) {
            String postid = CommonFunctions.isDownloadAdded(context, referenceId + "");
            Logger.e("DownloadService", " postid ::::::: " + postid);
            if (!TextUtils.isEmpty(postid)) {
                Logger.i("DownloadService", " End referenceId ::::::: " + referenceId + " postid " + postid);
                CommonFunctions.updateMessageStatus(context, postid, "100");

                EventNotifier notifier =
                        NotifierFactory.getInstance().getNotifier(
                                NotifierFactory.EVENT_NOTIFIER_DOWNLOAD_INFO);
                notifier.eventNotify(EventTypes.EVENT_DOWNLOAD_COMPLETED, postid);
            } else {

            }
        }
    }

    private boolean CheckDwnloadStatus(long id, Context context) {
        boolean isFailed = false;
        // TODO Auto-generated method stub
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
//        long id = preferenceManager.getLong(strPref_Download_ID, 0);
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(columnReason);
                String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                Logger.e("File Path ::::::::: ", "" + uriString);

                switch (status) {
                    case DownloadManager.STATUS_FAILED:
                        String failedReason = "";
                        isFailed = true;
                        switch (reason) {
                            case DownloadManager.ERROR_CANNOT_RESUME:
                                failedReason = "ERROR_CANNOT_RESUME";
                                break;
                            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                                failedReason = "ERROR_DEVICE_NOT_FOUND";
                                break;
                            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                                failedReason = "ERROR_FILE_ALREADY_EXISTS";
                                break;
                            case DownloadManager.ERROR_FILE_ERROR:
                                failedReason = "ERROR_FILE_ERROR";
                                break;
                            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                                failedReason = "ERROR_HTTP_DATA_ERROR";
                                break;
                            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                                failedReason = "ERROR_INSUFFICIENT_SPACE";
                                break;
                            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                                failedReason = "ERROR_TOO_MANY_REDIRECTS";
                                break;
                            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                                failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                                break;
                            case DownloadManager.ERROR_UNKNOWN:
                                failedReason = "ERROR_UNKNOWN";
                                break;
                        }

                        Logger.s("FAILED: " + failedReason);
                        break;
                    case DownloadManager.STATUS_PAUSED:
                        String pausedReason = "";
                        switch (reason) {
                            case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                                pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                                break;
                            case DownloadManager.PAUSED_UNKNOWN:
                                pausedReason = "PAUSED_UNKNOWN";
                                break;
                            case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                                pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                                break;
                            case DownloadManager.PAUSED_WAITING_TO_RETRY:
                                pausedReason = "PAUSED_WAITING_TO_RETRY";
                                break;
                        }

                        Logger.s("PAUSED: " + pausedReason);
                        break;
                    case DownloadManager.STATUS_PENDING:
                        Logger.s("PENDING");
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        Logger.s("RUNNING");
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        Logger.s("SUCCESSFUL");
                        //                    GetFile();
                        //                    downloadManager.remove(id);
                        break;
                }
            } else {
                isFailed = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isFailed;
    }
}
