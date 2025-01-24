//package com.rebel.photoeditor.ads;
//
//import android.app.Activity;
//import android.content.Context;
//import android.util.Log;
//import android.view.View;
//import android.widget.FrameLayout;
//import com.facebook.ads.AdError;
//import com.facebook.ads.AdListener;
//import com.facebook.ads.AdSize;
//import com.facebook.ads.AdView;
//import com.facebook.ads.Ad;
//import com.facebook.ads.InterstitialAd;
//import com.facebook.ads.InterstitialAdListener;
//import com.facebook.ads.NativeAd;
//import com.facebook.ads.NativeAdLayout;
//import com.facebook.ads.NativeAdListener;
//import com.facebook.ads.NativeAdView;
//
//import com.rebel.photoeditor.Constants;
//import com.rebel.photoeditor.R;
//import com.rebel.photoeditor.ads.AdmobAds;
//
//public class FacebookAds {
//
//    private static InterstitialAd interstitialAd;
//    private static InterstitialAd interstitialAdStart;
//    public static com.rebel.photoeditor.ads.AdmobAds.OnAdsCloseListener mOnAdsCloseListener;
//
//    public static void loadFullStartAd(Context context) {
//        if (interstitialAdStart == null) {
//            interstitialAdStart = new InterstitialAd(context, context.getString(R.string.facebook_inter_start_id));
//        }
//        if (context.getPackageName().equals(Constants.PKG_APP)) {
//            interstitialAdStart.loadAd();
//        }
//    }
//
//    public static boolean showFullStartAd() {
//        if (interstitialAdStart == null || !interstitialAdStart.isAdLoaded()) {
//            return false;
//        }
//        interstitialAdStart.show();
//        return true;
//    }
//
//    public static void initFullAds(final Context context) {
//        if (interstitialAd == null) {
//            interstitialAd = new InterstitialAd(context, context.getString(R.string.facebook_inter_id));
//            interstitialAd.setAdListener(new InterstitialAdListener() {
//                public void onAdClicked(Ad ad) {
//                }
//
//                public void onInterstitialDisplayed(Ad ad) {
//                }
//
//                public void onLoggingImpression(Ad ad) {
//                }
//
//                public void onAdLoaded(Ad ad) {
//                    Log.d("ADSSSSS", "onAdLoaded");
//                }
//
//                public void onInterstitialDismissed(Ad ad) {
//                    if (context.getPackageName().equals(Constants.PKG_APP)) {
//                        FacebookAds.loadFullAds();
//                    }
//                    if (FacebookAds.mOnAdsCloseListener != null) {
//                        FacebookAds.mOnAdsCloseListener.onAdsClose();
//                    }
//                }
//
//                public void onError(Ad ad, AdError adError) {
//                    Log.d("ADSSSSS", "Full Error " + adError.getErrorMessage());
//                }
//            });
//        }
//        if (context.getPackageName().equals(Constants.PKG_APP)) {
//            loadFullAds();
//        }
//    }
//
//    public static void destroyFullAds() {
//        if (interstitialAd != null) {
//            interstitialAd.destroy();
//        }
//        if (interstitialAdStart != null) {
//            interstitialAdStart.destroy();
//        }
//    }
//
//    public static void loadFullAds() {
//        if (interstitialAd != null) {
//            Log.d("ADSSSSS", "loadFullAds");
//            interstitialAd.loadAd();
//        }
//    }
//
//    public static boolean showFullAds(com.rebel.photoeditor.ads.AdmobAds.OnAdsCloseListener onAdsCloseListener) {
//        mOnAdsCloseListener = onAdsCloseListener;
//        if (interstitialAd == null || !interstitialAd.isAdLoaded()) {
//            return false;
//        }
//        interstitialAd.show();
//        return true;
//    }
//
//    public static void loadBanner(final Activity activity) {
//        AdView adView = new AdView(activity, activity.getString(R.string.facebook_banner_id), AdSize.BANNER_HEIGHT_50);
//        ((FrameLayout) activity.findViewById(R.id.fb_banner)).addView(adView);
//        adView.setAdListener(new AdListener() {
//            public void onAdClicked(Ad ad) {
//            }
//
//            public void onLoggingImpression(Ad ad) {
//            }
//
//            public void onError(Ad ad, AdError adError) {
//                Log.d("ADSSSSS", "Banner Error " + adError.getErrorMessage());
//            }
//
//            public void onAdLoaded(Ad ad) {
//                ((View) activity.findViewById(R.id.admob_banner).getParent()).setVisibility(View.GONE);
//            }
//        });
//        if (activity.getPackageName().equals(Constants.PKG_APP)) {
//            adView.loadAd();
//        }
//    }
//
//    public static void loadBanner(Activity activity, final View view) {
//        AdView adView = new AdView(activity, activity.getString(R.string.facebook_banner_id), AdSize.BANNER_HEIGHT_50);
//        ((FrameLayout) view.findViewById(R.id.fb_banner)).addView(adView);
//        adView.setAdListener(new AdListener() {
//            public void onAdClicked(Ad ad) {
//            }
//
//            public void onError(Ad ad, AdError adError) {
//            }
//
//            public void onLoggingImpression(Ad ad) {
//            }
//
//            public void onAdLoaded(Ad ad) {
//                ((View) view.findViewById(R.id.admob_banner).getParent()).setVisibility(View.GONE);
//            }
//        });
//        if (activity.getPackageName().equals(Constants.PKG_APP)) {
//            adView.loadAd();
//        }
//    }
//
//    public static void loadNativeAds(final Activity activity) {
//        final NativeAd nativeAd = new NativeAd(activity, activity.getString(R.string.facebook_native_id));
//        nativeAd.setAdListener(new NativeAdListener() {
//            public void onAdClicked(Ad ad) {
//            }
//
//            public void onLoggingImpression(Ad ad) {
//            }
//
//            public void onMediaDownloaded(Ad ad) {
//            }
//
//            public void onError(Ad ad, AdError adError) {
//                Log.d("ADSSSSS", "Native Error " + adError.getErrorMessage());
//            }
//
//            public void onAdLoaded(Ad ad) {
//                ((NativeAdLayout) activity.findViewById(R.id.fb_native_container)).addView(NativeAdView.render(activity, nativeAd));
//                AdmobAds.hideNativeAds(activity);
//            }
//        });
//        if (activity.getPackageName().equals(Constants.PKG_APP)) {
//            nativeAd.loadAd();
//        }
//    }
//}
