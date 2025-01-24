package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.View;

public class C0464b extends View {


    public Paint f1775b = new Paint(1);


    public Paint f1776c;


    public RectF f1777d;


    public RectF f1778e;


    public int f1779f = 100;


    public int f1780g = 0;


    public float f1781h;

    public C0464b(Context context) {
        super(context);
        this.f1775b.setStyle(Style.STROKE);
        this.f1775b.setStrokeWidth(2.0f);
        this.f1775b.setColor(-1);
        this.f1776c = new Paint(1);
        this.f1776c.setStyle(Style.FILL);
        this.f1776c.setColor(-1);
        this.f1781h = 5.0f;
        float f = this.f1781h;
        this.f1778e = new RectF(f, f, ((((float) getWidth()) - this.f1781h) * ((float) this.f1780g)) / ((float) this.f1779f), ((float) getHeight()) - this.f1781h);
        this.f1777d = new RectF();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectF = this.f1777d;
        canvas.drawRoundRect(rectF, rectF.height() / 2.0f, this.f1777d.height() / 2.0f, this.f1775b);
        RectF rectF2 = this.f1778e;
        canvas.drawRoundRect(rectF2, rectF2.height() / 2.0f, this.f1778e.height() / 2.0f, this.f1776c);
    }

    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        setMeasuredDimension(100, 20);
    }

    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        int a = 2;
        float f = (float) a;
        this.f1777d.set(f, f, (float) (i - a), (float) (i2 - a));
    }
}
