package macro.hd.wallpapers.Interface.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;


/**
 * Created by hungama on 4/9/15.
 */
public class NotificationAlertActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			Intent intent;
			Logger.e("AlertActivity","" + WallpapersApplication.isAppRunning);
			if(WallpapersApplication.isAppRunning){
				intent = new Intent(this, MainNavigationActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
						Intent.FLAG_ACTIVITY_SINGLE_TOP);
			}else{
				intent = new Intent(this, SplashActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			}

		if(getIntent()!=null && getIntent().getExtras()!=null) {

			Bundle bundle = getIntent().getBundleExtra("bundle");
			if (bundle != null) {
				intent.putExtras(bundle);
				if(bundle.containsKey("post")){
					EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_IMAGE_PUSH,EventManager.ATR_VALUE_OPEN);
				}else if(bundle.containsKey("category")){
					EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH_CATEGORY,EventManager.ATR_VALUE_OPEN);
				}
			}
		}else{
			EventManager.sendEvent(EventManager.LBL_PUSH,EventManager.ATR_KEY_PUSH,EventManager.ATR_VALUE_OPEN);
		}

		startActivity(intent);
		finish();
	}
}
