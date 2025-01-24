package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

public class C0468f {


    public C0469a f1783a;


    public float f1784b = 0.0f;


    public int f1785c;


    public float f1786d;


    public Context f1787e;


    public int f1788f;


    public int f1790h;


    public boolean f1792j;
    private LinearLayout f1801j;

    public class C0469a extends Dialog {


        public View f1793b;


        public C0466d f1794c;


        public View f1795d;


        public TextView f1796e;


        public TextView f1797f;


        public String f1798g;


        public String f1799h;


        public FrameLayout f1800i;


        public int f1804m = -1;


        public int f1805n = -1;

        public C0469a(Context context) {
            super(context);
        }


        public final void mo2341a(View view) {
            if (view != null) {
                this.f1800i.addView(view, new LayoutParams(-2, -2));
            }
        }


        public void mo2342b(View view) {
            if (view != null) {
                if (view instanceof C0466d) {
                    this.f1794c = (C0466d) view;
                }
                this.f1795d = view;
                if (isShowing()) {
                    this.f1800i.removeAllViews();
                    this.f1800i.addView(view, new LayoutParams(-2, -2));
                }
            }
        }

        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            requestWindowFeature(1);
            setContentView(R.layout.kprogresshud_hud);
            Window window = getWindow();
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.addFlags(2);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = C0468f.this.f1784b;
            attributes.gravity = 17;
            window.setAttributes(attributes);
            setCanceledOnTouchOutside(false);
            f1801j = (LinearLayout) findViewById(R.id.background);
            this.f1800i = (FrameLayout) findViewById(R.id.container);
            mo2341a(this.f1795d);
            C0466d dVar = this.f1794c;
            if (dVar != null) {
                ((C0477m) dVar).f1819c = (int) (83.0f / ((float) C0468f.this.f1788f));
            }
            this.f1796e = (TextView) findViewById(R.id.label);
            String str = this.f1798g;
            int i = this.f1804m;
            this.f1798g = str;
            this.f1804m = i;
            TextView textView = this.f1796e;
            if (textView != null) {
                if (str != null) {
                    textView.setText(str);
                    this.f1796e.setTextColor(i);
                    this.f1796e.setVisibility(0);
                } else {
                    textView.setVisibility(8);
                }
            }
            this.f1797f = (TextView) findViewById(R.id.details_label);
            String str2 = this.f1799h;
            int i2 = this.f1805n;
            this.f1799h = str2;
            this.f1805n = i2;
            TextView textView2 = this.f1797f;
            if (textView2 == null) {
                return;
            }
            if (str2 != null) {
                textView2.setText(str2);
                this.f1797f.setTextColor(i2);
                this.f1797f.setVisibility(0);
                return;
            }
            textView2.setVisibility(8);
        }
    }

    public enum C0470b {
        SPIN_INDETERMINATE,
        PIE_DETERMINATE,
        ANNULAR_DETERMINATE,
        BAR_DETERMINATE
    }

    public C0468f(Context context) {
        this.f1787e = context;
        this.f1783a = new C0469a(context);
        this.f1785c = context.getResources().getColor(R.color.kprogresshud_default_color);
        this.f1788f = 1;
        this.f1786d = 10.0f;
        this.f1790h = 0;
        this.f1792j = false;
        mo2339a(C0470b.SPIN_INDETERMINATE);
    }


    public C0468f mo2339a(C0470b bVar) {
        int ordinal = bVar.ordinal();
        View view = ordinal != 0 ? ordinal != 1 ? ordinal != 2 ? ordinal != 3 ? null : new C0464b(this.f1787e) : new C0463a(this.f1787e) : new C0471g(this.f1787e) : new C0477m(this.f1787e);
        this.f1783a.mo2342b(view);
        return this;
    }

}
