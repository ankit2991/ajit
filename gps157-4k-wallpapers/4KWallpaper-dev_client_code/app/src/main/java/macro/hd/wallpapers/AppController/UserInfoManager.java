package macro.hd.wallpapers.AppController;


import android.content.Context;
import android.text.TextUtils;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.NetworkManager.WebServices.UserInfoWebservice;
import macro.hd.wallpapers.NetworkManager.IWebServiceCallback;
import macro.hd.wallpapers.NetworkManager.OperationIds;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.Model.UserInfo;
import macro.hd.wallpapers.Model.UserInfoModel;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.NotifierFactory;

import java.util.ArrayList;

/**
 * controller class to get the data from webservice for login and signup
 *
 * @author simantini.ranade
 */
public class UserInfoManager implements IWebServiceCallback {

	public UserInfo getUserInfo() {
		if(userInfo==null)
			getOfflineInfo();
		else if(userInfo!=null && userInfo.getApp_settings()==null)
			getOfflineInfo();

		return userInfo;
	}

	private UserInfo userInfo;
	private static UserInfoManager sInstance = null;

	private static Context context;
	/**
	 * Singleton instance of loginSignup Manager class. This instance handles the login - sinup
	 * related process, gives calls to Gigya service in case of Gigya login or calls hungama
	 * login/signup and when response is received , UI is notified
	 *
	 * @return {@link UserInfoManager} representing the singleton instance
	 */
	public static UserInfoManager getInstance(Context mContext) {
		if (sInstance == null) {
			sInstance = new UserInfoManager();
			context=mContext;
		}
		return sInstance;
	}

	/**
	 * private constructor
	 */
	private UserInfoManager() {
	}

	@Override
	public void onSuccess(IModel response, int operationCode) {

		UserInfoModel model= (UserInfoModel) response;
		try {
			Logger.e("onSuccess User",""+model.getStatus());
			if(model.getStatus().equalsIgnoreCase("1")){
                userInfo=model.getData();
				SettingStore settingStore=SettingStore.getInstance(context);
				settingStore.setUserID(userInfo.getUser_id());
				settingStore.setLocation(userInfo.getCountry_code());
				if(!TextUtils.isEmpty(userInfo.getIs_pro()) && userInfo.getIs_pro().equalsIgnoreCase("1"))
					settingStore.setIsPro(true);
				else
					settingStore.setIsPro(false);
                EventNotifier notifier =
                        NotifierFactory.getInstance( ).getNotifier(
                                NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                notifier.eventNotify( EventTypes.EVENT_USER_INFO_UPDATED, response );
            }else if(model.getStatus().equalsIgnoreCase("401")){
				EventManager.sendEvent(EventManager.LBL_HOME,EventManager.LBL_USER_NOT_VALID,"Unauthorized");
				WebServiceError error=new WebServiceError();
				error.setDescription(model.getMsg());
				EventNotifier notifier =
						NotifierFactory.getInstance( ).getNotifier(
								NotifierFactory.EVENT_NOTIFIER_USER_INFO);
				notifier.eventNotify( EventTypes.EVENT_USER_NOT_VALID, error );
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeFromGroupExclusive(Wallpapers id){
		if(userInfo==null)
			return;
		if(userInfo.getLike_exclusive()==null)
			userInfo.setLike_exclusive(new ArrayList<Wallpapers>());
		Wallpapers temp = null;
		if(userInfo.getLike_exclusive()!=null && userInfo.getLike_exclusive().size()>0) {
			for (int i = 0; i < userInfo.getLike_exclusive().size(); i++) {
				if(id.getPostId().equalsIgnoreCase(userInfo.getLike_exclusive().get(i).getPostId())) {
					temp = userInfo.getLike_exclusive().get(i);
					break;
				}
			}
		}
		if(temp!=null) {
			userInfo.getLike_exclusive().remove(temp);
		}
	}

	public boolean isFavoriteLive(String groupId){
		if(userInfo!=null) {
			boolean temp=false;
			for (Wallpapers post:userInfo.getLikeLive()) {
				if(post.getPostId().equalsIgnoreCase(groupId)){
					temp=true;
					break;
				}
			}
			return temp;
		}else
			return false;
	}

	public boolean isFavoriteExclusive(String groupId){
		if(userInfo!=null) {
			boolean temp=false;
			for (Wallpapers post:userInfo.getLike_exclusive()) {
				if(post.getPostId().equalsIgnoreCase(groupId)){
					temp=true;
					break;
				}
			}
			return temp;
		}else
			return false;
	}

	public boolean isDownloaded(String groupId){
		if(userInfo!=null && userInfo.getDownload()!=null)
			return userInfo.getDownload().contains(groupId);
		else {
			userInfo.setDownload(new ArrayList<String>());
			userInfo.getDownload().contains(groupId);
			return false;
		}
	}

	public boolean isFavorite(String groupId){
		if(userInfo!=null) {
			boolean temp=false;
			for (Wallpapers post:userInfo.getLike()) {
				if(post.getPostId().equalsIgnoreCase(groupId)){
					temp=true;
					break;
				}
			}
//			return userInfo.getLike().contains(groupId);
			return temp;
		}else
			return false;
	}

	@Override
	public void onError(WebServiceError error) {
		getOfflineInfo();
	}

	@Override
	public void onStartLoading() {
	}


	public void addedToView(String id){
		if(userInfo==null)
			return;
		if(userInfo.getView()==null)
			userInfo.setView(new ArrayList<String>());
		if(userInfo.getView()!=null && !userInfo.getView().contains(id)) {
			userInfo.getView().add(id);
		}
	}

	public void addedToFav(Wallpapers group){
		if(userInfo==null)
			return;
		if(userInfo.getLike()==null)
			userInfo.setLike(new ArrayList<Wallpapers>());
		if(userInfo.getLike()!=null && !userInfo.getLike().contains(group)) {
			userInfo.getLike().add(0,group);
		}
	}

	public void addedToFavExclusive(Wallpapers group){
		if(userInfo==null)
			return;
		if(userInfo.getLike_exclusive()==null)
			userInfo.setLike_exclusive(new ArrayList<Wallpapers>());
		if(userInfo.getLike_exclusive()!=null && !userInfo.getLike_exclusive().contains(group)) {
			userInfo.getLike_exclusive().add(0,group);
		}
	}

	public void addedToFavLive(Wallpapers group){
		if(userInfo==null)
			return;
		if(userInfo.getLikeLive()==null)
			userInfo.setLike_live(new ArrayList<Wallpapers>());
		if(userInfo.getLikeLive()!=null && !userInfo.getLikeLive().contains(group)) {
			userInfo.getLikeLive().add(0,group);
		}
	}

	public void addedToDownload(String id){
		if(userInfo==null)
			return;
		if(userInfo.getDownload()==null)
			userInfo.setDownload(new ArrayList<String>());
		if(userInfo.getDownload()!=null && !userInfo.getDownload().contains(id)) {
			userInfo.getDownload().add(id);
		}
	}

	public void removeFromGroup(Wallpapers id){
		if(userInfo==null)
			return;
		if(userInfo.getLike()==null)
			userInfo.setLike(new ArrayList<Wallpapers>());
		Wallpapers temp = null;
		if(userInfo.getLike()!=null && userInfo.getLike().size()>0) {
			for (int i = 0; i < userInfo.getLike().size(); i++) {
				if(id.getPostId().equalsIgnoreCase(userInfo.getLike().get(i).getPostId())) {
					temp = userInfo.getLike().get(i);
					break;
				}
			}
		}
		if(temp!=null) {
			userInfo.getLike().remove(temp);
		}
	}


	public void getGroupList(String id) {
		if(CommonFunctions.isNetworkAvailable(context)){
			String token= null;
			SettingStore settingStore = null;
			try {
				settingStore=SettingStore.getInstance(context);
				token = settingStore.getFCM();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(TextUtils.isEmpty(token))
				token="";

			String oldDeviceId=settingStore.getOldDeviceID();
			String refere=settingStore.getReferId();

			UserInfoWebservice contentListContentInfoWebService =
					new UserInfoWebservice(context, id,oldDeviceId, token,refere,this);
			contentListContentInfoWebService.setOperationCode(OperationIds.GET_USER_INFO_OPERATION_ID);
			contentListContentInfoWebService.execute();
		}else{
			getOfflineInfo();
		}

	}

	private void getOfflineInfo(){
		SettingStore settingStore=SettingStore.getInstance(context);
		String response=settingStore.getSaveResponse(""+OperationIds.GET_USER_INFO_OPERATION_ID);
		if(!TextUtils.isEmpty(response)) {
			UserInfoModel model = UserInfoWebservice.parseResponse(response);
			onSuccess(model, OperationIds.GET_USER_INFO_OPERATION_ID);
		}else if(!CommonFunctions.isNetworkAvailable(context)){
			EventNotifier notifier =
					NotifierFactory.getInstance( ).getNotifier(
							NotifierFactory.EVENT_NOTIFIER_USER_INFO);
			notifier.eventNotify( EventTypes.EVENT_USER_INTERNET_NOT, response );
		}
	}

	public void removeFromGroupLive(Wallpapers id){
		if(userInfo==null)
			return;
		if(userInfo.getLikeLive()==null)
			userInfo.setLike_live(new ArrayList<Wallpapers>());
		Wallpapers temp = null;
		if(userInfo.getLikeLive()!=null && userInfo.getLikeLive().size()>0) {
			for (int i = 0; i < userInfo.getLikeLive().size(); i++) {
				if(id.getPostId().equalsIgnoreCase(userInfo.getLikeLive().get(i).getPostId())) {
					temp = userInfo.getLikeLive().get(i);
					break;
				}
			}
		}
		if(temp!=null) {
			userInfo.getLikeLive().remove(temp);
		}
	}



}
