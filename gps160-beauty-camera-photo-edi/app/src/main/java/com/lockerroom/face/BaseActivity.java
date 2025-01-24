//package com.drawely.drawely.activities;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.cardview.widget.CardView;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.FileProvider;
//
//import com.android.billingclient.api.AcknowledgePurchaseParams;
//import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
//import com.android.billingclient.api.BillingClient;
//import com.android.billingclient.api.BillingClientStateListener;
//import com.android.billingclient.api.BillingFlowParams;
//import com.android.billingclient.api.BillingResult;
//import com.android.billingclient.api.ConsumeParams;
//import com.android.billingclient.api.ConsumeResponseListener;
//import com.android.billingclient.api.ProductDetails;
//import com.android.billingclient.api.ProductDetailsResponseListener;
//import com.android.billingclient.api.Purchase;
//import com.android.billingclient.api.PurchasesUpdatedListener;
//import com.android.billingclient.api.QueryProductDetailsParams;
//import com.android.billingclient.api.SkuDetails;
//import com.android.billingclient.api.SkuDetailsParams;
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.DataSource;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.load.engine.GlideException;
//import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
//import com.bumptech.glide.request.RequestListener;
//import com.bumptech.glide.request.target.Target;
//import com.drawely.drawely.R;
//import com.drawely.drawely.classes.Constant;
//import com.drawely.drawely.classes.Func;
//import com.drawely.drawely.fragement.Favourite;
//import com.drawely.drawely.interfaces.IronRewardedCallbacks;
//import com.drawely.drawely.model.Art;
//import com.drawely.drawely.sqlite.DbAdapter;
//import com.drawely.drawely.sqlite.DbContract.DrawelyEntry;
//import com.drawely.drawely.utils.AdManager;
//import com.drawely.drawely.utils.MyApplication;
//import com.drawely.drawely.utils.SharedPref;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.rewarded.RewardedAd;
//import com.google.common.collect.ImmutableList;
//import com.ironsource.mediationsdk.IronSource;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.OnClick;
//import io.reactivex.Observer;
//import io.reactivex.disposables.Disposable;
//
///*
// * NB all the activites extends from BaseActivity only to extend the RewardedAd implementation
// * no need to implement the RewardAd for each activity [RewardedAd coded once in this activity]
// *
// * */
//public class BaseActivity extends ConnectionListenerAds implements PurchasesUpdatedListener {
//
//
//    @BindView(R.id.dialog_draworcolor)
//    View dialog_draworcolor;
//
//    @BindView(R.id.dialog2_watch_ad)
//    View dialog_watch_ad;
//
//
//    @BindView(R.id.rl_dialog_drawing)
//    RelativeLayout rl_dialog_drawing;
//
//    @BindView(R.id.rl_dialog_coloring)
//    RelativeLayout rl_dialog_coloring;
//    @BindView(R.id.rl_dialog_cancel)
//    RelativeLayout rl_dialog_cancel;
//    @BindView(R.id.rl_dialog2_cancel)
//    RelativeLayout rl_dialog2_cancel;
//
//    //Billing
//    @BindView(R.id.dialog_free_trial)
//    View dialog_free_trial;
//    @BindView(R.id.rl_dialog_free_trial_buy)
//    RelativeLayout rl_dialog_free_trial_buy;
//    @BindView(R.id.dialog_free_trial_cancel)
//    ImageView dialog_free_trial_cancel;
//    @BindView(R.id.dialog_cv_trial)
//    CardView dialog_cv_trial;
//    @BindView(R.id.cv_dialog2_free_trial)
//    CardView cv_dialog2_free_trial;
//    @BindView(R.id.cl_bg_trial)
//    ConstraintLayout cl_bg_trial;
//    @BindView(R.id.txt_price)
//    TextView txt_price;
//
//    @BindView(R.id.rl_dialog_watch_ad)
//    RelativeLayout rl_dialog_watch_ad;
//    @BindView(R.id.dialog_favourite)
//    ImageView dialog_favourite;
//    @BindView(R.id.dialog2_favourite)
//    ImageView dialog2_favourite;
//
//
//    @BindView(R.id.dialog_progressbar)
//    ProgressBar dialog_progressbar;
//    @BindView(R.id.dialog2_progressbar)
//    ProgressBar dialog2_progressbar;
//    @BindView(R.id.dialog2_play_progressbar)
//    ProgressBar dialog2_play_progressbar;
//
//    @BindView(R.id.dialog_image_main)
//    ImageView dialog_image_main;
//    @BindView(R.id.img_play)
//    ImageView img_play;
//
//    @BindView(R.id.dialog2_image_main)
//    ImageView dialog2_image_main;
//
//    @BindView(R.id.cv_dialog_drawing)
//    CardView cv_dialog_drawing;
//    @BindView(R.id.cv_dialog_coloring)
//    CardView cv_dialog_coloring;
//
//    @BindView(R.id.cv_dialog_cancel)
//    CardView cv_dialog_cancel;
//    @BindView(R.id.cv_dialog_subcancel)
//    CardView cv_dialog_subcancel;
//    @BindView(R.id.cv_dialog2_cancel)
//    CardView cv_dialog2_cancel;
//    @BindView(R.id.cv_dialog2_subcancel)
//    CardView cv_dialog2_subcancel;
//
//    @BindView(R.id.cv_dialog2_watch_ad)
//    CardView cv_dialog2_watch_ad;
//    @BindView(R.id.dialog_cv)
//    CardView dialog_cv;
//
//    @BindView(R.id.cardView_draworcolor_favourite)
//    CardView cardView_draworcolor_favourite;
//    @BindView(R.id.cardView_watchad_favourite)
//    CardView cardView_watchad_favourite;
//    @BindView(R.id.dialog2_cv)
//    CardView dialog2_cv;
//    @BindView(R.id.cl_bg)
//    ConstraintLayout cl_bg;
//    @BindView(R.id.cl2_bg)
//    ConstraintLayout cl2_bg;
//    @BindView(R.id.txt_pen)
//    TextView txt_pen;
//    @BindView(R.id.txt_title)
//    TextView txt_title;
//    @BindView(R.id.txt2_title)
//    TextView txt2_title;
//
//    @BindView(R.id.coverdrawelyplus)
//    ImageView coverdrawelyplus;
//
//
//    Art currentArt;
//    int currentId = -1;
//    String lastphoto;
//    boolean isFromPlay;
//    boolean isForceOpen;
//    Context context;
//    Animation zoomoutcard;
//    Animation zoomincard;
//
//    //SQLITE
//    private DbAdapter myDbHelper;
//    Cursor cur = null;
//    SQLiteDatabase db;
//    String[] columns;
//    private boolean favouriteIsChecked = false;
//    Target<Drawable> target;
//    String price = "";
//
//
//    private static final String TAG = "adsvideo";
//
//
//    private static final float RADIUS = 2.f;
//    private boolean isRadiusedDrawOrColor = false;
//    private boolean isRadiusedWatchAd = false;
//
//    BillingClient billingClient;
//    ProductDetails skuDetailsDrawelyPlus;
//    boolean isBillingConnected = false;
//    static boolean premiumActivated = false;
//    static boolean deviceNotSupported = false;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        this.context = this;
//
//
//        // Show [Loading Ad.. button] first instead of [Watch Ad button] on Dialog
//        Constant.RWDSubject.subscribe(new Observer<String>() {
//            @Override
//            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
//            }
//
//            @Override
//            public void onNext(@io.reactivex.annotations.NonNull String s) {
//                LoadDialogWatchRewardAd();
//            }
//
//            @Override
//            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//            }
//        });
//
//        Constant.OPENADSubject.subscribe(new Observer<String>() {
//            @Override
//            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(@io.reactivex.annotations.NonNull String s) {
//                if (Func.isValidContextForGlide(context))
//                    if (MyApplication.getInstance().appOpenAdManager != null)
//                        MyApplication.getInstance().appOpenAdManager.loadAd(BaseActivity.this);
//            }
//
//            @Override
//            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
//        //LoadDialogWatchRewardAd();
//
//        new Handler().postDelayed(() -> {
//            if (!isFinishing() || !isDestroyed()) {
//                setupBilling();
//            }
//        }, 2000);
//
//
//    }
//
//    public void LoadDialogWatchRewardAd() {
//        if (dialog2_play_progressbar != null && img_play != null && txt_pen != null) {
//            dialog2_play_progressbar.setVisibility(View.VISIBLE);
//            img_play.setVisibility(View.INVISIBLE);
//            txt_pen.setText(context.getString(R.string.loading_ad));
//        }
//        setupRewardAds();
//    }
//
//    private RewardedAd mRewardedAd;
//    private boolean RewardedAdFailedToLoad = false;
//
//
//
//    public void setupRewardAds() {
//        Log.d(TAG, "setupRewardAds: ");
//        RewardedAdFailedToLoad = false;
//        AdManager.INSTANCE.loadRewardedVideo(new IronRewardedCallbacks() {
//            @Override
//            public void onLoaded(boolean b) {
//
//                if(b){
//                    Log.d(TAG, "Ad was loaded.");
//                    DialogWatchLoaded();
//                    RewardedAdFailedToLoad = false;
//                }else{
//                    // Handle the error.
//                    //  Log.d(TAG, loadAdError.getMessage());
//                    mRewardedAd = null;
//                    // Ad failed to load.
//                    // let user enter even if the reward failed
//                    DialogWatchLoaded();
//                    RewardedAdFailedToLoad = true;
//                }
//
//            }
//
//            @Override
//            public void onRewarded() {
//
//            }
//            @Override
//            public void onFailed() {
//
//            }
//            @Override
//            public void onCancelled() {
//
//            }
//        });
//
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//
////        RewardedAd.load(this, Constant.RWD,
////                adRequest, new RewardedAdLoadCallback() {
////                    @Override
////                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
////                        // Handle the error.
////                        Log.d(TAG, loadAdError.getMessage());
////                        mRewardedAd = null;
////                        // Ad failed to load.
////                        // let user enter even if the reward failed
////                        DialogWatchLoaded();
////                        RewardedAdFailedToLoad = true;
////                    }
////
////                    @Override
////                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
////                        mRewardedAd = rewardedAd;
////                        Log.d(TAG, "Ad was loaded.");
////                        DialogWatchLoaded();
////                        RewardedAdFailedToLoad = false;
////                    }
////                });
//    }
//
//    @OnClick(R.id.rl_dialog_watch_ad)
//    public void rl_dialog_watch_ad() {
//
//        if(AdManager.INSTANCE.getAvailableRewarded()){
//
//            Log.d(TAG, "Ad was loaded.");
//            DialogWatchLoaded();
//            RewardedAdFailedToLoad = false;
//
//            AdManager.INSTANCE.showRewardedAds(new IronRewardedCallbacks() {
//                @Override
//                public void onLoaded(boolean b) {
//
//                }
//                @Override
//                public void onRewarded() {
//                    // Handle the reward.
//                    Log.d(TAG, "The user earned the reward.");
//                    /*int rewardAmount = rewardItem.getAmount();
//                    String rewardType = rewardItem.getType();*/
//                    // hide the alert dialog
//                    rl_dialog2_cancel();
//                    // show the opened character
//                    ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//                }
//
//                @Override
//                public void onFailed() {
//                    // Called when ad fails to show.
//                    Log.d(TAG, "Ad failed to show.");
//                    // hide the alert dialog
////                    rl_dialog2_cancel();
//                    // show the opened character even the [Rewarded Ad Failed to show]
////                    ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//                    // load again after click to [Watch Ad] and [Ad is Loaded] and [Ad is failed to show] NOT after [failed loading only]
////                    LoadDialogWatchRewardAd();
//                }
//
//                @Override
//                public void onCancelled() {
//
//                }
//            });
//        }
//        else {
//            mRewardedAd = null;
//            // Ad failed to load.
//            // let user enter even if the reward failed
//            DialogWatchLoaded();
//            RewardedAdFailedToLoad = true;
//
//            /*
//             * load again after click to [Watch Ad] and [Ad is not Loaded] and [Ad is failed for loading]
//             * and only if [RewardedAdFailedToLoad set to true] with the inform of from the [onRewardedAdFailedToLoad callback]
//             * by default [RewardedAdFailedToLoad set to false] so will not Load RewardedAd only if [onRewardedAdFailedToLoad] called
//             * */
////            if (RewardedAdFailedToLoad) {
//            rl_dialog2_cancel();
//            ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//            // load RewardedAd Again
//            LoadDialogWatchRewardAd();
////            }
//        }
//
//
//        /* CLICK BUTTON REWARD
//        if (mRewardedAd != null) {
//            // Set RewardAd Callback for Showing
//            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    // Called when ad is shown.
//                    Log.d(TAG, "Ad was shown.");
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(AdError adError) {
//                    // Called when ad fails to show.
//                    Log.d(TAG, "Ad failed to show.");
//                    // hide the alert dialog
//                    rl_dialog2_cancel();
//                    // show the opened character even the [Rewarded Ad Failed to show]
//                    ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//                    // load again after click to [Watch Ad] and [Ad is Loaded] and [Ad is failed to show] NOT after [failed loading only]
//                    LoadDialogWatchRewardAd();
//
//                }
//
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    // Called when ad is dismissed.
//                    // Set the ad reference to null so you don't show the ad a second time.
//                    Log.d(TAG, "Ad was dismissed.");
//                    mRewardedAd = null;
//                    LoadDialogWatchRewardAd();
//                }
//            });
//
//            // Show Reward Ad
//            mRewardedAd.show(this, new OnUserEarnedRewardListener() {
//                @Override
//                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//                    // Handle the reward.
//                    Log.d(TAG, "The user earned the reward.");
//                    /*int rewardAmount = rewardItem.getAmount();
//                    String rewardType = rewardItem.getType();
//                    // hide the alert dialog
//                    rl_dialog2_cancel();
//                    // show the opened character
//                    ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//
//                }
//            });
//        }
//        else {
//            /*
//             * load again after click to [Watch Ad] and [Ad is not Loaded] and [Ad is failed for loading]
//             * and only if [RewardedAdFailedToLoad set to true] with the inform of from the [onRewardedAdFailedToLoad callback]
//             * by default [RewardedAdFailedToLoad set to false] so will not Load RewardedAd only if [onRewardedAdFailedToLoad] called
//             *
//            if (RewardedAdFailedToLoad) {
//                rl_dialog2_cancel();
//                ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//                // load RewardedAd Again
//                LoadDialogWatchRewardAd();
//            }
//        }
//*/
//
//        /*if (mRewardedAd != null) {
//            if (mRewardedAd.isLoaded()) {
//                Activity activityContext = this;
//                RewardedAdCallback adCallback = new RewardedAdCallback() {
//                    @Override
//                    public void onRewardedAdOpened() {
//                        // Ad opened.
//                    }
//
//                    @Override
//                    public void onRewardedAdClosed() {
//                        // Ad closed. Load RewardedAd again
//                        LoadDialogWatchRewardAd();
//                    }
//
//                    @Override
//                    public void onUserEarnedReward(@NonNull com.google.android.gms.ads.rewarded.RewardItem reward) {
//                        // User earned reward.
//
//                        // hide the alert dialog
//                        rl_dialog2_cancel();
//                        // show the opened character
//                        ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//                    }
//
//                    @Override
//                    public void onRewardedAdFailedToShow(AdError adError) {
//                        // Ad failed to display.
//
//                        // hide the alert dialog
//                        rl_dialog2_cancel();
//                        // show the opened character even the [Rewarded Ad Failed to show]
//                        ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//                        // load again after click to [Watch Ad] and [Ad is Loaded] and [Ad is failed to show] NOT after [failed loading only]
//                        LoadDialogWatchRewardAd();
//
//                    }
//                };
//                //show RewardedAd
//                mRewardedAd.show(activityContext, adCallback);
//            } else {
//                *//*
//         * load again after click to [Watch Ad] and [Ad is not Loaded] and [Ad is failed for loading]
//         * and only if [RewardedAdFailedToLoad set to true] with the inform of from the [onRewardedAdFailedToLoad callback]
//         * by default [RewardedAdFailedToLoad set to false] so will not Load RewardedAd only if [onRewardedAdFailedToLoad] called
//         * *//*
//                if (RewardedAdFailedToLoad) {
//                    rl_dialog2_cancel();
//                    ShowDialogDrawingOrColoring(currentArt, lastphoto, isFromPlay, isForceOpen);
//                    // load RewardedAd Again
//                    LoadDialogWatchRewardAd();
//                }
//            }
//        }else {
//            Log.d("TAG", "The rewarded ad wasn't ready yet.");
//        }*/
//
//    }
//
//    public void DialogWatchLoaded() {
//        if (dialog2_play_progressbar != null && img_play != null && txt_pen != null) {
//            dialog2_play_progressbar.setVisibility(View.GONE);
//            img_play.setVisibility(View.VISIBLE);
//            txt_pen.setText(context.getString(R.string.watch_ad));
//        }
//    }
//
//
//    private void checkFreeTrialVisibility() {
//        if (Constant.ENABLE_DRAWELY_PLUS_IAP) {
//            cv_dialog2_free_trial.setVisibility(View.VISIBLE);
//        } else {
//            cv_dialog2_free_trial.setVisibility(View.GONE);
//        }
//    }
//
//    public void setupRadiusCardviewDraworColor() {
//        Log.d("tt", "setupRadiusCardviewDraworColor: " + isRadiusedDrawOrColor);
//
//        if (!isRadiusedDrawOrColor && cv_dialog_cancel.getHeight() != 0) {
//            float radius = 0;
//            float subRadius = 0;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                radius = (float) cv_dialog_drawing.getHeight() / RADIUS;
//                subRadius = (float) cv_dialog_subcancel.getHeight() / RADIUS;
//            }
//            Log.d("tt", "setupRadiusCardviewDraworColor: " + radius + "  " + cv_dialog_drawing.getHeight());
//
//            cv_dialog_drawing.setRadius(radius);
//            cv_dialog_coloring.setRadius(radius);
//            cv_dialog_cancel.setRadius(radius);
//            cv_dialog_subcancel.setRadius(subRadius);
//            isRadiusedDrawOrColor = true;
//        }
//    }
//
//
//    private void setupRadiusCardviewWatchAd() {
//        if (!isRadiusedWatchAd && cv_dialog2_cancel.getHeight() != 0) {
//            float radius = 0;
//            float subRadius = 0;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                radius = (float) cv_dialog2_watch_ad.getHeight() / RADIUS;
//                subRadius = (float) cv_dialog2_subcancel.getHeight() / RADIUS;
//
//            }
//            cv_dialog2_watch_ad.setRadius(radius);
//            cv_dialog2_cancel.setRadius(radius);
//            cv_dialog2_subcancel.setRadius(subRadius);
//            isRadiusedWatchAd = true;
//        }
//    }
//
//    public void ShowDialogDrawingOrColoring(Art currentArt, String lastphoto, boolean isFromPlay, boolean isForceOpen) {
//        this.currentArt = currentArt;
//        this.currentId = currentArt.getId();
//        this.lastphoto = lastphoto;
//        this.isFromPlay = isFromPlay;
//        this.isForceOpen = isForceOpen;
//
//        if (isFromPlay) {
//            cardView_draworcolor_favourite.setVisibility(View.INVISIBLE);
//        } else {
//            cardView_draworcolor_favourite.setVisibility(View.VISIBLE);
//
//        }
//        //for Search Fragment Hide Keyboard
//        /*Search myFragment = (Search) getSupportFragmentManager().findFragmentByTag("SEARCH");
//        if (myFragment != null && myFragment.isVisible()) {
//            // add your code here
//            Log.d(TAG, ": ");
//            hideKeyboard((Activity) context);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (dialog_draworcolor != null) {
//                        setupAnimationDraworColor(lastphoto);
//                    }
//                }
//            }, 500);
//
//
//        } else if (dialog_draworcolor != null) {
//            hideKeyboard((Activity) context);
//            setupAnimationDraworColor(lastphoto);
//        }*/
//        if (dialog_draworcolor != null) {
//            hideKeyboard((Activity) context);
//            setupAnimationDraworColor(lastphoto);
//        }
//
//    }
//
//    public void ShowDialogWatchAd(Art currentArt, String lastphoto, boolean isFromPlay, boolean isForceOpen) {
//        this.currentArt = currentArt;
//        this.currentId = currentArt.getId();
//        this.lastphoto = lastphoto;
//        this.isFromPlay = isFromPlay;
//        this.isForceOpen = isForceOpen;
//
//        if (isFromPlay) {
//            cardView_watchad_favourite.setVisibility(View.INVISIBLE);
//        } else {
//            cardView_watchad_favourite.setVisibility(View.VISIBLE);
//        }
//
//        if (dialog_watch_ad != null) {
//            hideKeyboard((Activity) context);
//            setupAnimationWatchAd(lastphoto);
//        }
//        //for Search Fragment Hide Keyboard
//        /*Search myFragment = (Search) getSupportFragmentManager().findFragmentByTag("SEARCH");
//        if (myFragment != null && myFragment.isVisible()) {
//            // add your code here
//            hideKeyboard((Activity) context);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (dialog_watch_ad != null) {
//                        setupAnimationWatchAd(lastphoto);
//                    }
//                }
//            }, 500);
//
//
//        } else if (dialog_watch_ad != null) {
//            hideKeyboard((Activity) context);
//            setupAnimationWatchAd(lastphoto);
//
//        }*/
//
//
//    }
//
//
//    public static void hideKeyboard(Activity activity) {
//        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        //Find the currently focused view, so we can grab the correct window token from it.
//        View view = activity.getCurrentFocus();
//        //If no view currently has focus, create a new one, just so we can grab a window token from it
//        if (view == null) {
//            view = new View(activity);
//        }
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//    }
//
//    private void setupAnimationDraworColor(String lastphoto) {
//        zoomoutcard = AnimationUtils.loadAnimation(context, R.anim.zoomoutcard);
//        zoomoutcard.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                setupRadiusCardviewDraworColor();
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                if (lastphoto != null && Func.isValidContextForGlide(context)) {
//
//                    Glide.with(dialog_image_main).clear(target);
//                    target = Glide.with(dialog_image_main)
//                            .load(lastphoto)
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    dialog_image_main.setVisibility(View.VISIBLE);
//                                    dialog_progressbar.setVisibility(View.GONE);
//                                    //dialog_image_main.setImageDrawable(resource);
//                                    return false;
//                                }
//                            })
//                            .into(dialog_image_main);
//
//                    //setupRank(currentId);
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        dialog_draworcolor.setVisibility(View.VISIBLE);
//        if (currentArt != null) {
//            txt_title.setText(currentArt.getTitle());
//        }
//        favouriteExecute(dialog_favourite);
//        dialog_cv.clearAnimation();
//        dialog_cv.startAnimation(zoomoutcard);
//
//
//    }
//
//
//    private void setupAnimationWatchAd(String lastphoto) {
//
//        checkFreeTrialVisibility();
//
//        zoomoutcard = AnimationUtils.loadAnimation(context, R.anim.zoomoutcard);
//        zoomoutcard.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                setupRadiusCardviewWatchAd();
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                if (lastphoto != null && Func.isValidContextForGlide(context)) {
//                    Glide.with(dialog2_image_main).clear(target);
//                    target = Glide.with(dialog2_image_main)
//                            .load(lastphoto)
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    dialog2_image_main.setVisibility(View.VISIBLE);
//                                    dialog2_progressbar.setVisibility(View.GONE);
//                                    //dialog_image_main.setImageDrawable(resource);
//                                    return false;
//                                }
//                            })
//                            .into(dialog2_image_main);
//                    //setupRank(currentId);
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        dialog_watch_ad.setVisibility(View.VISIBLE);
//        if (currentArt != null) {
//            txt2_title.setText(currentArt.getTitle());
//        }
//        favouriteExecute(dialog2_favourite);
//        dialog2_cv.clearAnimation();
//        dialog2_cv.startAnimation(zoomoutcard);
//
//
//    }
//
//
//    private void setupCoverDrawelyPlusDialog() {
//        if (Constant.coverDrawelyPlus != null && !Constant.coverDrawelyPlus.equals("")) {
//            if (Func.isValidContextForGlide(context))
//                Glide.with(context)
//                        .load(Constant.coverDrawelyPlus)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .transition(DrawableTransitionOptions.withCrossFade(1000))
//                        .into(coverdrawelyplus);
//        }
//    }
//
//    public void setupAnimationFreeTrial() {
//        setupCoverDrawelyPlusDialog();
//
//        zoomoutcard = AnimationUtils.loadAnimation(context, R.anim.zoomoutcard);
//
//
//        dialog_free_trial.setVisibility(View.VISIBLE);
//
//
//        dialog_cv_trial.clearAnimation();
//        dialog_cv_trial.startAnimation(zoomoutcard);
//
//
//    }
//
//    @OnClick(R.id.rl_dialog_drawing)
//    public void rl_dialog_drawing() {
//        Log.d(TAG, "rl_dialog_drawing: " + currentId);
//        if (currentId != -1) {
//            setupAnimationCancelDrawOrColor(PAINT);
//        }
//
//    }
//
//    @OnClick(R.id.rl_dialog_coloring)
//    public void rl_dialog_coloring() {
//        if (currentId != -1) {
//            setupAnimationCancelDrawOrColor(COLOR);
//        }
//
//    }
//
//    final int ONLYCANCEL = 0;
//    final int PAINT = 1;
//    final int COLOR = 2;
//
//    @OnClick(R.id.rl_dialog_cancel)
//    public void rl_dialog_cancel() {
//
//        setupAnimationCancelDrawOrColor(ONLYCANCEL);
//
//
//    }
//
//
//    private void setupAnimationCancelDrawOrColor(int cancelState) {
//
//        rl_dialog_drawing.setEnabled(false);
//        rl_dialog_coloring.setEnabled(false);
//        rl_dialog_cancel.setEnabled(false);
//
//        zoomincard = AnimationUtils.loadAnimation(context, R.anim.design_bottom_sheet_slide_out);
//        //zoomincard = AnimationUtils.loadAnimation(context, R.anim.zoomincard);
//        zoomincard.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                dialog_draworcolor.setVisibility(View.GONE);
//                dialog_image_main.setVisibility(View.GONE);
//                dialog_progressbar.setVisibility(View.VISIBLE);
//                cl_bg.setVisibility(View.VISIBLE);
//                rl_dialog_drawing.setEnabled(true);
//                rl_dialog_coloring.setEnabled(true);
//                rl_dialog_cancel.setEnabled(true);
//                dialog_favourite.setImageResource(R.drawable.star);
//                if (cancelState != ONLYCANCEL) {
//                    switch (cancelState) {
//                        case PAINT:
//                            Func.getInstance().goToPaint(context, currentId, true, currentArt, isFromPlay, isForceOpen);
//                            break;
//                        case COLOR:
//                            Func.getInstance().goToColor(context, currentId, true, currentArt, isFromPlay, isForceOpen);
//                            break;
//                    }
//
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        dialog_cv.clearAnimation();
//        dialog_cv.startAnimation(zoomincard);
//        cl_bg.setVisibility(View.GONE);
//
//    }
//
//
//    private void setupAnimationCancelWatchAd() {
//        zoomincard = AnimationUtils.loadAnimation(context, R.anim.zoomincard);
//        zoomincard.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                dialog_watch_ad.setVisibility(View.GONE);
//                dialog2_image_main.setVisibility(View.GONE);
//                dialog2_progressbar.setVisibility(View.VISIBLE);
//                /*img_play.setVisibility(View.INVISIBLE);
//                txt_pen.setText("Loading Ad");
//                dialog2_play_progressbar.setVisibility(View.VISIBLE);*/
//                cl2_bg.setVisibility(View.VISIBLE);
//                rl_dialog_watch_ad.setEnabled(true);
//                rl_dialog2_cancel.setEnabled(true);
//                dialog2_favourite.setImageResource(R.drawable.star);
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        dialog2_cv.clearAnimation();
//        dialog2_cv.startAnimation(zoomincard);
//        cl2_bg.setVisibility(View.GONE);
//
//
//    }
//
//    private void setupAnimationCancelFreeTrial() {
//        if (dialog_free_trial.getVisibility() == View.VISIBLE) {
//            zoomincard = AnimationUtils.loadAnimation(context, R.anim.design_bottom_sheet_slide_out);
//            //zoomincard = AnimationUtils.loadAnimation(context, R.anim.zoomincard);
//            zoomincard.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    dialog_free_trial.setVisibility(View.GONE);
//                    cl_bg_trial.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
//
//            dialog_cv_trial.clearAnimation();
//            dialog_cv_trial.startAnimation(zoomincard);
//            cl_bg_trial.setVisibility(View.GONE);
//        }
//
//
//    }
//
//
//    @OnClick(R.id.rl_dialog2_cancel)
//    public void rl_dialog2_cancel() {
//        rl_dialog_watch_ad.setEnabled(false);
//        rl_dialog2_cancel.setEnabled(false);
//        setupAnimationCancelWatchAd();
//
//    }
//
//    //    Billing Buttons
//    @OnClick(R.id.rl_dialog2_free_trial)
//    public void rl_dialog2_free_trial() {
//        setupAnimationFreeTrial();
//        //setupAnimationCancelWatchAd();
//    }
//
//    @OnClick(R.id.dialog_free_trial_cancel)
//    public void dialog_free_trial_cancel() {
//
//        setupAnimationCancelFreeTrial();
//    }
//
//
//    @OnClick(R.id.rl_dialog_free_trial_buy)
//    public void rl_dialog_free_trial_buy() {
//        //setup Billing Pay
//        Log.d(TAG, "rl_dialog_free_trial_buy: 0");
//
//        if (skuDetailsDrawelyPlus != null && isBillingConnected) {
//
//            ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
//                    ImmutableList.of(
//                            BillingFlowParams.ProductDetailsParams.newBuilder()
//                                    .setProductDetails(skuDetailsDrawelyPlus)
//                                    //.setOfferToken(offerToken)
//                                    .build()
//                    );
//
//            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
//                    .setProductDetailsParamsList(productDetailsParamsList)
//                    .build();
//
//// Launch the billing flow
//            BillingResult billingResult = billingClient.launchBillingFlow(this, billingFlowParams);
//        }
//
//
//    }
//
//    private void setupBilling() {
//        if (context != null) {
//            if (!SharedPref.getInstance(context).isDeviceSupported() && Constant.DEVICE_NOT_SUPPORTED_FEAUTURE) {
//                return;
//            }
//            billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build();
//            billingClient.startConnection(new BillingClientStateListener() {
//                @Override
//                public void onBillingSetupFinished(BillingResult billingResult) {
//                    Log.d(TAG, "queryBillingParams: " + billingResult.getResponseCode());
//                    //Log.d(TAG, "isBillingSupportedNOW: " + billingClient.isFeatureSupported(BillingClient.FeatureType.IN_APP_ITEMS_ON_VR).getResponseCode());
//
//                    //connection is on
//                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                        // The BillingClient is ready. You can query purchases here.
//                        billingClient.queryPurchasesAsync(BillingClient.ProductType.INAPP, (billingResult1, purchases) -> {
//                            if (!purchases.isEmpty()) {
//                                verifyPurchase(purchases);
//                            } else {
//                                if (Constant.isPremiumMember) desactivatePremium();
//                                queryBillingParams();
//                            }
//                        });
//                        //List<Purchase> purchases = purchasesResult.getPurchasesList();
//
//                        // The BillingClient is ready. You can query purchases here.
//                        Toast.makeText(context, "Successfuly connected to the Billing Client", Toast.LENGTH_SHORT).show();
//                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//                        Log.d(TAG, "queryBillingParams: ITEM_ALREADY_OWNED");
//                        queryBillingParams();
//                        Toast.makeText(context, "Successfuly connected to the Billing Client owned", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        Toast.makeText(context, "Failed to connect the billing client", Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, "checkSupportBilling: Failed to connect");
//                        checkSupportBilling();
//                    }
//                }
//
//                @Override
//                public void onBillingServiceDisconnected() {
//                    Log.d(TAG, "checkSupportBilling DISNCON: " + billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS).getResponseCode());
//                    isBillingConnected = false;
//                    //Toast.makeText(context, "Disconnected from the client", Toast.LENGTH_SHORT).show();
//                    numberQuery++;
//                }
//            });
//        }
//    }
//
//    private boolean checkSupportBilling() {
//        if (!SharedPref.getInstance(context).isDeviceSupported() && Constant.DEVICE_NOT_SUPPORTED_FEAUTURE) {
//            return false;
//        }
//        int response = billingClient.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS).getResponseCode();
//        Log.d(TAG, "checkSupportBilling: " + response + " DEVICE_NOT_SUPPORTED_FEAUTURE" + Constant.DEVICE_NOT_SUPPORTED_FEAUTURE);
//
//        if ((response == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED || response == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) && Constant.DEVICE_NOT_SUPPORTED_FEAUTURE) {
//            //Toast.makeText(context, getResources().getString(R.string.not_supported), Toast.LENGTH_SHORT).show();
//            //txt_price.setText(getResources().getString(R.string.not_supported) + "");
//            deviceNotSupported();
//            return false;
//        } else if (response == BillingClient.BillingResponseCode.OK) {
//            return true;
//        }
//        return true;
//    }
//
//    private void queryBillingParams() {
//
//        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(Constant.ID_SKU_IAP)
//                .setProductType(BillingClient.ProductType.INAPP)
//                .build());
//        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
//                .setProductList(productList)
//                .build();
//        billingClient.queryProductDetailsAsync(
//                params,
//                new ProductDetailsResponseListener() {
//                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
//                        // Process the result
//
//                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
//                            for (ProductDetails productDetails : productDetailsList) {
//                                //String sku = skuDetails.getSku();
//                                skuDetailsDrawelyPlus = productDetails;
//                                isBillingConnected = true;
//                                price = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
//                             //   Log.e("price",productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
//                             //   Log.e("detail","product"+productDetails);
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        txt_price.setText(getResources().getString(R.string.buy_now_for) + " " + price);
//
//                                    }
//                                });
//
//                            }
//                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED && skuDetailsDrawelyPlus != null) {
//
//                            activatePremium();
//                            Log.d(TAG, "queryBillingParams: ITEM_ALREADY_OWNED");
//                            if (context instanceof MainActivity) {
//                                ((MainActivity) context).home();
//                            }
//                        }
//                    }
//                }
//        );
//        //        List<String> skuList = new ArrayList<>();
//        //Log.d(TAG, "isBillingSupported: " + billingClient.isFeatureSupported(BillingClient.FeatureType.IN_APP_ITEMS_ON_VR).getResponseCode());
////        Log.d(TAG, "queryBillingParams: ");
////        if (checkSupportBilling()) {
////            Log.d(TAG, "queryBillingParams: 2");
////            skuList.add(Constant.ID_SKU_IAP);
////            QueryProductDetailsParams.Builder params = QueryProductDetailsParams.newBuilder();
////            params.setProductList(skuList).setType(BillingClient.ProductType.INAPP);
//
//    /*billingClient.queryPurchaseHistoryAsync(SkuType.INAPP, new PurchaseHistoryResponseListener() {
//            @Override
//            public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> list) {
//                if (list != null) {
//                    verifyPurchaseHistory(list);
//                }
//            }
//        });*/
//
////            billingClient.queryProductDetailsAsync(params.build(),
////                    (billingResult, skuDetailsList) -> {
////                        Log.d(TAG, "queryBillingParams: querySkuDetailsAsync" + billingResult.getResponseCode());
////                        // Process the result.
////                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
////                            for (SkuDetails skuDetails : skuDetailsList) {
////                                //String sku = skuDetails.getSku();
////                                skuDetailsDrawelyPlus = skuDetails;
////                                isBillingConnected = true;
////                                price = skuDetails.getPrice();
////                                runOnUiThread(new Runnable() {
////                                    @Override
////                                    public void run() {
////                                        txt_price.setText(getResources().getString(R.string.buy_now_for) + " " + price);
////
////                                    }
////                                });
////
////                            }
////                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED && skuDetailsList != null) {
////
////                            activatePremium();
////                            Log.d(TAG, "queryBillingParams: ITEM_ALREADY_OWNED");
////                            if (context instanceof MainActivity) {
////                                ((MainActivity) context).home();
////                            }
////                        }
////                    });
//        }
//
//
//
//    final int TIME_OUT_QUERY = 5;
//    int numberQuery = 0;
//
//
//    @OnClick(R.id.dialog2_favourite)
//    public void dialog2_favourite() {
//        int i = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (i != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//        } else {
//            if (favouriteIsChecked == false) {
//
//                myDbHelper = new DbAdapter(context);
//                db = myDbHelper.getWritableDatabase();
//                ContentValues values = new ContentValues();
//
//
//                values.put(DrawelyEntry.ID, currentArt.getId());
//                values.put(DrawelyEntry.TITLE, currentArt.getTitle());
//                if (currentArt.isLock() && !Constant.isPremiumMember) {
//                    values.put(DrawelyEntry.LOCK, DrawelyEntry.LOCKED);
//                }
//                values.put(DrawelyEntry.MAIN_IMAGE_URL, lastphoto);
//                values.put(DrawelyEntry.PTS, currentArt.getPts());
//
//
//                long insertedRow = db.insert(DrawelyEntry.TABLE_NAME, null, values);
//                Log.d("TAGSQLITE", "ll_favourite: insertedRow=" + insertedRow);
//
//                db.close();
//                myDbHelper.close();
//
//                dialog2_favourite.setImageResource(R.drawable.favstar);
//                favouriteIsChecked = true;
//
//            } else {
//
//                myDbHelper = new DbAdapter(context);
//
//                db = myDbHelper.getWritableDatabase();
//                String whereClause = DrawelyEntry.ID + "=?";
//                String[] whereArgs = {currentArt.getId() + ""};
//
//                int deleteRow = db.delete(DrawelyEntry.TABLE_NAME,
//                        whereClause,
//                        whereArgs);
//
//
//                db.close();
//                myDbHelper.close();
//                dialog2_favourite.setImageResource(R.drawable.star);
//                favouriteIsChecked = false;
//            }
//
//            //for Favourite Fragment
//            Favourite myFragment = (Favourite) getSupportFragmentManager().findFragmentByTag("FAVOURITE");
//            if (myFragment != null && myFragment.isVisible()) {
//                // add your code here
//                myFragment.favRefrech();
//            }
//
//        }
//    }
//
//    @OnClick(R.id.dialog_favourite)
//    public void dialog_favourite() {
//        int i = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (i != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//        } else {
//            if (favouriteIsChecked == false) {
//
//                myDbHelper = new DbAdapter(context);
//                db = myDbHelper.getWritableDatabase();
//                ContentValues values = new ContentValues();
//
//
//                values.put(DrawelyEntry.ID, currentArt.getId());
//                values.put(DrawelyEntry.TITLE, currentArt.getTitle());
//                if (currentArt.isLock()) {
//                    values.put(DrawelyEntry.LOCK, DrawelyEntry.LOCKED);
//                }
//                values.put(DrawelyEntry.MAIN_IMAGE_URL, lastphoto);
//                values.put(DrawelyEntry.PTS, currentArt.getPts());
//
//
//                long insertedRow = db.insert(DrawelyEntry.TABLE_NAME, null, values);
//                Log.d("TAGSQLITE", "ll_favourite: insertedRow=" + insertedRow);
//
//                db.close();
//                myDbHelper.close();
//
//                dialog_favourite.setImageResource(R.drawable.favstar);
//                favouriteIsChecked = true;
//
//            } else {
//
//                myDbHelper = new DbAdapter(context);
//
//                db = myDbHelper.getWritableDatabase();
//                String whereClause = DrawelyEntry.ID + "=?";
//                String[] whereArgs = {currentArt.getId() + ""};
//
//                int deleteRow = db.delete(DrawelyEntry.TABLE_NAME,
//                        whereClause,
//                        whereArgs);
//
//
//                db.close();
//                myDbHelper.close();
//                dialog_favourite.setImageResource(R.drawable.star);
//                favouriteIsChecked = false;
//            }
//
//            //for Favourite Fragment
//            Favourite myFragment = (Favourite) getSupportFragmentManager().findFragmentByTag("FAVOURITE");
//            if (myFragment != null && myFragment.isVisible()) {
//                // add your code here
//                myFragment.favRefrech();
//            }
//
//        }
//    }
//
//    public void favouriteExecute(ImageView dialog_fav) {
//        myDbHelper = new DbAdapter(context);
//        int i = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        if (i != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
//
//        } else {
//            db = myDbHelper.getReadableDatabase();
//
//            try {
//                String selection = DrawelyEntry.ID + "=?";
//                String[] selectionArgs = {currentArt.getId() + ""};
//                cur = db.query(DrawelyEntry.TABLE_NAME,
//                        columns,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        null
//                );
//                if (cur.getCount() != 0) {
//                    if (cur.moveToFirst()) {
//                        favouriteIsChecked = true;
//                        dialog_fav.setImageResource(R.drawable.favstar);
//                    }
//
//                } else {
//                    dialog_fav.setImageResource(R.drawable.star);
//                    favouriteIsChecked = false;
//                }
//
//                cur.close();
//                db.close();
//                myDbHelper.close();
//            } catch (
//                    Exception e) {
//                // TODO: handle exception
//            }
//        }
//
//    }
//
//    @OnClick(R.id.dialog_share)
//    public void share() {
//        //img_draw.performDoubleTap();
//        shareImage();
//    }
//
//    private void shareImage() {
//        try {
//
//            dialog_image_main.setDrawingCacheEnabled(true);
//
//            dialog_image_main.setDrawingCacheBackgroundColor(Color.WHITE);
//            if (dialog_image_main.getDrawingCache().isRecycled()) {
//                return;
//            }
//            Bitmap map = Bitmap.createBitmap(dialog_image_main.getDrawingCache());
//
//            //this one
//            dialog_image_main.setDrawingCacheEnabled(false);
//
//            File cachePath = new File(this.getCacheDir(), "images");
//            cachePath.mkdirs(); // don't forget to make the directory
//            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
//            map.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            stream.close();
//
//            Uri contentUri = FileProvider.getUriForFile(this, getResources().getString(R.string.authority), new File(cachePath + "/image.png"));
//
//            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//            sharingIntent.setType("image/jpeg");
//            sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
//            String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
//            if (currentArt != null) {
//                sharingIntent.putExtra(Intent.EXTRA_TEXT, "It's " + currentArt.getTitle() + " ,You too TRY THIS APP NOW !! " + url);
//            } else {
//                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Hey You too TRY THIS APP NOW !! " + url);
//            }
//            startActivity(Intent.createChooser(sharingIntent, "Share image using"));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        IronSource.onResume(this);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        IronSource.onPause(this);
//    }
//    @Override
//    public void onDestroy() {
//
//        super.onDestroy();
//    }
//
//
//    @Override
//    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
//        Log.d(TAG, "onPurchasesUpdated: " + billingClient.isReady());
//        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
//                && purchases != null) {
//            Log.d(TAG, "getDebugMessage:OK " + billingResult.getDebugMessage());
//            activatePremium();
//            handlePurchase(purchases);
//        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//            Log.d(TAG, "getDebugMessage:CANCEL " + billingResult.getDebugMessage());
//            // Handle an error caused by a user cancelling the purchase flow.
//        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//            Log.d(TAG, "getDebugMessage:ITEM_ALREADY_OWNED " + billingResult.getDebugMessage());
//            activatePremium();
//            handlePurchase(purchases);
//
//            // Handle an error caused by a user cancelling the purchase flow.
//        } else {
//            Log.d(TAG, "getDebugMessage:ERROR " + billingResult.getDebugMessage());
//            // Handle any other error codes.
//        }
//    }
//
//
//    private void handlePurchase(List<Purchase> purchases) {
//        if (purchases != null && purchases.size() > 0) {
//
//            for (Purchase purchase : purchases) {
//                handleAcknowledgePurchase(purchase);
//               /* switch (purchase.getSku()) {
//                    case ID_SKU_IAP:
//                        SharedPref.getInstance(context).setTokenBill(purchase.getPurchaseToken());
//                        Log.d(TAG, "handleAcknowledgePurchase: before=== " + purchase.isAcknowledged());
//                        handleAcknowledgePurchase(purchase);
//                        break;
//                    default:
//                        break;
//                }*/
//            }
//        }
//
//    }
//
//    private void verifyPurchase(@NonNull List<Purchase> purchases) {
//        for (Purchase purchase : purchases) {
//            //handleConsumePurchase(purchase);
//            Log.d(TAG, "verifyPurchase State getPurchaseState" + purchase.getPurchaseState());
//
//            // Handle Refund
//            int purchaseState = purchase.getPurchaseState();
//            if (purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
//                desactivatePremium();
//            } else {
//                handleAcknowledgePurchase(purchase);
//                activatePremium();
//            }
//            // because we have only one SKU
//            break;
//                    /*switch (purchase.getSku()) {
//                        case ID_SKU_IAP:
//                            Log.d(TAG, "verifyPurchase: " + purchase.getPurchaseToken());
//                            SharedPref.getInstance(context).setTokenBill(purchase.getPurchaseToken());
//                            activatePremium();
//                            break;
//                        default:
//                            break;
//                    }*/
//        }
//
//    }
//
//    private void handleConsumePurchase(Purchase purchase) {
//// Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.
//        // Verify the purchase.
//        // Ensure entitlement was not already granted for this purchaseToken.
//        // Grant entitlement to the user.
//
//        ConsumeParams consumeParams =
//                ConsumeParams.newBuilder()
//                        .setPurchaseToken(purchase.getPurchaseToken())
//                        .build();
//
//        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
//            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                // Handle the success of the consume operation.
//            }
//        };
//
//        billingClient.consumeAsync(consumeParams, listener);
//    }
//
//
//    private void desactivatePremium() {
//        Log.d(TAG, "verifyPurchase: DESACTIVE");
//
//        Log.d(TAG, "desactivatePremium:");
//        SharedPref sharedPref = SharedPref.getInstance(context);
//        sharedPref.setPremiumMember(false);
//        sharedPref.setFan(false);
//        Constant.isPremiumMember = false;
//        Constant.isFan = sharedPref.isFan();
//        Constant.SHOWBANNER = !Constant.isPremiumMember;
//        Constant.SHOWINTERSITIAL = !Constant.isPremiumMember;
//        CheckForConnection();
//        setupBilling();
//
//        premiumActivated = false;
//        if (context instanceof MainActivity) {
//            ((MainActivity) context).onResume();
//            Log.d(TAG, "desactivatePremium: on resume");
//        }
//
//
//    }
//
//    private void deviceNotSupported() {
//        SharedPref.getInstance(context).setDeviceSupported(false);
//        deviceNotSupported = true;
//        Constant.setDrawelyForFree(true);
//        if (context instanceof MainActivity) {
//            ((MainActivity) context).onResume();
//            Log.d(TAG, "deviceNotSupported: on resume");
//        }
//    }
//
//    private void activatePremium() {
//        Log.d(TAG, "activatePremium: IS ON");
//
//        SharedPref sharedPref = SharedPref.getInstance(context);
//        sharedPref.setPremiumMember(true);
//        sharedPref.setFan(true);
//        Constant.isPremiumMember = true;
//        Constant.isFan = sharedPref.isFan();
//        Constant.SHOWBANNER = !Constant.isPremiumMember;
//        Constant.SHOWINTERSITIAL = !Constant.isPremiumMember;
//
//        premiumActivated = true;
//        if (context instanceof MainActivity) {
//            ((MainActivity) context).onResume();
//            Log.d(TAG, "activatePremium: on resume");
//        }
//        setupAnimationCancelFreeTrial();
//        CheckForConnection();
//
//        //unlock current character
//        if (dialog_watch_ad != null && dialog_watch_ad.getVisibility() == View.VISIBLE) {
//            rl_dialog2_cancel();
//            ShowDialogDrawingOrColoring(currentArt, lastphoto, false, false);
//        }
//
//    }
//
//    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
//        @Override
//        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
//
//        }
//    };
//
//    private void handleAcknowledgePurchase(Purchase purchase) {
//        Log.d(TAG, "handleAcknowledgePurchase: ");
//        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
//            Log.d(TAG, "handleAcknowledgePurchase: PURCHASED");
//            // Grant entitlement to the user.
//
//            // Acknowledge the purchase if it hasn't already been acknowledged.
//            if (!purchase.isAcknowledged()) {
//                Log.d(TAG, "handleAcknowledgePurchase: NOT ALREADY Acknowledged");
//                AcknowledgePurchaseParams acknowledgePurchaseParams =
//                        AcknowledgePurchaseParams.newBuilder()
//                                .setPurchaseToken(purchase.getPurchaseToken())
//                                .build();
//                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
//
//                Log.d(TAG, "handleAcknowledgePurchase: Acknowledged");
//            } else {
//                Log.d(TAG, "handleAcknowledgePurchase: ALREADY Acknowledged");
//            }
//
//
//        }
//    }
//
//
//    ConsumeResponseListener consumeResponseListener = new ConsumeResponseListener() {
//        @Override
//        public void onConsumeResponse(BillingResult billingResult, String s) {
//
//        }
//    };
//
//    /*private void handleConsumePurchase(Purchase purchase) {
//
//        Log.d(TAG, "verifyPurchase Consumed ");
//        ConsumeParams consumeParams =
//                ConsumeParams.newBuilder()
//                        .setPurchaseToken(purchase.getPurchaseToken())
//                        .setDeveloperPayload(purchase.getDeveloperPayload())
//                        .build();
//
//        billingClient.consumeAsync(consumeParams, consumeResponseListener);
//    }*/
//
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putSerializable("isPremiumMember", Constant.isPremiumMember);
//        outState.putSerializable("SHOWBANNER", Constant.SHOWBANNER);
//        outState.putSerializable("SHOWINTERSITIAL", Constant.SHOWINTERSITIAL);
//        outState.putSerializable("isFan", Constant.isFan);
//        outState.putSerializable("drawelyPlusCount", Constant.drawelyPlusCount);
//
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        Constant.isPremiumMember = (boolean) savedInstanceState.getSerializable("isPremiumMember");
//        Constant.SHOWBANNER = (boolean) savedInstanceState.getSerializable("SHOWBANNER");
//        Constant.SHOWINTERSITIAL = (boolean) savedInstanceState.getSerializable("SHOWINTERSITIAL");
//        Constant.isFan = (boolean) savedInstanceState.getSerializable("isFan");
//        Constant.drawelyPlusCount = (int) savedInstanceState.getSerializable("drawelyPlusCount");
//    }
//
//
//}
