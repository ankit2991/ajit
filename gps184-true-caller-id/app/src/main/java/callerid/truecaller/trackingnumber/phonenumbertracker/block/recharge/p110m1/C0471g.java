package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.View;

public class C0471g extends View {


    public Paint f1812b = new Paint(1);


    public Paint f1813c;


    public RectF f1814d;


    public int f1815e = 100;


    public int f1816f = 0;

    public C0471g(Context context) {
        super(context);
        this.f1812b.setStyle(Style.FILL_AND_STROKE);
        this.f1812b.setStrokeWidth(0.1f);
        this.f1812b.setColor(-1);
        this.f1813c = new Paint(1);
        this.f1813c.setStyle(Style.STROKE);
        this.f1813c.setStrokeWidth(2.0f);
        this.f1813c.setColor(-1);
        this.f1814d = new RectF();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Canvas canvas2 = canvas;
        canvas2.drawArc(this.f1814d, 270.0f, (((float) this.f1816f) * 360.0f) / ((float) this.f1815e), true, this.f1812b);
        canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2), (float) ((getWidth() / 2) - 4.0f), this.f1813c);
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
        this.f1814d.set(f, f, (float) (i - a), (float) (i2 - a));
    }
}
