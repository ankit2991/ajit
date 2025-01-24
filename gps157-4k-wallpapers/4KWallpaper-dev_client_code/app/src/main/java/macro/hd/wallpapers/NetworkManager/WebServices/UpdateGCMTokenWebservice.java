package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;

import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModelBase;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Observable;


/**
 * Created by hungama on 1/9/16.
 */
public class UpdateGCMTokenWebservice extends NetworkCommunicationManager {

	public UpdateGCMTokenWebservice(Context context, String device_id, String u_device_token, IWebServiceCallback callback) {
		super(context, CommonFunctions.getDomain()+ AppConstant.URL_UPDATE_GCM_TOKAN, callback);
		addRequestParameters("device_id",device_id);
		addRequestParameters("user_device_token",u_device_token);
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
	public IModelBase onResponseReceived(String in) throws Exception {
		JSONObject object=new JSONObject(in);
		IModelBase baseModel=new IModelBase();
		baseModel.setStatus(object.optString("status"));
		baseModel.setMsg(object.optString("msg"));
		return baseModel;
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
