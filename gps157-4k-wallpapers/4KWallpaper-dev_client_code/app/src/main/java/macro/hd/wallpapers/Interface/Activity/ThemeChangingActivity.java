package macro.hd.wallpapers.Interface.Activity;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.DB.SettingStore;


/**
 * Created by hungama on 4/9/15.
 */
public class ThemeChangingActivity extends BaseActivity {

	public final int INTERVAL_TIME=800;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		setContentView(R.layout.activity_theme_changing);

	}

	@Override
	public void onResume() {
		super.onResume();
		SettingStore settingStore=SettingStore.getInstance(this);
		int colorFrom,colorTo;
		if(settingStore.getTheme()==1){
			colorFrom=getResources().getColor(R.color.colorPrimary);
			colorTo=getResources().getColor(R.color.colorPrimary1);
		}else{
			colorFrom=getResources().getColor(R.color.colorPrimary1);
			colorTo=getResources().getColor(R.color.colorPrimary);
		}

		if(settingStore.getTheme()==1) {
			((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.label_apply_dark));
		}else
			((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.label_apply_light));

		changeBgColorWithAnimation(findViewById(R.id.activity_adverts),colorFrom,colorTo);
		changeBgColorWithAnimationText((TextView) findViewById(R.id.txt_title),colorTo,colorFrom);
	}

	private void changeBgColorWithAnimation(final View mainPlayer, int colorFrom, int colorTo) {
		try {
			if (colorFrom == colorTo)
				return;
			ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
			colorAnimation.setDuration(INTERVAL_TIME); // milliseconds
			colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					mainPlayer.setBackgroundColor((int) animator.getAnimatedValue());
				}
			});
			colorAnimation.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animator) {

				}

				@Override
				public void onAnimationEnd(Animator animator) {
					startActivity(new Intent(ThemeChangingActivity.this, MainNavigationActivity.class));
					ThemeChangingActivity.this.overridePendingTransition(0, 0);
					finish();
				}

				@Override
				public void onAnimationCancel(Animator animator) {

				}

				@Override
				public void onAnimationRepeat(Animator animator) {

				}
			});
			colorAnimation.start();
		} catch (Exception e) {
			mainPlayer.setBackgroundColor(colorTo);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						startActivity(new Intent(ThemeChangingActivity.this, MainNavigationActivity.class));
						ThemeChangingActivity.this.overridePendingTransition(0, 0);
						finish();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			},INTERVAL_TIME);

		}
	}


	private void changeBgColorWithAnimationText(final TextView mainPlayer, int colorFrom, int colorTo) {
		try {
			if (colorFrom == colorTo)
				return;
			ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
			colorAnimation.setDuration(INTERVAL_TIME); // milliseconds
			colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					mainPlayer.setTextColor((int) animator.getAnimatedValue());

				}

			});

			colorAnimation.start();
		} catch (Exception e) {
			mainPlayer.setTextColor(colorTo);
		}
	}

}
