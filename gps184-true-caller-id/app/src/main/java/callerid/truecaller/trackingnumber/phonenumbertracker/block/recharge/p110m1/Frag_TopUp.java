package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.os.Bundle;
import android.util.Log;
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

public class Frag_TopUp extends Fragment {

    public C3541g f15197Y;
    public RecyclerView f15198Z;
    public RelativeLayout f15199a0;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RelativeLayout relativeLayout;
        int i = 0;
        View inflate = layoutInflater.inflate(R.layout.fragment_topup, viewGroup, false);
        this.f15198Z = (RecyclerView) inflate.findViewById(R.id.recyle_Recharge);
        this.f15199a0 = (RelativeLayout) inflate.findViewById(R.id.relNoData);
        this.f15198Z.setLayoutManager(new LinearLayoutManager(getContext()));
        Collections.sort(RechargePlanActivity.f2252G);
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        sb.append(RechargePlanActivity.f2252G.size());
        this.f15197Y = new C3541g(RechargePlanActivity.f2252G);
        if (RechargePlanActivity.f2252G.size() > 0) {
            this.f15198Z.setAdapter(this.f15197Y);
            relativeLayout = this.f15199a0;
            i = 8;
        } else {
            relativeLayout = this.f15199a0;
        }
        relativeLayout.setVisibility(i);
        return inflate;
    }
}
