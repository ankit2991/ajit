package macro.hd.wallpapers.Interface.Activity;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Interface.Fragments.ExclusiveDetailFragment;
import macro.hd.wallpapers.Interface.Fragments.LiveWallDetailFragment;

public class LiveWallDetailActivity extends BaseActivity {
    LiveWallDetailFragment photoDetailActivity;
    ExclusiveDetailFragment exclusiveDetailFragment;
    FragmentManager mFragmentManager;
//    Toolbar toolbar;
boolean isExclusive=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        CommonFunctions.setOverlayAction(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_detail);
        CommonFunctions.updateWindow(LiveWallDetailActivity.this);

        mFragmentManager = getSupportFragmentManager();

        if(getIntent()!=null)
            isExclusive =  getIntent().getBooleanExtra("isExclusive",false);

//        post.setImg("Alcatel31.jpg");
        if(isExclusive) {
            exclusiveDetailFragment = new ExclusiveDetailFragment();
            exclusiveDetailFragment.setArguments(getIntent().getExtras());

            mFragmentManager.beginTransaction().
                    add(R.id.frame_layout, exclusiveDetailFragment).disallowAddToBackStack().
                    commit();
        }else{
            photoDetailActivity = new LiveWallDetailFragment();
            photoDetailActivity.setArguments(getIntent().getExtras());

            mFragmentManager.beginTransaction().
                    add(R.id.frame_layout, photoDetailActivity).disallowAddToBackStack().
                    commit();
        }


    }

    @Override
    public void onBackPressed() {

        try {
            if(isExclusive){
                if(exclusiveDetailFragment.isPhotoVisible()){
                    exclusiveDetailFragment.phoneGone();
                }else
                    super.onBackPressed();
            }else
                super.onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            mFragmentManager.beginTransaction().
                    remove(photoDetailActivity).
                    commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        photoDetailActivity=null;
        try {
            mFragmentManager.beginTransaction().
                    remove(exclusiveDetailFragment).
                    commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        exclusiveDetailFragment=null;
        mFragmentManager=null;
    }
}
