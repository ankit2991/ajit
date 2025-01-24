package callerid.truecaller.trackingnumber.phonenumbertracker.block.sim_info;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

public class C2559j extends RecyclerView.Adapter<C2559j.C2560a> {

    public String[] f13010d = new String[8];

    public Activity f13011e;

    public LayoutInflater f13012f;

    public class C2560a extends RecyclerView.ViewHolder {

        public final TextView f13013t;

        public final TextView f13014u;

        public final ImageView f13015v;

        public C2560a(C2559j jVar, C2559j jVar2, View view) {
            super(view);
            this.f13013t = (TextView) view.findViewById(R.id.txtTitle);
            this.f13014u = (TextView) view.findViewById(R.id.txtdesc);
            this.f13015v = (ImageView) view.findViewById(R.id.callBtn);
        }
    }

    public C2559j(Activity activity, String[] strArr) {
        this.f13011e = activity;
        this.f13012f = LayoutInflater.from(activity);
        this.f13010d = strArr;
    }

    public int getItemCount() {
        return this.f13010d.length;
    }

    public void onBindViewHolder(C2560a c0Var, final int i) {
        C2560a aVar = (C2560a) c0Var;
        aVar.f13013t.setText(MainActivity1.f2023D[i]);
        aVar.f13014u.setText(this.f13010d[i]);
        aVar.f13015v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.DIAL");
                StringBuilder a = new StringBuilder("tel:");
                a.append(Uri.encode(f13010d[i]));
                intent.setData(Uri.parse(a.toString()));
                f13011e.startActivity(intent);
            }
        });
    }

    public C2560a onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new C2560a(this, this, this.f13012f.inflate(R.layout.item2, viewGroup, false));
    }
}
