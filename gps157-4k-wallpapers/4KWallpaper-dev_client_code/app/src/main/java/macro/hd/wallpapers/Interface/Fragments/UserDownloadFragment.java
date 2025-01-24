package macro.hd.wallpapers.Interface.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.Interface.Adapters.AllInOneAdapter;
import macro.hd.wallpapers.Interface.Activity.MyDownloadActivity;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.SpacesItemDecoration;
import macro.hd.wallpapers.Model.Wallpapers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class UserDownloadFragment extends BaseFragment {

    private List<Wallpapers> movieList = new ArrayList<Wallpapers>();
    private RecyclerView recyclerView;
    private AllInOneAdapter adapter;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        rootView.findViewById(R.id.txt_path).setVisibility(View.VISIBLE);
        showDialogue();
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void getData(){

        if(!isPermissionGranted()){
            return;
        }

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
                    movieList.addAll(FetchImages());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (getActivity() == null || getActivity().isFinishing())
                    return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() == null || getActivity().isFinishing())
                            return;
                        try {
                            dismissDialogue();
                            fillupAdapter();
                            removeRefresh();
                            ((TextView)rootView.findViewById(R.id.txt_path)).setText(CommonFunctions.getSavedFilePath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }.start();
    }

    private ArrayList<Wallpapers> FetchImages() {

        ArrayList<Wallpapers> filenames = new ArrayList<Wallpapers>();
        String path = CommonFunctions.getSavedFilePath();

        File directory = new File(path);
        File[] files = directory.listFiles();

    Logger.e("UserDownloadFragment","before");
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                Logger.e("UserDownloadFragment","under");
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    }else {
                        return f2.lastModified()<f1.lastModified()?-1:
                                f2.lastModified()>f1.lastModified()?1:0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        Logger.e("UserDownloadFragment","after");
        for (int i = 0; i < files.length; i++) {
            if(files[i].isFile()){
                String file_name = files[i].getAbsolutePath();
                Logger.e("file_name",""+file_name);
                // you can store name to arraylist and use it later
                Wallpapers post=new Wallpapers();
                post.setPostId("-100");
                post.setImg(file_name);
                filenames.add(post);
            }
        }
        return filenames;
    }


    private void fillupAdapter() {

        Logger.e("results.size()", "" + movieList.size());
        Logger.e("difference", "" + (movieList.size()));

        if (movieList != null && movieList.size() > 0) {

            ((MyDownloadActivity)getActivity()).displayBannerAd();
            rootView.findViewById(R.id.rl_no_content).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            if (adapter == null) {
                adapter = new AllInOneAdapter(getActivity(), movieList,2);
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
            if(adapter!=null){
                adapter.notifyDataSetChanged();
            }
            noDataLayout(getActivity().getResources().getString(R.string.no_found)+ CommonFunctions.getSavedFilePath()+"\"");
        }
    }

    private void noDataLayout(String msg) {
        String values[] = new String[]{msg};
        rootView.findViewById(R.id.txt_path).setVisibility(View.GONE);
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

    public void showDialogue() {
        rootView.findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
    }

    public void dismissDialogue() {
        rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
    }


    private static final int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 1000;
    private boolean isPermissionGranted() {
        try {
            if (Build.VERSION.SDK_INT >= 23){
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions( //Method of Fragment
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSIONS_CODE_WRITE_STORAGE
                    );
                    return false;
                } else {
                    return true;
                }
            }else
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.e("onRequestPermissionsResult", "Activity");
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE) {
            if (grantResults!=null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getData();
            }else if (grantResults!=null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.allow_per),Toast.LENGTH_SHORT).show();
                isPermissionGranted();
            }
        }else{
        }
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
