package com.translate.languagetranslator.freetranslation.activities.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


import com.facebook.shimmer.ShimmerFrameLayout;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.translate.languagetranslator.freetranslation.R;
import com.translate.languagetranslator.freetranslation.appUtils.AdsPriority;
import com.translate.languagetranslator.freetranslation.interfaces.SlideAdInterface;
//import com.weewoo.sdkproject.events.EventHelper


public class SliderAdAdapter extends SliderViewAdapter<SliderAdAdapter.SliderAdapterVH> {

    private Context context;

/*    private FacebookAdsUtils facebookAdsUtils0;
    private FacebookAdsUtils facebookAdsUtils1;
    private AdmobUtils admobClassic0;
    private AdmobUtils admobClassic1;*/

    boolean isFirstFailed, isSecondFailed = false;
    boolean isFirstLoaded, isSecondLoaded = false;

    SlideAdInterface slideAdInterface;

    private AdsPriority adsPriorityOne, adsPriorityTwo;

//    private EventHelper eventHelper = new EventHelper();

    public void setAdInterface(SlideAdInterface slideAdInterface) {
        this.slideAdInterface = slideAdInterface;
    }


    public SliderAdAdapter(Context context,  AdsPriority adsPriorityOne, AdsPriority adsPriorityTwo) {
        this.context = context;

        this.adsPriorityOne = adsPriorityOne;
        this.adsPriorityTwo= adsPriorityTwo;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_slider_layout, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {

        switch (position) {
            case 0:
              /*  this.admobClassic0 = new AdmobUtils(context);
                this.facebookAdsUtils0 = new FacebookAdsUtils(context);*/

                if (!isFirstFailed && !isFirstLoaded) {
                    loadSliderFirstAmAd(viewHolder, adsPriorityOne);

                    //removed facebook native ads
/*                    switch (adsPriorityOne) {
                        case ADMOB:
                        case ADMOB_FACEBOOK:
                            loadSliderFirstAmAd(viewHolder, adsPriorityOne, nativeAdsIdTypeAdmob, nativeAdsIdTypeFaceBook);
                            break;
                        case FACEBOOK:
                        case FACEBOOK_ADMOB:
                            loadSliderFirstFbAd(viewHolder, adsPriorityOne, nativeAdsIdTypeFaceBook, nativeAdsIdTypeAdmob);
                            break;
                        default:
                    }*/

                }


                break;
            case 1:

                if (!isSecondFailed && !isSecondLoaded) {

                    loadSliderSecondAmAd(viewHolder, adsPriorityTwo);

                    //removed facebook native ads
                    /*switch (adsPriorityTwo) {
                        case ADMOB:
                        case ADMOB_FACEBOOK:
                            loadSliderSecondAmAd(viewHolder, adsPriorityTwo, nativeAdsIdTypeAdmob, nativeAdsIdTypeFaceBook);
                            break;
                        case FACEBOOK:
                        case FACEBOOK_ADMOB:
                            loadSliderSecondFbAd(viewHolder, adsPriorityTwo, nativeAdsIdTypeFaceBook, nativeAdsIdTypeAdmob);
                            break;
                        default:
                    }*/


                }


                break;

        }

    }

    private void loadSliderSecondAmAd(SliderAdapterVH viewHolder, AdsPriority adsPriority) {

//        admobUtils.loadMainNativeAd(viewHolder.nativeAdmobLayout, R.layout.main_native, adsIdTypeAm);

        /*
            public void onNativeAdLoaded() {

                viewHolder.nativeAdmobLayout.setVisibility(View.VISIBLE);
                viewHolder.fbNativeAdLayout.setVisibility(View.INVISIBLE);
                slideAdInterface.slideAdLoaded();
                isSecondLoaded = true;
                viewHolder.shimmerFrameLayout.setVisibility(View.GONE);
            }
            public void onNativeAdError() {
                viewHolder.nativeAdmobLayout.setVisibility(View.INVISIBLE);
                    viewHolder.fbNativeAdLayout.setVisibility(View.INVISIBLE);
                    isSecondFailed = true;
                    if (isFirstFailed) {
                        slideAdInterface.slideAddFailed();

                    }



            }
              */



        //        TODO()4.5.0  I guess it was a view holder and native ad was being loaded here see above comments

    }



    private void loadSliderFirstAmAd(SliderAdapterVH viewHolder, AdsPriority adsPriority) {


        //admobUtils.loadMainNativeAd(viewHolder.nativeAdmobLayout, R.layout.main_native, adsIdTypeAm);

          /*  public void onNativeAdLoaded() {

                viewHolder.nativeAdmobLayout.setVisibility(View.VISIBLE);
                viewHolder.fbNativeAdLayout.setVisibility(View.INVISIBLE);
                isFirstLoaded = true;
                slideAdInterface.slideAdLoaded();
                viewHolder.shimmerFrameLayout.setVisibility(View.GONE);
            }
            public void onNativeAdError() {

                    viewHolder.nativeAdmobLayout.setVisibility(View.INVISIBLE);
                    isFirstFailed = true;
                    if (isSecondFailed) {
                        slideAdInterface.slideAddFailed();
                    }
            }*/


        //        TODO()4.5.0  I guess it was a view holder and native ad was being loaded here see above comments




    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return 1;
    }

    public class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        private FrameLayout nativeAdmobLayout;
        //private NativeAdLayout fbNativeAdLayout;
        ShimmerFrameLayout shimmerFrameLayout;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            nativeAdmobLayout = itemView.findViewById(R.id.native_admob_container);
            //fbNativeAdLayout = itemView.findViewById(R.id.native_ad_container);
            shimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container);
        }
    }


}
