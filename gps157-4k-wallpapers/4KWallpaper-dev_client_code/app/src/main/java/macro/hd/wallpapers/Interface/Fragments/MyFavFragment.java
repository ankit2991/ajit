package macro.hd.wallpapers.Interface.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.Interface.Adapters.AllInOneAdapter;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.Interface.Activity.FavoriteActivity;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.SpacesItemDecoration;
import macro.hd.wallpapers.Model.Wallpapers;

import java.util.ArrayList;
import java.util.List;

public class MyFavFragment extends BaseFragment  {

    private List<Wallpapers> movieList = new ArrayList<Wallpapers>();
    private RecyclerView recyclerView;
    private AllInOneAdapter adapter;
    private View rootView;
    boolean isVideoWall,isExclusiveWall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            isVideoWall=getArguments().getBoolean("isVideoWall");
            isExclusiveWall=getArguments().getBoolean("isExclusiveWall");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_parent_notification, container,
                false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        showDialogue();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void getData(){

        new Thread(){
            @Override
            public void run() {
                super.run();

                try {
                    if(getActivity()==null || getActivity().isFinishing())
                        return;
                    if(movieList!=null){
                        movieList.clear();
                    }
//                    movieList.addAll(Common.getList(getActivity()));
                    if(isVideoWall){
                        movieList.addAll(UserInfoManager.getInstance(getActivity().getApplicationContext()).getUserInfo().getLikeLive());
                    }else if(isExclusiveWall){
                        movieList.addAll(UserInfoManager.getInstance(getActivity().getApplicationContext()).getUserInfo().getLike_exclusive());
                    }else
                        movieList.addAll(UserInfoManager.getInstance(getActivity().getApplicationContext()).getUserInfo().getLike());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (getActivity() != null && !getActivity().isFinishing())
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (getActivity() == null || getActivity().isFinishing())
                                    return;

                                dismissDialogue();
                                fillupAdapter();
                                removeRefresh();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            }
        }.start();
    }

    public void showDialogue() {
        rootView.findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
    }

    public void dismissDialogue() {
        rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
    }

    private void fillupAdapter() {

        Logger.e("results.size()", "" + movieList.size());
        Logger.e("difference", "" + (movieList.size()));

        if (movieList != null && movieList.size() > 0) {

            ((FavoriteActivity)getActivity()).displayBannerAd();
            rootView.findViewById(R.id.rl_no_content).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new AllInOneAdapter(getActivity(), movieList,2);
                adapter.setVideoWall(isVideoWall);
                adapter.setExclusiveWallpaper(isExclusiveWall);
                recyclerView.setHasFixedSize(true);
//                linearLayoutManager = new LinearLayoutManager(getActivity());
                LinearLayoutManager linearLayoutManager = new GridLayoutManager(getActivity(),AppConstant.TILE_SIZE);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(linearLayoutManager);

                int spacing = (int) getResources()
                        .getDimension(R.dimen.content_padding_recycle);
//				int spacingInPixels = 1;
                recyclerView.addItemDecoration(new SpacesItemDecoration(spacing));

//                int spacingInPixels = 1;
//                recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
                recyclerView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        } else {
            noDataLayout(getActivity().getResources().getString(R.string.nodataforfav));
        }
    }

    private void noDataLayout(String msg) {
        if(adapter!=null)
            adapter.notifyDataSetChanged();

        String values[] = new String[]{msg};
        rootView.findViewById(R.id.rl_no_content).setVisibility(View.VISIBLE);
        ((TextView) rootView.findViewById(R.id.txt_no)).setText(values[0]);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void refreshContent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getData();
            }
        }, AppConstant.REFERESH_TIME_OUT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(movieList!=null) {
            movieList.clear();
            movieList=null;
        }
        recyclerView=null;
        adapter=null;
        rootView=null;
        swiperefresh=null;
    }

}
