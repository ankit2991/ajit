package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;

import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.IModelBase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONException;

import java.io.InputStream;
import java.util.Observable;

public class UpdateUserProWebService extends NetworkCommunicationManager {

    private final static String TAG = UpdateUserProWebService.class.getSimpleName();
    Context context;

    public UpdateUserProWebService(Context activity, String device_id, String user_id, String in_app_purchase_id, String purchaseToken, IWebServiceCallback callback ) {
        super(activity, CommonFunctions.getDomain()+ AppConstant.URL_USER_UPDATE, callback);
        this.context=activity;
        addRequestParameters("device_id",device_id);
        addRequestParameters("user_id",user_id);
        addRequestParameters("in_app_purchase_id",in_app_purchase_id);
        addRequestParameters("purchaseToken",purchaseToken);
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

        IModelBase styleListRider = new Gson().fromJson(
                in, new TypeToken<IModelBase>() {
                }.getType());

        return styleListRider;
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
