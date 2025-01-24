package com.lockerroom.face.features.crop;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.isseiaoki.simplecropview.CropImageView;
import com.lockerroom.face.Constants;
import com.lockerroom.face.activities.EditImageActivity;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.features.crop.adapter.AspectRatioPreviewAdapter;
import com.lockerroom.face.R;
import com.lockerroom.face.features.picker.PhotoPicker;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.steelkiwi.cropiwa.AspectRatio;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class CropDialogFragment extends DialogFragment implements AspectRatioPreviewAdapter.OnNewSelectedListener {
    private static final String TAG = "CropDialogFragment";
    private Bitmap bitmap;
    FrameLayout app_ad;
    TextView tvLoading;
    RelativeLayout mainConaitner;
    private RelativeLayout loadingView;
    public CropImageView mCropView;
    public OnCropPhoto onCropPhoto;

    public interface OnCropPhoto {
        void finishCrop(Bitmap bitmap);
    }

    public void setBitmap(Bitmap bitmap2) {
        this.bitmap = bitmap2;
    }

    public static CropDialogFragment show(@NonNull AppCompatActivity appCompatActivity, OnCropPhoto onCropPhoto2, Bitmap bitmap2) {
        CropDialogFragment cropDialogFragment = new CropDialogFragment();
        cropDialogFragment.setBitmap(bitmap2);
        cropDialogFragment.setOnCropPhoto(onCropPhoto2);
        cropDialogFragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return cropDialogFragment;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    public void setOnCropPhoto(OnCropPhoto onCropPhoto2) {
        this.onCropPhoto = onCropPhoto2;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(-1, -1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(-16777216));
        }
        IronSourceAdsManager.INSTANCE.loadInter(requireActivity(), new IronSourceCallbacks() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {

            }
        });
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        getDialog().getWindow().requestFeature(1);
        getDialog().getWindow().setFlags(1024, 1024);
        View inflate = layoutInflater.inflate(R.layout.crop_layout, viewGroup, false);
        AspectRatioPreviewAdapter aspectRatioPreviewAdapter = new AspectRatioPreviewAdapter();
        aspectRatioPreviewAdapter.setListener(this);
        RecyclerView recyclerView = inflate.findViewById(R.id.fixed_ratio_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), 0, false));
        recyclerView.setAdapter(aspectRatioPreviewAdapter);
        this.mCropView = inflate.findViewById(R.id.crop_view);
        this.mCropView.setCropMode(CropImageView.CropMode.FREE);
        app_ad =  inflate.findViewById(R.id.bannerContainer);
        tvLoading = inflate.findViewById(R.id.bannerTvLoading);
        mainConaitner = inflate.findViewById(R.id.adsContainer);

        inflate.findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (requireActivity() != null) {
                    MaxAdManager.INSTANCE.checkTap(requireActivity(),()->{
                        CropDialogFragment.this.mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                        return null;
                    });
                }else{
                    CropDialogFragment.this.mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                }
            }
        });

        inflate.findViewById(R.id.imgSave).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (requireActivity() != null) {
                    MaxAdManager.INSTANCE.checkTap(requireActivity(),()->{
                        new OnSaveCrop().execute(new Void[0]);
                        return null;
                    });
                }else{
                    new OnSaveCrop().execute(new Void[0]);
                }
            }
        });
        this.loadingView = inflate.findViewById(R.id.loadingView);
        this.loadingView.setVisibility(View.GONE);
        inflate.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (requireActivity() != null) {
                    MaxAdManager.INSTANCE.checkTap(requireActivity(),()->{
                        CropDialogFragment.this.dismiss();
                        return null;
                    });
                }else{
                    CropDialogFragment.this.dismiss();
                }

            }
        });
        return inflate;
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mCropView = view.findViewById(R.id.crop_view);
        this.mCropView.setImageBitmap(this.bitmap);
    }

    public void onNewAspectRatioSelected(AspectRatio aspectRatio) {
        if (aspectRatio.getWidth() == 10 && aspectRatio.getHeight() == 10) {
            this.mCropView.setCropMode(CropImageView.CropMode.FREE);
        } else {
            this.mCropView.setCustomRatio(aspectRatio.getWidth(), aspectRatio.getHeight());
        }
    }

    class OnSaveCrop extends AsyncTask<Void, Bitmap, Bitmap> {
        OnSaveCrop() {
        }


        public void onPreExecute() {
            CropDialogFragment.this.showLoading(true);
        }


        public Bitmap doInBackground(Void... voidArr) {
            return CropDialogFragment.this.mCropView.getCroppedBitmap();
        }


        public void onPostExecute(Bitmap bitmap) {
            CropDialogFragment.this.showLoading(false);
            CropDialogFragment.this.onCropPhoto.finishCrop(bitmap);
            requireActivity();

//                            Intent intent = new Intent(requireActivity(), EditImageActivity.class);
//                            intent.putExtra(PhotoPicker.KEY_SELECTED_PHOTOS, BitMapToString(bitmap));
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    requireActivity().finish();
//                                }
//                            }, 700);
            CropDialogFragment.this.dismiss();

            //            } else {
//
//                CropDialogFragment.this.onCropPhoto.finishCrop(bitmap);
//                CropDialogFragment.this.dismiss();
//            }
        }
    }


    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public void showLoading(boolean z) {
        if (z) {
            try {
                requireActivity().getWindow().setFlags(16, 16);
                this.loadingView.setVisibility(View.VISIBLE);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            requireActivity().getWindow().clearFlags(16);
            this.loadingView.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        //MaxAdBanner>>done
        if (getActivity() != null && !SharePreferenceUtil.isPurchased(getActivity())) {
            mainConaitner.setVisibility(View.VISIBLE);
            MaxAdManager.INSTANCE.createBannerAd(getActivity(), mainConaitner,app_ad , tvLoading, false, new BannerAdListener() {
                @Override
                public void bannerAdLoaded(boolean isLoad) {
                }
            });
        }else{
            mainConaitner.setVisibility(View.GONE);
        }
    }
}
