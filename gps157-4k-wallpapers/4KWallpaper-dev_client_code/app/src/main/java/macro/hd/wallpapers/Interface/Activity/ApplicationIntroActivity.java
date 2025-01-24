/*
 * MIT License
 *
 * Copyright (c) 2017 Jan Heinrich Reimer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package macro.hd.wallpapers.Interface.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Interface.Fragments.IntroFagment.FragmentIntro1;
import macro.hd.wallpapers.Interface.Fragments.IntroFagment.FragmentIntro2;
import macro.hd.wallpapers.Interface.Fragments.IntroFagment.FragmentIntro3;
import macro.hd.wallpapers.Interface.Fragments.IntroFagment.FragmentIntro4;
import macro.hd.wallpapers.Interface.Fragments.IntroFagment.FragmentIntro5;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;

import java.util.Locale;

public class ApplicationIntroActivity extends IntroActivity {

    public static final String EXTRA_FULLSCREEN = "macro.hd.wallpapers.EXTRA_FULLSCREEN";
    public static final String EXTRA_SCROLLABLE = "macro.hd.wallpapers.EXTRA_SCROLLABLE";
    public static final String EXTRA_CUSTOM_FRAGMENTS = "macro.hd.wallpapers.EXTRA_CUSTOM_FRAGMENTS";
    public static final String EXTRA_PERMISSIONS = "macro.hd.wallpapers.EXTRA_PERMISSIONS";
    public static final String EXTRA_SHOW_BACK = "macro.hd.wallpapers.EXTRA_SHOW_BACK";
    public static final String EXTRA_SHOW_NEXT = "macro.hd.wallpapers.EXTRA_SHOW_NEXT";
    public static final String EXTRA_SKIP_ENABLED = "macro.hd.wallpapers.EXTRA_SKIP_ENABLED";
    public static final String EXTRA_FINISH_ENABLED = "macro.hd.wallpapers.EXTRA_FINISH_ENABLED";
    public static final String EXTRA_GET_STARTED_ENABLED = "macro.hd.wallpapers.EXTRA_GET_STARTED_ENABLED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        boolean fullscreen = intent.getBooleanExtra(EXTRA_FULLSCREEN, false);
        boolean scrollable = intent.getBooleanExtra(EXTRA_SCROLLABLE, false);
        boolean customFragments = intent.getBooleanExtra(EXTRA_CUSTOM_FRAGMENTS, true);
        boolean permissions = intent.getBooleanExtra(EXTRA_PERMISSIONS, true);
        boolean showBack = intent.getBooleanExtra(EXTRA_SHOW_BACK, true);
        boolean showNext = intent.getBooleanExtra(EXTRA_SHOW_NEXT, true);
        boolean skipEnabled = intent.getBooleanExtra(EXTRA_SKIP_ENABLED, true);
        boolean finishEnabled = intent.getBooleanExtra(EXTRA_FINISH_ENABLED, true);
        boolean getStartedEnabled = intent.getBooleanExtra(EXTRA_GET_STARTED_ENABLED, true);

        setFullscreen(fullscreen);

        super.onCreate(savedInstanceState);
        setLocale();
        setButtonBackFunction(skipEnabled ? BUTTON_BACK_FUNCTION_SKIP : BUTTON_BACK_FUNCTION_BACK);
        setButtonNextFunction(
                finishEnabled ? BUTTON_NEXT_FUNCTION_NEXT_FINISH : BUTTON_NEXT_FUNCTION_NEXT);
        setButtonBackVisible(showBack);
        setButtonNextVisible(showNext);
        setButtonCtaVisible(getStartedEnabled);
        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);

        setButtonBackOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.e("StatusIntroActivity","finisha");
                launchHomeScreen();
            }
        });

//        addSlide(new SimpleSlide.Builder()
//                .title(R.string.title_material_metaphor)
//                .description(R.string.description_material_metaphor)
//                .image(R.mipmap.ic_launcher_about)
//                .background(R.color.color_material_metaphor)
//                .backgroundDark(R.color.color_dark_material_metaphor)
//                .scrollable(scrollable)
//                .build());

        final Slide loginSlide;

            loginSlide = new FragmentSlide.Builder()
                    .background(R.color.color_material_metaphor)
                    .backgroundDark(R.color.color_dark_material_metaphor)
                    .fragment(FragmentIntro1.newInstance(false))
                    .build();
            addSlide(loginSlide);


        final Slide loginSlide2;

        loginSlide2 = new FragmentSlide.Builder()
                .background(R.color.color_material_bold)
                .backgroundDark(R.color.color_dark_material_bold)
                .fragment(FragmentIntro2.newInstance())
                .build();
        addSlide(loginSlide2);


        if(CommonFunctions.isDoubleWallpaperSupported()) {
            final Slide loginSlide3;

            loginSlide3 = new FragmentSlide.Builder()
                    .background(R.color.color_material_motion)
                    .backgroundDark(R.color.color_dark_material_motion)
                    .fragment(FragmentIntro3.newInstance())
                    .build();
            addSlide(loginSlide3);
        }



//        addSlide(new SimpleSlide.Builder()
//                .title(R.string.title_material_metaphor)
//                .description(R.string.description_material_metaphor)
//                .image(R.mipmap.ic_launcher_about)
//                .background(R.color.color_material_metaphor)
//                .backgroundDark(R.color.color_dark_material_metaphor)
//                .scrollable(scrollable)
//                .build());

//        addSlide(new SimpleSlide.Builder()
//                .title(R.string.title_material_bold)
//                .description(R.string.description_material_bold)
//
//                .background(R.color.color_material_bold)
//                .backgroundDark(R.color.color_dark_material_bold)
//                .scrollable(scrollable)
//                .build());

//        addSlide(new SimpleSlide.Builder()
//                .title(R.string.title_material_motion)
//                .description(R.string.description_material_motion)
//                .image(R.mipmap.ic_launcher_about)
//                .background(R.color.color_material_motion)
//                .backgroundDark(R.color.color_dark_material_motion)
//                .scrollable(scrollable)
//                .build());


        final Slide loginSlide4;

        loginSlide4 = new FragmentSlide.Builder()
                .background(R.color.color_custom_fragment_2)
                .backgroundDark(R.color.color_dark_custom_fragment_2)
                .fragment(FragmentIntro4.newInstance())
                .build();
        addSlide(loginSlide4);

//        addSlide(new SimpleSlide.Builder()
//                .title(R.string.title_material_shadow)
//                .description(R.string.description_material_shadow)
//                .image(R.mipmap.ic_launcher_about)
//                .background(R.color.color_material_shadow)
//                .backgroundDark(R.color.color_dark_material_shadow)
//                .scrollable(scrollable)
//                .build());


//        addSlide(new SimpleSlide.Builder()
//                .title(R.string.title_material_auto)
//                .description(R.string.description_material_auto)
//                .image(R.mipmap.ic_launcher_about)
//                .background(R.color.color_custom_fragment_1)
//                .backgroundDark(R.color.color_dark_custom_fragment_1)
//                .scrollable(scrollable)
//                .build());



        final Slide loginSlide5;

        loginSlide5 = new FragmentSlide.Builder()
                .background(R.color.color_custom_fragment_1)
                .backgroundDark(R.color.color_dark_custom_fragment_1)
                .fragment(FragmentIntro1.newInstance(true))
                .build();
        addSlide(loginSlide5);


        if(CommonFunctions.isEdgeWallpaperSupported()) {
            final Slide loginSlide6;

            loginSlide6 = new FragmentSlide.Builder()
                    .background(R.color.color_custom_fragment_5)
                    .backgroundDark(R.color.color_dark_fragment_5)
                    .fragment(FragmentIntro5.newInstance())
                    .build();
            addSlide(loginSlide6);
        }


        //Feel free to add and remove page change listeners
        /*
        addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        */
    }

    private void launchHomeScreen() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void setLocale() {
        String languageCode="";
        SettingStore settingStore = SettingStore.getInstance(this);
        if (settingStore.getLanguage()==0) {
            languageCode="en";
        }else if (settingStore.getLanguage()==1) {
            languageCode="hi";
        }

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            createConfigurationContext(config);
        } else {
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
