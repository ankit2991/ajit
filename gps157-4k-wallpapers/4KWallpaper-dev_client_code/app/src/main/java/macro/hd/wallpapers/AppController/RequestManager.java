package macro.hd.wallpapers.AppController;

import android.content.Context;

import macro.hd.wallpapers.NetworkManager.WebServices.SearchManuallyWebservice;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.NetworkManager.WebServices.AutoWallpaperWebservice;
import macro.hd.wallpapers.NetworkManager.WebServices.DeleteWebService;
import macro.hd.wallpapers.NetworkManager.WebServices.DoubleWallListWebservice;
import macro.hd.wallpapers.NetworkManager.WebServices.GetLikeWebservice;
import macro.hd.wallpapers.NetworkManager.WebServices.WallpaperListWebservice;
import macro.hd.wallpapers.NetworkManager.WebServices.SendAllDataWebService;
import macro.hd.wallpapers.NetworkManager.WebServices.UpdateGCMTokenWebservice;
import macro.hd.wallpapers.NetworkManager.WebServices.UpdateUserProWebService;
import macro.hd.wallpapers.NetworkManager.WebServices.UserFeedbackWebservice;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.OperationIds;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;

/**
 * controller class to get data from webservice or db send it to UI classes
 *
 */
public class RequestManager implements IWebServiceCallback {


	private NetworkCommunicationManager.CommunicationListnerNew<IModel> mCallbackListener;
	Context context;

	public RequestManager(Context context) {
		this.context = context;
	}

	@Override
	public void onSuccess(IModel response, int operationCode) {
		if (mCallbackListener != null)
			mCallbackListener.onSuccess(response, operationCode);
	}

	@Override
	public void onError(WebServiceError error) {
		Logger.e("onError", "PurchaseWebService: on error : " + error);
		if (mCallbackListener != null)
			mCallbackListener.onFail(error);
	}

	@Override
	public void onStartLoading() {
		if (mCallbackListener != null)
			mCallbackListener.onStartLoading();
	}



	public void getPostListService(String api,String page,String device_id,String search,String category,String used_ids,String screenType,int filter, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		WallpaperListWebservice contentListContentInfoWebService =
				new WallpaperListWebservice(context,api, page,device_id,search,category, used_ids,screenType,filter,this);
		contentListContentInfoWebService.setOperationCode(OperationIds.GET_POST_LIST_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void getSearchService(String api,String page,String device_id,String search,String used_ids,NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		SearchManuallyWebservice contentListContentInfoWebService =
				new SearchManuallyWebservice(context,api, page,device_id,search, used_ids,this);
		contentListContentInfoWebService.setOperationCode(OperationIds.GET_SEARCH_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void updateGCMtokenService(String device_id,String u_device_token, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		UpdateGCMTokenWebservice contentListContentInfoWebService =
				new UpdateGCMTokenWebservice(context, device_id,u_device_token, this);
		contentListContentInfoWebService.setOperationCode(OperationIds.UPDATE_GCM_TOKEN_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void sendDataToServer(String like, String view, String download, String like_live, String exclusive_live, String search_keyword, String device_id, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		SendAllDataWebService contentListContentInfoWebService =
				new SendAllDataWebService(context,like,view,download,like_live,exclusive_live,search_keyword,device_id, this);
		contentListContentInfoWebService.setOperationCode(OperationIds.SEND_DATA_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void getLikeWallService(String device_id, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		GetLikeWebservice contentListContentInfoWebService =
				new GetLikeWebservice(context,device_id,this);
		contentListContentInfoWebService.setOperationCode(OperationIds.GET_LIKE_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void getPurchaseService(String device_id, String user_id,String in_app_purchase_id,String purchaseToken, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		UpdateUserProWebService contentListContentInfoWebService =
				new UpdateUserProWebService(context,device_id,user_id,in_app_purchase_id,purchaseToken,this);
		contentListContentInfoWebService.setOperationCode(OperationIds.PURCHASE_API_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void setFeedbackService(String user_id,String message, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		UserFeedbackWebservice contentListContentInfoWebService =
				new UserFeedbackWebservice(context,user_id,message,this);
		contentListContentInfoWebService.setOperationCode(OperationIds.USER_FEEDBACK_API_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void autoWallpaperService(String cat_id, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		AutoWallpaperWebservice contentListContentInfoWebService =
				new AutoWallpaperWebservice(context, cat_id, this);
		contentListContentInfoWebService.setOperationCode(OperationIds.GET_AUTO_WALLPAPER_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void deletePostService(String post_id, String api, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		DeleteWebService contentListContentInfoWebService =
				new DeleteWebService(context,post_id, api,this);
		contentListContentInfoWebService.setOperationCode(OperationIds.GET_DELETE_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

	public void getDoubleWallService(String api,String page,String device_id,String used_id, NetworkCommunicationManager.CommunicationListnerNew callbackListener) {
		mCallbackListener = callbackListener;
		DoubleWallListWebservice contentListContentInfoWebService =
				new DoubleWallListWebservice(context,api, page,device_id,used_id,this);
		contentListContentInfoWebService.setOperationCode(OperationIds.GET_DOUBLE_WALL_OPERATION_ID);
		contentListContentInfoWebService.execute();
	}

}
