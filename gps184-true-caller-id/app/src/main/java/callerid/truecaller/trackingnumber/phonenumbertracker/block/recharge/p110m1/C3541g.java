package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

import java.util.List;

public class C3541g extends RecyclerView.Adapter<C3541g.C3542a> {


    public List<C3540f> f15204d;

    public class C3542a extends RecyclerView.ViewHolder {

        public TextView f15205t;
        public TextView f15206u;
        public TextView f15207v;
        public TextView f15208w;

        public C3542a(C3541g gVar, View view) {
            super(view);
            this.f15206u = (TextView) view.findViewById(R.id.txtPrice);
            this.f15208w = (TextView) view.findViewById(R.id.txtValidity);
            this.f15207v = (TextView) view.findViewById(R.id.txtTalktime);
            this.f15205t = (TextView) view.findViewById(R.id.txtDescriprion);
        }
    }

    public C3541g(List<C3540f> list) {
        this.f15204d = list;
    }


    public int getItemCount() {
        return this.f15204d.size();
    }


    public void onBindViewHolder(C3542a c0Var, int i) {
        String str;
        TextView textView;
        String str2;
        TextView textView2;
        C3542a aVar = (C3542a) c0Var;
        TextView textView3 = aVar.f15206u;
        StringBuilder a = new StringBuilder().append("₹ ");
        a.append(((C3540f) this.f15204d.get(i)).f15203e);
        textView3.setText(a.toString());
        String str3 = "NA";
        if (((C3540f) this.f15204d.get(i)).f15202d.matches(str3)) {
            textView = aVar.f15208w;
            StringBuilder a2 = new StringBuilder().append("Validity : ");
            a2.append(((C3540f) this.f15204d.get(i)).f15202d);
            str = a2.toString();
        } else {
            textView = aVar.f15208w;
            str = ((C3540f) this.f15204d.get(i)).f15202d;
        }
        textView.setText(str);
        if (((C3540f) this.f15204d.get(i)).mo13297a().matches(str3)) {
            textView2 = aVar.f15207v;
            str2 = "Talktime : 0.0";
        } else {
            textView2 = aVar.f15207v;
            StringBuilder a3 = new StringBuilder().append("Talktime : ");
            a3.append(((C3540f) this.f15204d.get(i)).mo13297a());
            str2 = a3.toString();
        }
        textView2.setText(str2);
        aVar.f15205t.setText(((C3540f) this.f15204d.get(i)).f15200b);
    }


    public C3542a onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new C3542a(this, LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recharge_info_layout, null));
    }
}
