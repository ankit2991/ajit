package com.lockerroom.face.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lockerroom.face.sticker.StickerView;
import com.lockerroom.face.R;

import org.wysaid.view.ImageGLSurfaceView;

public class PhotoEditorView extends StickerView {

    private Bitmap currentBitmap;
    private BrushDrawingView mBrushDrawingView;

    public ImageGLSurfaceView mGLSurfaceView;
    private FilterImageView mImgSource;

    public PhotoEditorView(Context context) {
        super(context);
        init(null);
    }

    public PhotoEditorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet);
    }

    public PhotoEditorView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(attributeSet);
    }


    @SuppressLint({"Recycle", "ResourceType"})
    private void init(@Nullable AttributeSet attributeSet) {
        this.mImgSource = new FilterImageView(getContext());
        this.mImgSource.setId(1);
        this.mImgSource.setAdjustViewBounds(true);
        this.mImgSource.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.black));
        LayoutParams layoutParams = new LayoutParams(-1, -2);
        layoutParams.addRule(13, -1);
        this.mBrushDrawingView = new BrushDrawingView(getContext());
        this.mBrushDrawingView.setVisibility(View.GONE);
        this.mBrushDrawingView.setId(2);
        LayoutParams layoutParams2 = new LayoutParams(-1, -2);
        layoutParams2.addRule(13, -1);
        layoutParams2.addRule(6, 1);
        layoutParams2.addRule(8, 1);
        this.mGLSurfaceView = new ImageGLSurfaceView(getContext(), attributeSet);
        this.mGLSurfaceView.setId(3);
        this.mGLSurfaceView.setVisibility(View.VISIBLE);
        this.mGLSurfaceView.setAlpha(1.0f);
        this.mGLSurfaceView.setDisplayMode(ImageGLSurfaceView.DisplayMode.DISPLAY_ASPECT_FIT);
        LayoutParams layoutParams3 = new LayoutParams(-1, -2);
        layoutParams3.addRule(13, -1);
        layoutParams3.addRule(6, 1);
        layoutParams3.addRule(8, 1);
        addView(this.mImgSource, layoutParams);
        addView(this.mGLSurfaceView, layoutParams3);
        addView(this.mBrushDrawingView, layoutParams2);
    }


    public void setImageSource(final Bitmap bitmap) {
        this.mImgSource.setImageBitmap(bitmap);
        if (this.mGLSurfaceView.getImageHandler() != null) {
            this.mGLSurfaceView.setImageBitmap(bitmap);
        } else {
            this.mGLSurfaceView.setSurfaceCreatedCallback(new ImageGLSurfaceView.OnSurfaceCreatedCallback() {
                public void surfaceCreated() {
                    PhotoEditorView.this.mGLSurfaceView.setImageBitmap(bitmap);
                }
            });
        }
        this.currentBitmap = bitmap;
    }

    public void setImageSource(Bitmap bitmap, ImageGLSurfaceView.OnSurfaceCreatedCallback onSurfaceCreatedCallback) {
        this.mImgSource.setImageBitmap(bitmap);
        if (this.mGLSurfaceView.getImageHandler() != null) {
            this.mGLSurfaceView.setImageBitmap(bitmap);
        } else {
            this.mGLSurfaceView.setSurfaceCreatedCallback(onSurfaceCreatedCallback);
        }
        this.currentBitmap = bitmap;
    }

    public Bitmap getCurrentBitmap() {
        return this.currentBitmap;
    }


    public BrushDrawingView getBrushDrawingView() {
        return this.mBrushDrawingView;
    }

    public ImageGLSurfaceView getGLSurfaceView() {
        return this.mGLSurfaceView;
    }

    public void saveGLSurfaceViewAsBitmap(@NonNull final OnSaveBitmap onSaveBitmap) {
        if (this.mGLSurfaceView.getVisibility() == VISIBLE) {
            this.mGLSurfaceView.getResultBitmap(new ImageGLSurfaceView.QueryResultBitmapCallback() {
                public void get(Bitmap bitmap) {
                    onSaveBitmap.onBitmapReady(bitmap);
                }
            });
        }
    }

    public void setFilterEffect(String str) {
        this.mGLSurfaceView.setFilterWithConfig(str);
    }

    public void setFilterIntensity(float f) {
        this.mGLSurfaceView.setFilterIntensity(f);
    }
}
