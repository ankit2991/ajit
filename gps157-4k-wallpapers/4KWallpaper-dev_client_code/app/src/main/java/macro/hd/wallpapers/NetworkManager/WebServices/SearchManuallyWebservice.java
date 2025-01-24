package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Observable;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Model.WallInfoModel;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.OperationIds;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Utilily.Logger;


/**
 * Created by hungama on 1/9/16.
 */
public class SearchManuallyWebservice extends NetworkCommunicationManager {

	private String page;
	private Context context;

	public SearchManuallyWebservice(Context context, String api, String page, String device_id, String search, String used_ids, IWebServiceCallback callback) {
		super(context, api, callback);
		this.context=context;
		this.page=page;
		addRequestParameters("page",page);
		addRequestParameters("device_id",device_id);
		addRequestParameters("keyword",search);
		addRequestParameters("used_ids",used_ids);
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
	public WallInfoModel onResponseReceived(String in) throws Exception {
		return parseResponse(in);
	}

	public static WallInfoModel parseResponse(String response){

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
