package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.IModelBase;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Observable;

public class SendAllDataWebService extends NetworkCommunicationManager {

    private final static String TAG = SendAllDataWebService.class.getSimpleName();
    Context context;

    public SendAllDataWebService(Context activity, String like,String view,String download,String like_live,String exclusive_live,String search_keyword, String device_id, IWebServiceCallback callback ) {
        super(activity, CommonFunctions.getDomain()+ AppConstant.URL_ALL_DATA, callback);
        this.context=activity;
        SettingStore settingStore= SettingStore.getInstance(context);

        addRequestParameters("like",like);
        addRequestParameters("like_live",like_live);
        addRequestParameters("exclusive_live",exclusive_live);
        addRequestParameters("download",download);
        addRequestParameters("device_id",device_id);
        addRequestParameters("view",view);
        addRequestParameters("search_keyword",search_keyword);

        addRequestParameters("unlike",settingStore.getUnLikeCount());
        addRequestParameters("unlike_live",settingStore.getUnLikeCountLive());
        addRequestParameters("unlike_exclusive",settingStore.getUnLikeCountExclusive());
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
    public IModel onResponseReceived(String in) throws Exception {

        Gson gson = new Gson();
        IModelBase userInfoModel= gson.fromJson(in, IModelBase.class);

        return userInfoModel;
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
