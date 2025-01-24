package macro.hd.wallpapers.Interface.Fragments.IntroFagment;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.Logger;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

public class FragmentIntro2 extends SlideFragment {

    public FragmentIntro2() {
    }

    public static FragmentIntro2 newInstance() {
        return new FragmentIntro2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_intro2, container,
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

        playVideo();
    }

    VideoView videoView;
    private void playVideo() {
        try {

            videoView = (VideoView) rootView.findViewById(R.id.videoView1);
            videoView.setVisibility(View.VISIBLE);

            //Creating MediaController
//        MediaController mediaController = new MediaController(getActivity());
//        mediaController.setAnchorView(videoView);

            //specify the location of media file

            String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.intro_live;

//            videoUrl= java.net.URLDecoder.decode(videoUrl, "UTF-8");
            Uri uri = Uri.parse(path);

            //Setting MediaController and URI, then starting the videoView
//        videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.requestFocus();

            videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(0,0);
                    mp.setLooping(true);
//                    mp.start();
                }
            });

            videoView.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if(videoView!=null) {
                videoView.stopPlayback();
                videoView.setVideoURI(null);
                videoView=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        rootView=null;
    }

}
