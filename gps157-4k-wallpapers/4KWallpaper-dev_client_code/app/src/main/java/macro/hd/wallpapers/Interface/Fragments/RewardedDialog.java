package macro.hd.wallpapers.Interface.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.RewardDialogCallback;

/**
 * Custom alert dialog which can be used all over application.
 */
public class RewardedDialog extends Dialog {

    public RewardedDialog(Activity a, RewardDialogCallback rewardDialogCallback) {
        super(a);
        this.rewardDialogCallback=rewardDialogCallback;
    }

    RewardDialogCallback rewardDialogCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_reward_video);

        TextView btn_like_ads = (TextView) findViewById(R.id.btn_like_ads);
        btn_like_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rewardDialogCallback!=null)
                    rewardDialogCallback.setLikeAdsListener(RewardedDialog.this);
            }
        });


        TextView btn_remove_Ad = (TextView) findViewById(R.id.btn_remove_Ad);
        btn_remove_Ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rewardDialogCallback!=null)
                    rewardDialogCallback.setRemoveAdListener(RewardedDialog.this);
            }
        });
    }

    /**
     * Sets if dialog is cancelable or not.
     *
     * @param isCancelable
     */
    public void setIsCancelable(boolean isCancelable) {
        setCancelable(isCancelable);
    }
}
