package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.AppAddDataList;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import com.adjust.sdk.Adjust;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.fm.MoreAppsAdapter;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.fm.MyPagerAdapter;


public class PlayStoreActivity extends AppCompatActivity {


    protected void onResume() {
        super.onResume();
        Adjust.onResume();
    }
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_store);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setViewForAds();
    }


    private void setViewForAds() {

        AutoScrollViewPager vpPager = (AutoScrollViewPager) findViewById(R.id.viewpager);
        vpPager.startAutoScroll();
        vpPager.setInterval(3000);
        vpPager.setCycle(true);
        vpPager.setStopScrollWhenTouch(true);


        MyPagerAdapter adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        DotsIndicator dotsIndicator = (DotsIndicator) findViewById(R.id.dots_indicator);
        dotsIndicator.setViewPager(vpPager);


        RecyclerView recycler_view = (RecyclerView) findViewById(R.id.recycler_view);

        ArrayList<AppAddDataList> list = new ArrayList<>();
        if (Utils.Big_native != null) {
            if (Utils.Big_native.size() > 0) {
                for (int i = 4; i < Utils.Big_native.size(); i++) {
                    list.add(Utils.Big_native.get(i));
                }
                recycler_view.setLayoutManager(new GridLayoutManager(PlayStoreActivity.this, 3));
                recycler_view.setAdapter(new MoreAppsAdapter(PlayStoreActivity.this, list));
            }
        }
    }
}
