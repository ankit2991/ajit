package macro.hd.wallpapers.Interface.Activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;

public class AboutUsActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme();
		setLocale();
		setContentView(R.layout.activity_about_us);

		Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.txtcontact));

		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		final String version = pInfo.versionName; 
		((TextView)findViewById(R.id.tv_version)).setText(getResources().getString(R.string.label_version) +" "+ version);

		findViewById(R.id.txt_terms).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!CommonFunctions.isNetworkAvailable(AboutUsActivity.this)) {
					CommonFunctions.showInternetDialog(AboutUsActivity.this);
					return;
				}
				final Intent i = new Intent(AboutUsActivity.this, Termsactivity.class);
				startActivity(i);
			}
		});

		findViewById(R.id.txt_privacy_policy).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!CommonFunctions.isNetworkAvailable(AboutUsActivity.this)) {
					CommonFunctions.showInternetDialog(AboutUsActivity.this);
					return;
				}
				final Intent i = new Intent(AboutUsActivity.this, Termsactivity.class);
				i.putExtra("privacy",true);
				startActivity(i);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
