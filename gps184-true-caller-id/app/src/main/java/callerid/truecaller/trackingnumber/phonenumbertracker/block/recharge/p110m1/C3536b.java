package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.RechargePlanActivity;

import java.util.Collections;

public class C3536b extends Fragment {

    public C3541g f15188Y;

    public RecyclerView f15189Z;

    public RelativeLayout f15190a0;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RelativeLayout relativeLayout;
        int i = 0;
        View inflate = layoutInflater.inflate(R.layout.fragment_topup, viewGroup, false);
        this.f15189Z = (RecyclerView) inflate.findViewById(R.id.recyle_Recharge);
        this.f15190a0 = (RelativeLayout) inflate.findViewById(R.id.relNoData);
        this.f15189Z.setLayoutManager(new LinearLayoutManager(getContext()));
        Collections.sort(RechargePlanActivity.f2251F);
        this.f15188Y = new C3541g(RechargePlanActivity.f2251F);
        if (RechargePlanActivity.f2251F.size() > 0) {
            this.f15189Z.setAdapter(this.f15188Y);
            relativeLayout = this.f15190a0;
            i = 8;
        } else {
            relativeLayout = this.f15190a0;
        }
        relativeLayout.setVisibility(i);
        return inflate;
    }
}
