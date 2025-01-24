package macro.hd.wallpapers.NetworkManager.WebServices;

import android.content.Context;

import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.HttpStatusException;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.GetLikeModel;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Observable;


/**
 * Created by hungama on 1/9/16.
 */
public class GetLikeWebservice extends NetworkCommunicationManager {

	private Context context;
	public GetLikeWebservice(Context context, String device_id, IWebServiceCallback callback) {
		super(context, CommonFunctions.getDomain()+ AppConstant.URL_GET_LIKE, callback);
		this.context=context;
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
	public GetLikeModel onResponseReceived(String in) throws Exception {
		GetLikeModel conversationListModel = new GetLikeModel();
		try {
			Gson gson = new Gson();
			conversationListModel = gson.fromJson(in, GetLikeModel.class);
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
