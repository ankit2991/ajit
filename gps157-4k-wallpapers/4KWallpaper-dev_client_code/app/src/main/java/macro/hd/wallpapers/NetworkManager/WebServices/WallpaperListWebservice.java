package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;
import android.text.TextUtils;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.OperationIds;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.WallInfoModel;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Observable;


/**
 * Created by hungama on 1/9/16.
 */
public class WallpaperListWebservice extends NetworkCommunicationManager {

	private String page;
	private Context context;
	private String category;
	private  String screenType;
	public WallpaperListWebservice(Context context, String api, String page, String device_id, String search, String category, String used_ids, String screenType, int filter, IWebServiceCallback callback) {
		super(context, api, callback);
		this.context=context;
		this.page=page;
		this.category=category;
		this.screenType=screenType;

		addRequestParameters("page",page);
		addRequestParameters("filter",""+filter);
		addRequestParameters("device_id",device_id);
		addRequestParameters("search",search);
		addRequestParameters("used_ids",used_ids);
		if(TextUtils.isEmpty(category) || category.equalsIgnoreCase("-1"))
			Logger.e("ct","fdafd");
		else
			addRequestParameters("category",category);
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

		if((TextUtils.isEmpty(category) || (!TextUtils.isEmpty(category) && category.equalsIgnoreCase("-1"))))
			if(!TextUtils.isEmpty(page) && page.equalsIgnoreCase("1")){
				SettingStore settingStore=SettingStore.getInstance(context);
				if(!TextUtils.isEmpty(screenType))
					settingStore.setSaveResponse(screenType+"_"+OperationIds.GET_POST_LIST_OPERATION_ID,in);
			}

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
