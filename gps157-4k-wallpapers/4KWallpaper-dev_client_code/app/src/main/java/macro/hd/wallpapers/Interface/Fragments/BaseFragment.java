package macro.hd.wallpapers.Interface.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Toast;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;


/**
 * Created by hungam on 19/3/18.
 */

public abstract class BaseFragment extends Fragment{

    SwipeRefreshLayout swiperefresh;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swiperefresh = view.findViewById(R.id.swiperefresh);
        if(swiperefresh!=null) {
//            swiperefresh.setMaterialRefreshListener(new MaterialRefreshListener() {
//                @Override
//                public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
//                    if ((Network.isNetworkAvailable())) {
//                        refreshContent();
//                    } else {
//                        removeRefresh();
//                        Toast.makeText(getActivity(), getResources().getString(R.string.error_msg_no_internet), Toast.LENGTH_SHORT).show();
//                    }
//                    //refreshing...
//
//                }
//
//                @Override
//                public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
//                    //load more refreshing...
//                }
//            });

            swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if ((CommonFunctions.isNetworkAvailable(getActivity()))) {
                        refreshContent();
                    } else {
                        removeRefresh();
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_msg_no_network), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void removeRefresh(){
//        if(swiperefresh!=null)
//            swiperefresh.finishRefresh();
        if(swiperefresh!=null)
            swiperefresh.setRefreshing(false);
    }

    public abstract void refreshContent();
}
