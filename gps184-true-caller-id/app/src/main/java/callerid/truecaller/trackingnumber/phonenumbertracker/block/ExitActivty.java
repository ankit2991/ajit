package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjust.sdk.Adjust;

import java.util.ArrayList;
import java.util.Collections;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.AppAddDataList;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;


public class ExitActivty extends AppCompatActivity {

    private RecyclerView rec_moreapps;
    private LinearLayoutManager layoutManager;
    private Adapter_moreExit adapter_moreStart;


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Adjust.onResume();

        final FrameLayout app_ad = (FrameLayout) findViewById(R.id.app_ad);
        MaxAdManager.INSTANCE.createNativeAd(
                this,
                app_ad,
                null
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_exit);

        rec_moreapps = findViewById(R.id.exitads);

        layoutManager = new GridLayoutManager(ExitActivty.this, 3);
        rec_moreapps.setLayoutManager(layoutManager);

        ArrayList<AppAddDataList> abc = new ArrayList<>();
        if (Utils.Big_native != null && !Utils.Big_native.isEmpty()) {
            Collections.reverse(Utils.Big_native);
            if (Utils.Big_native.size() >= 3) {
                for (int i = 0; i < 3; i++) {
                    abc.add(Utils.Big_native.get(i));
                }
                adapter_moreStart = new Adapter_moreExit(abc, ExitActivty.this);
                rec_moreapps.setAdapter(adapter_moreStart);
            }
        }

        findViewById(R.id.btnexit).setOnClickListener(view -> {
            finishAffinity();
        });
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
