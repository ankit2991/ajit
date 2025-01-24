package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.View;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

public class C0463a extends View {


    public Paint f1770b = new Paint(1);


    public Paint f1771c;


    public RectF f1772d;


    public int f1773e = 100;


    public int f1774f = 0;

    public C0463a(Context context) {
        super(context);
        this.f1770b.setStyle(Style.STROKE);
        this.f1770b.setStrokeWidth(3.0f);
        this.f1770b.setColor(-1);
        this.f1771c = new Paint(1);
        this.f1771c.setStyle(Style.STROKE);
        this.f1771c.setStrokeWidth(3.0f);
        this.f1771c.setColor(context.getResources().getColor(R.color.kprogresshud_grey_color));
        this.f1772d = new RectF();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float f = (((float) this.f1774f) * 360.0f) / ((float) this.f1773e);
        canvas.drawArc(this.f1772d, 270.0f, f, false, this.f1770b);
        canvas.drawArc(this.f1772d, f + 270.0f, 360.0f - f, false, this.f1771c);
    }

    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int a = 40;
        setMeasuredDimension(a, a);
    }

    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        int a = 4;
        float f = (float) a;
        this.f1772d.set(f, f, (float) (i - a), (float) (i2 - a));
    }
}
