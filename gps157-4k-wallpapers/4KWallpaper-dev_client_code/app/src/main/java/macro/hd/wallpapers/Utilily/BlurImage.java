package macro.hd.wallpapers.Utilily;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import macro.hd.wallpapers.MyCustomView.GlideImageView;

import java.lang.ref.WeakReference;


public class BlurImage {

    private static  final float BITMAP_SCALE=0.3f;
    private static final float BLUR_RADIUS=7f;
    private Bitmap image;

    private Context context;
    private float intensity = 08f;
    private float MAX_RADIUS=25;

    private float MIN_RADIUS=0;
    private boolean async = false;

    BlurImage(Context context) {
        this.context = context;
    }

    /*
     *  This method is creating a blur bitmap , this method use renderscript which efficient
     *  and here we use scriptIntrinsicBlur for performing blurring
     * */
    private  Bitmap blur(){

        if (image == null) {
            return image;
        }

        Bitmap output= null;
        try {
            int width= Math.round(image.getWidth());
            int height = Math.round(image.getHeight());

            Bitmap input= Bitmap.createScaledBitmap(image,width,height,false);

            output = Bitmap.createBitmap(input);

            RenderScript rs = RenderScript.create(context);
            ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));


            Allocation inputallocation= Allocation.createFromBitmap(rs,input);
            Allocation outputallocation= Allocation.createFromBitmap(rs,output);
            intrinsicBlur.setRadius(intensity);
            intrinsicBlur.setInput(inputallocation);
            intrinsicBlur.forEach(outputallocation);

            outputallocation.copyTo(output);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }

    public void into(ImageView imageView) {

        if (async) {
            new AsyncBlurImage(imageView).execute();
        }
        else {
            try {
                Bitmap b=blur();
                if(b!=null)
                    imageView.setImageBitmap(b);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void into(GlideImageView imageView) {

        if (async) {
            new AsyncBlurImageGlide(imageView).execute();
        }
        else {
            try {
                Bitmap b=blur();
                if(b!=null)
                    imageView.setImageBitmap(b);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
    }

    @NonNull
    public static BlurImage with(Context context) {
        return new BlurImage(context);
    }

    /*
     * Here we get bitmap on which we apply the blur process
     * */

    public BlurImage load(Bitmap bitmap) {
        this.image = bitmap;
        return this;
    }

    public BlurImage load(int res) {
        image = BitmapFactory.decodeResource(context.getResources(), res);
        return this;
    }

    public BlurImage intensity(float intensity) {
        if (intensity<MAX_RADIUS && intensity > 0)
            this.intensity = intensity;
        else
            this.intensity = MAX_RADIUS;
        return this;
    }

    public BlurImage Async(boolean async) {
        this.async = async;
        return this;
    }


    /*
     * when developer make async true the this class will executed and it perform the blurring process in background
     * */

    private class AsyncBlurImageGlide extends AsyncTask<Void, Void, Bitmap> {
        private WeakReference<GlideImageView> weakReference;

        public AsyncBlurImageGlide(GlideImageView image) {
            this.weakReference = new WeakReference<GlideImageView>(image);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            return blur();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            GlideImageView imageView = weakReference.get();

            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

        }
    }

    /*
     * when developer make async true the this class will executed and it perform the blurring process in background
     * */

    private class AsyncBlurImage extends AsyncTask<Void, Void, Bitmap> {
        private WeakReference<ImageView> weakReference;

        public AsyncBlurImage(ImageView image) {
            this.weakReference = new WeakReference<ImageView>(image);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            return blur();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            ImageView imageView = weakReference.get();

            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

        }
    }
}