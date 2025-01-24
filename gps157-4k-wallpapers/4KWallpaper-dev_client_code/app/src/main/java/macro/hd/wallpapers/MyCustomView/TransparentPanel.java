package macro.hd.wallpapers.MyCustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TransparentPanel extends LinearLayout {
    private Paint innerPaint;

    public TransparentPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public TransparentPanel(Context context) {
        super(context);
        init();
    }

    private void init() {
        this.innerPaint = new Paint();
        this.innerPaint.setARGB(50, 75, 75, 75);
    }

    public void setInnerPaint(Paint paint) {
        this.innerPaint = paint;
    }

    /* Access modifiers changed, original: protected */
    public void dispatchDraw(Canvas canvas) {
        RectF rectF = new RectF();
        rectF.set(0.0f, 0.0f, (float) getMeasuredWidth(), (float) getMeasuredHeight());
        canvas.drawRoundRect(rectF, 5.0f, 5.0f, this.innerPaint);
        super.dispatchDraw(canvas);
    }
}
