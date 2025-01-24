package macro.hd.wallpapers.fcm;

/**
 * Created by Rajus on 9/11/2016.
 */

import android.content.Context;
import android.text.TextUtils;

import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.IModelBase;


//Class extending FirebaseInstanceIdService
public class FirebaseInstanceIDService {

    private static final String TAG = "MyFirebaseIIDService";

    public static void sendRegistrationToServer(final Context context) {

        final SettingStore settings= SettingStore.getInstance(context);
        String token = settings.getFCM();

        if(TextUtils.isEmpty(token))
            return;

        if (settings.getIsFcmSend())
            return;

        if (TextUtils.isEmpty(settings.getUserID()))
            return;

            RequestManager rewardManager=new RequestManager(context);
            rewardManager.updateGCMtokenService(CommonFunctions.getHardwareId(context),token, new NetworkCommunicationManager.CommunicationListnerNew() {
                @Override
                public void onSuccess(IModel response, int operationCode) {
                    IModelBase base_model= (IModelBase) response;
                    Logger.e(TAG,"" + base_model.getStatus());
                    Logger.e(TAG,"" + base_model.getMsg());
                    settings.setIsFcmSend(true);
                }

                @Override
                public void onStartLoading() {

                }

                @Override
                public void onFail(WebServiceError errorMsg) {

                }
            });

    }
}