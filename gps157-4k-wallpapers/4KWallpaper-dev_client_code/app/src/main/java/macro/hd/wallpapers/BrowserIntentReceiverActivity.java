package macro.hd.wallpapers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import macro.hd.wallpapers.Interface.Activity.NotificationAlertActivity;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Model.Category;

public class BrowserIntentReceiverActivity extends Activity  {

	public static Uri data = null;

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Logger.e("BrowserIntentReceiverActivity","onCreate");
		handleDeepLink();
	}

	private void handleDeepLink() {
		try {
			if(data == null)
				data = getIntent().getData();

			String scheme = data.getScheme();
			String host = data.getHost();
			String path = data.getEncodedPath();

			String category="";
			try {
				if(!TextUtils.isEmpty(path) && !path.contains("/.*")){
					category=path.substring(path.lastIndexOf("/")+1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Logger.e("BrowserIntentReceiverActivity","Uri ::: " + data.toString() + " scheme:"+scheme+" host:"+host+" category:"+category);
			Category category1=null;
			if(!TextUtils.isEmpty(category)){
				category.replace("/","");
				category1=new Category();
				category1.setCat_id(category);
				category1.setDisplay_name(category);
				category1.setName(category);
			}

//			List<String> params = data.getPathSegments();
//			if(params!=null)
//				for (String str:params) {
//					Logger.e("BrowserIntentReceiverActivity","Param ::: " + str);
//				}

			EventManager.sendEvent(EventManager.LBL_HOME,EventManager.LBL_WEB_DEEPLINK,"OPEN");
			Intent intent = new Intent(this, NotificationAlertActivity.class);
			if(category1!=null) {
				Bundle bundle = new Bundle();
				bundle.putSerializable("category", category1);
				intent.putExtra("bundle", bundle);
			}

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
		} catch (Exception e) {
			Logger.s(" Exception :::::::::: " + e);
			Intent intent = new Intent(this, NotificationAlertActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
		data = null;
		finish();
	}
}
