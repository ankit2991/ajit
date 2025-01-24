package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;

import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.IModelBase;
import com.google.gson.Gson;

import org.json.JSONException;

import java.io.InputStream;
import java.util.Observable;

public class DeleteWebService extends NetworkCommunicationManager {

    private final static String TAG = DeleteWebService.class.getSimpleName();
    Context context;

    public DeleteWebService(Context activity, String post_id,String api, IWebServiceCallback callback ) {
        super(activity, api, callback);
        this.context=activity;
        addRequestParameters("post_id",post_id);
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
    public IModel onResponseReceived(String in) throws JSONException, Exception {

        Gson gson = new Gson();
        IModelBase userInfoModel= gson.fromJson(in.toString(), IModelBase.class);

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
