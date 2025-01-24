package com.gpsnavigation.maps.gpsroutefinder.routemap.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.IntDef;

import com.gpsnavigation.maps.gpsroutefinder.routemap.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.gpsnavigation.maps.gpsroutefinder.routemap.utility.DisplayUtilsKt.dpToPx;


public class TimelineView extends View {

    private TextPaint mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private String text;
    private int textSize;
    private int textColor;
    private boolean showText = false;
    private Rect mTextBounds = new Rect();
    private Drawable mMarker;
    private int mMarkerSize;
    private boolean mMarkerInCenter;
    private Paint mLinePaint = new Paint();
    private boolean mDrawStartLine = false;
    private boolean mDrawEndLine = false;
    private float mStartLineStartX, mStartLineStartY, mStartLineStopX, mStartLineStopY;
    private float mEndLineStartX, mEndLineStartY, mEndLineStopX, mEndLineStopY;
    private int mStartLineColor;
    private int mEndLineColor;
    private int mLineWidth;
    private int mLineOrientation;
    private int mLineStyle;
    private int mLineStyleDashLength;
    private int mLineStyleDashGap;
    private int mLinePadding;
    private Rect mBounds;
    public TimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     * Gets timeline view type.
     *
     * @param position  the position of current item view
     * @param totalSize the total size of the items
     * @return the timeline view type
     */
    public static int getTimeLineViewType(int position, int totalSize) {
        if (totalSize == 1) {
            return LineType.ONLYONE;
        } else if (position == 0) {
            return LineType.START;
        } else if (position == totalSize - 1) {
            return LineType.END;
        } else {
            return LineType.NORMAL;
        }
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TimelineView);
        mMarker = typedArray.getDrawable(R.styleable.TimelineView_marker);
        mMarkerSize = typedArray.getDimensionPixelSize(R.styleable.TimelineView_markerSize, dpToPx(20, getContext()));
        showText = typedArray.getBoolean(R.styleable.TimelineView_showText, false);
        text = typedArray.getString(R.styleable.TimelineView_text);
        textSize = typedArray.getDimensionPixelSize(R.styleable.TimelineView_textSize, 40);
        textColor = typedArray.getColor(R.styleable.TimelineView_textColor, Color.WHITE);
        mMarkerInCenter = typedArray.getBoolean(R.styleable.TimelineView_markerInCenter, true);
        mStartLineColor = typedArray.getColor(R.styleable.TimelineView_startLineColor, getResources().getColor(android.R.color.darker_gray));
        mEndLineColor = typedArray.getColor(R.styleable.TimelineView_endLineColor, getResources().getColor(android.R.color.darker_gray));
        mLineWidth = typedArray.getDimensionPixelSize(R.styleable.TimelineView_lineWidth, dpToPx(2, getContext()));
        mLineOrientation = typedArray.getInt(R.styleable.TimelineView_lineOrientation, LineOrientation.VERTICAL);
        mLinePadding = typedArray.getDimensionPixelSize(R.styleable.TimelineView_linePadding, 0);
        mLineStyle = typedArray.getInt(R.styleable.TimelineView_lineStyle, LineStyle.NORMAL);
        mLineStyleDashLength = typedArray.getDimensionPixelSize(R.styleable.TimelineView_lineStyleDashLength,dpToPx(8f, getContext()));
        mLineStyleDashGap = typedArray.getDimensionPixelSize(R.styleable.TimelineView_lineStyleDashGap, dpToPx(4f, getContext()));
        typedArray.recycle();

        if (isInEditMode()) {
            mDrawStartLine = true;
            mDrawEndLine = true;
        }

        if (mMarker == null) {
            mMarker = getResources().getDrawable(R.drawable.blue_circle);
        }

        initTimeline();
        initLinePaint();

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Width measurements of the width and height and the inside view of child controls
        int w = mMarkerSize + getPaddingLeft() + getPaddingRight();
        int h = mMarkerSize + getPaddingTop() + getPaddingBottom();

        // Width and height to determine the final view through a systematic approach to decision-making
        int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);

        setMeasuredDimension(widthSize, heightSize);
        initTimeline();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        initTimeline();
    }

    private void initTimeline() {

        int pLeft = getPaddingLeft();
        int pRight = getPaddingRight();
        int pTop = getPaddingTop();
        int pBottom = getPaddingBottom();

        int width = getWidth();// Width of current custom view
        int height = getHeight();

        int cWidth = width - pLeft - pRight;// Circle width
        int cHeight = height - pTop - pBottom;

        int markSize = Math.min(mMarkerSize, Math.min(cWidth, cHeight));

        if (mMarkerInCenter) { //Marker in center is true

            if (mMarker != null) {
                mMarker.setBounds((width / 2) - (markSize / 2), (height / 2) - (markSize / 2), (width / 2) + (markSize / 2), (height / 2) + (markSize / 2));
                mBounds = mMarker.getBounds();
            }

        } else { //Marker in center is false

            if (mMarker != null) {
                mMarker.setBounds(pLeft, pTop, pLeft + markSize, pTop + markSize);
                mBounds = mMarker.getBounds();
            }
        }

        if (mLineOrientation == LineOrientation.HORIZONTAL) {

            if (mDrawStartLine) {
                mStartLineStartX = pLeft;
                mStartLineStartY = mBounds.centerY();
                mStartLineStopX = mBounds.left - mLinePadding;
                mStartLineStopY = mBounds.centerY();
            }

            if (mDrawEndLine) {
                mEndLineStartX = mBounds.right + mLinePadding;
                mEndLineStartY = mBounds.centerY();
                mEndLineStopX = getWidth();
                mEndLineStopY = mBounds.centerY();
            }
        } else {

            if (mDrawStartLine) {
                mStartLineStartX = mBounds.centerX();

                if (mLineStyle == LineStyle.DASHED) {
                    mStartLineStartY = pTop - mLineStyleDashLength;
                } else {
                    mStartLineStartY = pTop;
                }

                mStartLineStopX = mBounds.centerX();
                mStartLineStopY = mBounds.top - mLinePadding;
            }

            if (mDrawEndLine) {
                mEndLineStartX = mBounds.centerX();
                mEndLineStartY = mBounds.bottom + mLinePadding;
                mEndLineStopX = mBounds.centerX();
                mEndLineStopY = getHeight();
            }
        }

        invalidate();
    }

    private void initLinePaint() {
        mLinePaint.setAlpha(0);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(mStartLineColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mLineWidth);

        if (mLineStyle == LineStyle.DASHED)
            mLinePaint.setPathEffect(new DashPathEffect(new float[]{(float) mLineStyleDashLength, (float) mLineStyleDashGap}, 0.0f));
        else
            mLinePaint.setPathEffect(new PathEffect());

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMarker != null) {
            mMarker.draw(canvas);
        }
        if (showText && text!=null) {
            mPaint.setColor(textColor);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
            int textWidth = mTextBounds.right - mTextBounds.left;
            int textHeight = mTextBounds.bottom - mTextBounds.top;
            mPaint.setTextSize(textSize);
            float x =  (float) ((mMarker.getBounds().right)/ 2);
            float y = (mMarker.getBounds().bottom+textHeight+1)/2;
            canvas.drawText(text, x, y, mPaint);
        }
        if (mDrawStartLine) {
            mLinePaint.setColor(mStartLineColor);
            invalidate();
            canvas.drawLine(mStartLineStartX, mStartLineStartY, mStartLineStopX, mStartLineStopY, mLinePaint);
        }

        if (mDrawEndLine) {
            mLinePaint.setColor(mEndLineColor);
            invalidate();
            canvas.drawLine(mEndLineStartX, mEndLineStartY, mEndLineStopX, mEndLineStopY, mLinePaint);
        }
    }

    public Drawable getMarker() {
        return mMarker;
    }

    /**
     * Sets marker.
     *
     * @param marker will set marker drawable to timeline
     */
    public void setMarker(Drawable marker) {
        mMarker = marker;
        initTimeline();
    }

    /**
     * Sets marker.
     *
     * @param marker will set marker drawable to timeline
     * @param color  with a color
     */
    public void setMarker(Drawable marker, int color) {
        mMarker = marker;
        mMarker.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        initTimeline();
    }

    /**
     * Sets marker color.
     *
     * @param color the color
     */
    public void setMarkerColor(int color) {
        mMarker.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        initTimeline();
    }

    /**
     * Sets start line.
     *
     * @param color    the color of the start line
     * @param viewType the view type
     */
    public void setStartLineColor(int color, int viewType) {
        mStartLineColor = color;
        initLine(viewType);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStartLineColor() {
        return mStartLineColor;
    }

    /**
     * Sets end line.
     *
     * @param color    the color of the end line
     * @param viewType the view type
     */
    public void setEndLineColor(int color, int viewType) {
        mEndLineColor = color;
        initLine(viewType);
    }

    public int getEndLineColor() {
        return mEndLineColor;
    }

    public int getMarkerSize() {
        return mMarkerSize;
    }

    /**
     * Sets marker size.
     *
     * @param markerSize the marker size
     */
    public void setMarkerSize(int markerSize) {
        mMarkerSize = markerSize;
        initTimeline();
    }

    public boolean isMarkerInCenter() {
        return mMarkerInCenter;
    }

    public void setMarkerInCenter(boolean markerInCenter) {
        this.mMarkerInCenter = markerInCenter;
        initTimeline();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public int getLineWidth() {
        return mLineWidth;
    }

    /**
     * Sets line width.
     *
     * @param lineWidth the line width
     */
    public void setLineWidth(int lineWidth) {
        mLineWidth = lineWidth;
        initTimeline();
    }

    public int getLineOrientation() {
        return mLineOrientation;
    }

    public void setLineOrientation(int lineOrientation) {
        this.mLineOrientation = lineOrientation;
    }

    public int getLineStyle() {
        return mLineStyle;
    }

    public void setLineStyle(int lineStyle) {
        this.mLineStyle = lineStyle;
        initLinePaint();
    }

    public int getLineStyleDashLength() {
        return mLineStyleDashLength;
    }

    public void setLineStyleDashLength(int lineStyleDashLength) {
        this.mLineStyleDashLength = lineStyleDashLength;
        initLinePaint();
    }

    public int getLineStyleDashGap() {
        return mLineStyleDashGap;
    }

    public void setLineStyleDashGap(int lineStyleDashGap) {
        this.mLineStyleDashGap = lineStyleDashGap;
        initLinePaint();
    }

    public int getLinePadding() {
        return mLinePadding;
    }

    /**
     * Sets line padding
     *
     * @param padding the line padding
     */
    public void setLinePadding(int padding) {
        mLinePadding = padding;
        initTimeline();
    }

    private void showStartLine(boolean show) {
        mDrawStartLine = show;
        initTimeline();
    }

    private void showEndLine(boolean show) {
        mDrawEndLine = show;
        initTimeline();
    }

    /**
     * Init line.
     *
     * @param viewType the view type
     */
    public void initLine(int viewType) {
        if (viewType == LineType.START) {
            showStartLine(false);
            showEndLine(true);
        } else if (viewType == LineType.END) {
            showStartLine(true);
            showEndLine(false);
        } else if (viewType == LineType.ONLYONE) {
            showStartLine(false);
            showEndLine(false);
        } else {
            showStartLine(true);
            showEndLine(true);
        }

        initTimeline();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineOrientation.HORIZONTAL, LineOrientation.VERTICAL})
    public @interface LineOrientation {
        int HORIZONTAL = 0;
        int VERTICAL = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineType.NORMAL, LineType.START, LineType.END, LineType.ONLYONE})
    public  @interface LineType {
        int NORMAL = 0;
        int START = 1;
        int END = 2;
        int ONLYONE = 3;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LineStyle.NORMAL, LineStyle.DASHED})
    public @interface LineStyle {
        int NORMAL = 0;
        int DASHED = 1;
    }
}
