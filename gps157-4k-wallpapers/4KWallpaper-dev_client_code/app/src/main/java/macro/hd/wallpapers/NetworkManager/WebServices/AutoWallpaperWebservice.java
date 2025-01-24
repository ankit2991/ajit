package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;

import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.WallInfoModel;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Observable;


/**
 * Created by hungama on 1/9/16.
 */
public class AutoWallpaperWebservice extends NetworkCommunicationManager {

    private Context context;

    public AutoWallpaperWebservice(Context context, String cat_id, IWebServiceCallback callback) {
        super(context, CommonFunctions.getDomain() + AppConstant.URL_AUTO_WALLPAPER, callback);
        this.context = context;
//        SettingStore preferenceStore = SettingStore.getInstance(context);
        boolean festival = false;
//        boolean festival = preferenceStore.getFromSettingPreferenceBoolean(PreferenceStore.AUTO_FESTIVAL_ENABLE);
        addRequestParameters("cat_ids", cat_id);
        addRequestParameters("festival_enable", festival ? "1" : "0");
        addRequestParameters("device_id", CommonFunctions.getHardwareId(context));
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public String getRequestMethod() {
        return null;
    }

    @Override
    public void addCookie(String name, String value) {

    }

    @Override
    public void removeCookie(String name, String value) {

    }

    @Override
    public void removeAllCookies() {

    }

    @Override
    public void setRequestHeaderField(String name, String value) {

    }

    @Override
    public String getResponseHeaderField(String name) {
        return null;
    }

    @Override
    public void onPreRequest() throws Exception {
    }

    @Override
    public void onCheckStatusCode(int statusCode, String statusMessage) throws HttpStatusException {

    }

    @Override
    public void onConnected() throws Exception {
    }

    @Override
    public InputStream onDataRequested() {
        return null;
    }

    @Override
    public WallInfoModel onResponseReceived(String response) throws Exception {

        WallInfoModel conversationListModel = new WallInfoModel();
        try {
            Gson gson = new Gson();
            conversationListModel = gson.fromJson(response, WallInfoModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conversationListModel;
    }

    @Override
    public WebServiceError onErrorReceived(Throwable cause) {
        return null;
    }

    @Override
    public void setPriority(int priority) {

    }

    @Override
    public Observable getPriorityObservable() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
