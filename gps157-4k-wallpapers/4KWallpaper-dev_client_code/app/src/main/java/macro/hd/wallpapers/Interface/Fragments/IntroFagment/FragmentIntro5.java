package macro.hd.wallpapers.Interface.Fragments.IntroFagment;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.Logger;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

public class FragmentIntro5 extends SlideFragment{

    protected static final String TAG = FragmentIntro5.class.getSimpleName();
    public FragmentIntro5() {
    }

    public static FragmentIntro5 newInstance() {
        return new FragmentIntro5();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_intro5, container,
                false);
        return rootView;
    }

    View rootView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView=view;
        CardView card_view=view.findViewById(R.id.card_view);

        try {
            Point windowDimensions = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            Logger.e("DoubleFragment","y:"+windowDimensions.y + " X:"+windowDimensions.x);
            int itemHeight = Math.round(windowDimensions.y * 0.54f);
            int itemWidht = Math.round(windowDimensions.x * 0.56f);

            card_view.getLayoutParams().height = itemHeight;
            card_view.getLayoutParams().width = itemWidht;
        } catch (Exception e) {
            e.printStackTrace();
            card_view.getLayoutParams().width = (int) getResources().getDimension(R.dimen.intro_witdh);
            card_view.getLayoutParams().height = (int) getResources().getDimension(R.dimen.intro_hieght);

        }

        setUpDefault();
        DrawingSurface glSurfaceView = new DrawingSurface(getActivity());
        glSurfaceView.setIntro(true);
        RelativeLayout relativeLayout=rootView.findViewById(R.id.rl_photos);
        relativeLayout.addView(glSurfaceView);
    }

    private void setUpDefault(){

        SettingStore settingStore=SettingStore.getInstance(getActivity());
        if(!settingStore.getEdgeSetDafualt()){
            settingStore.setEdgeSetDafualt(true);
            SharedPreferences.Editor edit = getActivity().getSharedPreferences("borderlightwall", 0).edit();
            edit.putInt("cyclespeed", 10);
            edit.putInt("bordersize", 20);
            edit.putInt("bordersizelockscreen", 20);
            edit.putInt("radiusbottom", 76);
            edit.putInt("radiustop", 96);
            edit.putBoolean("enablenotch", false);
            edit.putBoolean("enableimage", false);
            edit.putInt("notchwidth", 158);
            edit.putInt("notchheight", 80);
            edit.putInt("notchradiustop", 28);
            edit.putInt("notchradiusbottom", 50);
            edit.putInt("notchfullnessbottom", 82);
            edit.putInt("imagevisibilitylocked", 100);
            edit.putInt("imagevisibilityunlocked", 100);
            edit.putInt("imagedesaturationlocked", 0);
            edit.putInt("imagedesaturationunlocked", 50);
            edit.putBoolean("is_dash", false).apply();
            edit.apply();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy");
        rootView = null;
    }
}
