package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;
import android.os.Build;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.OperationIds;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.UserInfoModel;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Observable;


/**
 * Created by hungama on 1/9/16.
 */
public class UserInfoWebservice extends NetworkCommunicationManager {

	private Context context;
	public UserInfoWebservice(Context context, String device_id,String oldDeviceId,String fcm_id,String referer, IWebServiceCallback callback) {
		super(context, CommonFunctions.getDomain()+ AppConstant.URL_CATEGORY_LIST, callback);
		this.context=context;
		addRequestParameters("device_id",device_id);
		addRequestParameters("old_device_id",oldDeviceId);
		addRequestParameters("model_number", CommonFunctions.getDeviceName());
		addRequestParameters("os_version", ""+ Build.VERSION.SDK_INT);
		addRequestParameters("fcm_id", ""+fcm_id);
		addRequestParameters("referer", ""+referer);
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
	public UserInfoModel onResponseReceived(String in) throws Exception {
		SettingStore settingStore=SettingStore.getInstance(context);
		settingStore.setSaveResponse(""+ OperationIds.GET_USER_INFO_OPERATION_ID,in);
		return parseResponse(in);
	}

	public static UserInfoModel parseResponse(String response){

		UserInfoModel conversationListModel = new UserInfoModel();
		try {
			Gson gson = new Gson();
			conversationListModel = gson.fromJson(response, UserInfoModel.class);
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
