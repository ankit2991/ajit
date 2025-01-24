package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;
import android.text.TextUtils;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.OperationIds;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.DoubleWallInfoModel;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Observable;


/**
 * Created by hungama on 1/9/16.
 */
public class DoubleWallListWebservice extends NetworkCommunicationManager {

	private String page;
	private Context context;

	public DoubleWallListWebservice(Context context, String api, String page, String device_id,String used_id, IWebServiceCallback callback) {
		super(context, api, callback);
		this.context=context;
		this.page=page;

		addRequestParameters("avoid_item_ids",used_id);
		addRequestParameters("page",page);
		addRequestParameters("device_id",device_id);
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
	public DoubleWallInfoModel onResponseReceived(String in) throws Exception {

		if(!TextUtils.isEmpty(page) && page.equalsIgnoreCase("1")){
				SettingStore settingStore=SettingStore.getInstance(context);
				settingStore.setSaveResponse(""+OperationIds.GET_DOUBLE_WALL_OPERATION_ID,in);
			}

		return parseResponse(in);
	}

	public static DoubleWallInfoModel parseResponse(String response){

		DoubleWallInfoModel conversationListModel = new DoubleWallInfoModel();
		try {
			Gson gson = new Gson();
			conversationListModel = gson.fromJson(response, DoubleWallInfoModel.class);
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
