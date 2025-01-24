package macro.hd.wallpapers;

/**
 * Created by hungam on 17/7/19.
 */

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;

import macro.hd.wallpapers.Utilily.Logger;

public class VideoSurfaceHolder implements SurfaceHolder {

    private SurfaceHolder surfaceHolder;

    public VideoSurfaceHolder(SurfaceHolder holder) {
        surfaceHolder = holder;
    }

    @Override
    public void addCallback(Callback callback) {
        surfaceHolder.addCallback(callback);
    }

    @Override
    public Surface getSurface() {
        return surfaceHolder.getSurface();
    }

    @Override
    public Rect getSurfaceFrame() {
        return surfaceHolder.getSurfaceFrame();
    }

    @Override
    public boolean isCreating() {
        return surfaceHolder.isCreating();
    }

    @Override
    public Canvas lockCanvas() {
        return surfaceHolder.lockCanvas();
    }

    @Override
    public Canvas lockCanvas(Rect dirty) {
        return surfaceHolder.lockCanvas(dirty);
    }

    @Override
    public void removeCallback(Callback callback) {
        surfaceHolder.removeCallback(callback);
    }

    @Override
    public void setFixedSize(int width, int height) {
        Logger.e("setFixedSize","width:"+width+":"+"height:"+height);
        surfaceHolder.setFixedSize(width, height);
        surfaceHolder.setSizeFromLayout();
    }

    @Override
    public void setFormat(int format) {
        surfaceHolder.setFormat(format);
    }

    @Override
    public void setSizeFromLayout() {
        surfaceHolder.setSizeFromLayout();
    }

    @Override
    public void setType(int type) {
        surfaceHolder.setType(SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void setKeepScreenOn(boolean bool){
        //do nothing
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
        surfaceHolder.unlockCanvasAndPost(canvas);
    }
}