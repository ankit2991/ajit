package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.ImageView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

public class C0477m extends ImageView {


    public float f1818b;


    public int f1819c = 83;


    public boolean f1820d;

    public C0477m(Context context) {
        super(context);
        setImageResource(R.drawable.kprogresshud_spinner);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.f1820d = true;
    }

    public void onDetachedFromWindow() {
        this.f1820d = false;
        super.onDetachedFromWindow();
    }

    public void onDraw(Canvas canvas) {
        canvas.rotate(this.f1818b, (float) (getWidth() / 2), (float) (getHeight() / 2));
        super.onDraw(canvas);
    }
}
