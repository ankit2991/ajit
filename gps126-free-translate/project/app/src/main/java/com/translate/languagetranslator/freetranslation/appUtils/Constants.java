package com.translate.languagetranslator.freetranslation.appUtils;

import android.app.Activity;
import android.content.ClipboardManager;
import android.hardware.Camera;
import android.os.Build;
import android.speech.tts.Voice;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.adapty.models.AdaptyPaywall;
import com.adapty.models.AdaptyPaywallProduct;
import com.adapty.ui.AdaptyUI;
import com.translate.languagetranslator.freetranslation.R;
import com.translate.languagetranslator.freetranslation.database.entity.ConversationModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Constants {

    //     Intent key
    public static String PAYWALL_TYPE = "paywall_type";

    // Intent value
    public static String GPS_ONBOARDING = "Onboarding";
    public static String GPS_DEFAULT = "Default";
    public static String GPS_PREMIUM = "Premium";

    public static AdaptyPaywall adaptyPaywallOnboarding = null;
    public static AdaptyPaywall adaptyPaywallDefault = null;
    public static AdaptyPaywall adaptyPaywallPremium = null;
    public static List<AdaptyPaywallProduct> adaptyProductOnboarding = null;
    public static List<AdaptyPaywallProduct> adaptyProductDefault = null;
    public static List<AdaptyPaywallProduct> adaptyProductPremium = null;
    public static AdaptyUI.ViewConfiguration adaptyConfigOnboarding = null;
    public static AdaptyUI.ViewConfiguration adaptyConfigDefault = null;
    public static AdaptyUI.ViewConfiguration adaptyConfigPremium = null;
    public static final String setPrice = "";

    public static final String APP_OPEN_TIMES = "app_open_times";
    public static final int YEARLY_SUBSCRIPTION_APP_OPEN_TIMES_TO_SHOW_RATING_DIALOG = 4;
    public static final String SHOWED_RATING_DIALOG_AFTER_FIRST_RENEWAL = "rating_dialog_after_first_renewal";

    public static final String IS_PREMIUM = "is_premium";
    public static final String CHECK_INTER_AD_SHOW = "check_inter_ad_show";
    public static final String SPLASH_FIRST = "isPolicyAccepted";
    public static final String ocrIntentKey = "102";


    public static final int REQ_CODE_LANGUAGE_SELECTION = 1;
    public static final int REQ_CODE_SPEECH_INPUT = 2;
    public static final int REQUEST_CODE_IMAGE_SELECTOR = 3;
    public static final int REQUEST_CODE_LANGUAGE_OCR = 4;
    public static final int REQUEST_CODE_OCR_BACK = 10;
    public static final int REQ_CODE_BOOKMARK = 11;
    public static final int REQ_CODE_HISTORY = 12;
    public static final int REQ_CODE_RATEUS = 15;

    //Phrasebook
    public static final String LANG_FROM_PHR = "lang_from_phr";
    public static final int LANG_FROM_FLAG_PHR = R.drawable.english;
    public static final int LANG_TO_FLAG_PHR = R.drawable.french;
    public static final String LANG_FROM_CODE_PHR = "lang_from_code_phr";
    public static final String LANG_FROM_CODE_SUPPORT_PHR = "lang_from_code_support_phr";
    public static final String LANG_FROM_MEANING_PHR = "lang_from_meaning_phr";
    public static final String LANG_TO_MEANING_PHR = "lang_to_meaning_phr";
    public static final String LANG_TO_PHR = "lang_to_phr";
    public static final String LANG_TO_CODE_PHR = "lang_to_code_phr";
    public static final String LANG_TO_CODE_SUPPORT_PHR = "lang_to_code_support_phr";


    //Fictinal language
    public static final String LANG_FROM_FIC = "lang_from_fic";
    public static final String LANG_FROM_CODE_FIC = "lang_from_code_fic";
    public static final String LANG_FROM_CODE_SUPPORT_FIC = "lang_from_code_support_fic";
    public static final String LANG_FROM_MEANING_FIC = "lang_from_meaning_fic";
    public static final String LANG_TO_MEANING_FIC = "lang_to_meaning_fic";
    public static final String LANG_TO_FIC = "lang_to_fic";
    public static final String LANG_TO_CODE_FIC = "lang_to_code_fic";
    public static final String LANG_TO_CODE_SUPPORT_FIC = "lang_to_code_support_fic";

    public static final String LANG_FROM = "lang_from";
    public static final String LANG_FROM_CODE = "lang_from_code";
    public static final String LANG_FROM_CODE_SUPPORT = "lang_from_code_support";
    public static final String LANG_FROM_MEANING = "lang_from_meaning";
    public static final String LANG_TO = "lang_to";
    public static final String LANG_TO_CODE = "lang_to_code";
    public static final String LANG_TO_CODE_SUPPORT = "lang_to_code_support";
    public static final String LANG_TO_MEANING = "lang_to_meaning";
    public static final String LANG_TYPE_NORMAL = "lang_normal";
    public static final String LANG_TYPE_OCR = "lang_ocr";

    public static final String CLIP_RIGHT_LANG_NAME = "clip_right_lang_name";
    public static final String CLIP_RIGHT_LANG_CODE = "clip_right_lang_code";
    public static final String CLIP_RIGHT_LANG_MEANING = "clip_right_lang_meaning";
    public static final String CLIP_RIGHT_LANG_SUPPORT = "lang_from_code_support";


    public static final String DETAIL_SOURCE_MAIN = "source_main";
    public static final String DETAIL_SOURCE_CAM = "source_camera";

    public static final String TRANSLATE_TYPE = "translate_type";
    public static final String TRANSLATE_TYPE_MIC = "translate_type_mic";
    public static final String TRANSLATE_TYPE_TEXT = "translate_type_text";
    public static final String TRANSLATE_ORIGIN = "translate_origin";
    public static final String TRANSLATE_ORIGIN_MAIN = "translate_origin_main";
    public static final String TRANSLATE_ORIGIN_NOT_MAIN = "translate_origin_not_main";
    public static final String TRANSLATE_INTENT_DATA = "translate_intent_data";


    public static final String INTENT_KEY_LANG_NAME = "lang_name";
    public static final String INTENT_KEY_LANG_CODE = "lang_code";
    public static final String INTENT_KEY_LANG_SUPPORT = "support_lang_code";
    public static final String INTENT_KEY_LANG_MEANING = "lang_meaning";
    public static final int INTENT_KEY_LANG_FLAG = R.drawable.english;

    public static final String INTENT_KEY_CONVERSATION_ORIGIN = "conversation_origin";
    public static final String CONVERSATION_ORIGIN_MAIN = "conversation_main";
    public static final String CONVERSATION_ORIGIN_LIST = "conversation_list";
    public static final String INTENT_PARAM_CONVERSATION_NAME = "param_conversation_name";
    public static final String INTENT_PARAM_CONVERSATION_LIST = "param_conversation_list";

    public static final String INTENT_KEY_HISTORY_INPUT_WORD = "history_input_word";
    public static final String INTENT_KEY_HISTORY_TRANSLATED_WORD = "history_translated_word";
    public static final String INTENT_KEY_HISTORY_INPUT_LANG = "history_input_lang";
    public static final String INTENT_KEY_HISTORY_TRANSLATED_LANG = "history_translated_lang";

    public static final String DICTIONARY_LANG_POSITION = "lang_position_dictionary";
    public static final String DICTIONARY_LANG_CODE = "lang_code_dictionary";
    public static final String DICTIONARY_LANG_NAME = "lang_name_dictionary";

    // counts
    public static final String HOME_BUTTON_COUNT = "home_button_count";
    public static final String TRANSLATE_BUTTON_COUNT = "translate_button_count";

    public static final String SUBSCRIPTION_SCREEN_SHOW = "Subsucription_Screen_Enabled";

    public static final String INTENT_KEY_SOURCE_HISTORY = "source_history";
    public static final String INTENT_KEY_SOURCE_BOOKMARK = "source_bookmark";

    public static final String KEY_ORIGIN = "origin";
    public static final String INTENT_KEY_CLIP_BOARD_DATA = "data_clip_board_copied";


//    public static final String MAIN_INTER_SEARCH_COUNT = "Translator_MainScreen_ButtonClick_Int_Show_AtCounter";
//    public static final String MAIN_INTER_BACK_SEARCH_COUNT = "Translator_MainScreen_ButtonBackPress_Int_Show_AtCounter";
//    public static final String TRANSLATOR_INTER_SEARCH_COUNT = "Translator_TranslatorScreen_Int_Show_AtCounter";
//    public static final String CAMERA_INTER_SEARCH_COUNT = "Translator_CameraTranslate_Int_Show_AtCounter";
//    public static final String CONVERSATION_INTER_SEARCH_COUNT = "Translator_Conversation_Int_Show_AtCounter";
//    public static final String DICTIONARY_INTER_SEARCH_COUNT = "Translator_Dictionary_Int_Show_AtCounter";
//    /// camera new
//    public static final String CAMERA_INTER_SEARCH_COUNT_FORWARD = "Translator_Camera_Forward_Int_Show_AtCounter";
//    public static final String CAMERA_INTER_SEARCH_COUNT_BACKWARD = "Translator_Camera_Backward_Int_Show_AtCounter";
//    public static final String CAMERA_INTER_CONTROLLER_FORWARD = "is_camera_inter_show_forward";
//    public static final String CAMERA_INTER_CONTROLLER_BACKWARD = "is_camera_inter_show_backward";
//    public static final String CAMERA_SCREEN_AD_PRIORITY_INTER_FORWARD = "inter_am_priority_camera_on_forward";
//    public static final String CAMERA_SCREEN_AD_PRIORITY_INTER_BACKWARD = "inter_fb_priority_camera_on_backward";
//    public static final String CAMERA_SCREEN_AD_PRIORITY_NATIVE = "native_am_priority_camera";
//    public static final String CAMERA_NATIVE_CONTROLLER = "is_camera_native_show";
//
//
//    // priority
//    public static final String HOME_SCREEN_AD_PRIORITY_ONRESUME = "inter_fb_priority_home_on_resume";
//    public static final String HOME_SCREEN_AD_PRIORITY_ONCLICK = "inter_fb_priority_home_on_click";
//    public static final String TRANSLATOR_SCREEN_AD_PRIORITY_ON_CLICK = "inter_fb_priority_translator_on_click";
//    public static final String TRANSLATOR_SCREEN_AD_PRIORITY_BANNER = "banner_fb_priority_translator";
//    public static final String CONVERSATION_SCREEN_AD_PRIORITY_ON_SEARCH = "inter_fb_priority_conversation_on_search";
//    public static final String CONVERSATION_SCREEN_AD_PRIORITY_NATIVE = "native_fb_priority_conversation";
//    public static final String DICTIONARY_SCREEN_AD_PRIORITY_ON_SEARCH = "inter_fb_priority_dictionary_on_search";
//    public static final String DICTIONARY_SCREEN_AD_PRIORITY_NATIVE = "native_fb_priority_dictionary";
//    public static final String BOOKMARK_SCREEN_AD_PRIORITY_NATIVE = "native_fb_priority_bookmark";
//    public static final String HISTORY_SCREEN_AD_PRIORITY_NATIVE = "native_fb_priority_history";
//    public static final String HISTORY_DETAIL_SCREEN_AD_PRIORITY_NATIVE = "native_fb_priority_history_detail";
//    public static final String LANGUAGE_SCREEN_AD_PRIORITY_NATIVE_TOP = "native_fb_priority_language_selection_top";
//    public static final String LANGUAGE_SCREEN_AD_PRIORITY_NATIVE_BOTTOM = "native_am_priority_language_selection_bottom";
//    public static final String CAMERA_SCREEN_AD_PRIORITY_INTER = "inter_fb_priority_camera_on_click";
//    public static final String CAMERA_SCREEN_AD_PRIORITY_BANNER = "banner_am_priority_camera";
//    public static final String SPLASH_INTER_PRIORITY = "inter_aftersplash_facebook_priority";
//

    // ads controller
//    public static final String SPLASH_NATIVE_CONTROLLER = "is_splash_native_show";
//    public static final String SPLASH_INTER_CONTROLLER = "is_splash_inter_show";
//    public static final String APP_ONRESUME_INTER_CONTROLLER = "is_app_onresume_show";
//    public static final String HOME_ONCLICK_INTER_CONTROLLER = "is_home_onclik_show";
//    public static final String HOME_BACKPRESS_INTER_CONTROLLER = "is_home_backpress_show";
//    public static final String HOME_BACKPRESS_NATIVE_CONTROLLER = "is_home_native_show";
//    public static final String TRANSLATOR_NATIVE_CONTROLLER = "is_translator_native_show";
//    public static final String TRANSLATOR_BANNER_CONTROLLER = "is_translator_banner_show";
//    public static final String TRANSLATOR_INTER_CONTROLLER = "is_translator_inter_show";
//    public static final String BOOKMARK_NATIVE_CONTROLLER = "is_bookmark_native_show";
//    public static final String CONVERSATION_NATIVE_CONTROLLER = "is_conversation_native_show";
//    public static final String CONVERSATION_INTER_CONTROLLER = "is_conversation_inter_show";
//    public static final String DICTIONARY_NATIVE_CONTROLLER = "is_dictionary_native_show";
//    public static final String DICTIONARY_INTER_CONTROLLER = "is_dictionary_inter_show";
//    public static final String HISTORY_NATIVE_CONTROLLER = "is_history_native_show";
//    public static final String LANGUAGE_NATIVE_CONTROLLER = "is_language_selection_native_show";
//    public static final String CAMERA_INTER_CONTROLLER = "is_camera_inter_show";
//    public static final String CAMERA_BANNER_CONTROLLER = "is_camera_banner_show";
//    public static final String HOME_SCREEN_SUBSCRIPTION_CONTROLLER = "home_screen_subscription_show";
//    public static final String HOME_SCREEN_DIALOG_CONTROLLER = "home_screen_dialog_show";


    public static final String NAME_SOURCE_LANG = "English";
    public static final String NAME_TARGET_LANG = "French";

    public static final String SOURCE_LANG_NAME_OCR = "ocr_lang_name_src";
    public static final String SOURCE_LANG_CODE_OCR = "ocr_lang_code_src";
    public static final String SOURCE_LANG_CODE_PAIR_OCR = "ocr_lang_code_pair_src";

    public static final String TARGET_LANG_NAME_OCR = "ocr_lang_name_tar";
    public static final String TARGET_LANG_CODE_OCR = "ocr_lang_code_tar";
    public static final String TARGET_LANG_CODE_PAIR_OCR = "ocr_lang_code_pair_tar";

    public static final String TABLE_NAME_SAVED_CONVERSATION = "savedConversation";

    //ads disable or enable
    public static final String ADS_ONLY = "ads_only";

    // subscription
    public static final String SUBSCRIPTION_ID_MONTH = "mon_subscription";
    public static final String SUBSCRIPTION_ID_YEAR = "year_sub";
    public static final String SUBSCRIPTION_ID_WEEKLY = "weekly_subscription3";

    public static final String SUBSCRIPTION_ID_YEAR_BUY_TIME = "year_sub_buy_time";
    public static final String SUBSCRIPTION_ID_DISCOUNT = "discuount_offer";
    public static final String SUBSCRIPTION_ID_LIFE = "life_subscription";
    public static final String IN_APP_REMOVE_AD = "remove_ads";
    public static final String MERCHANT_ID = "00121273569685518950";
    public static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvryKDvvaQn0upUaDTVYdbp4Ih0QZIKXKLb1FH6061zNNYZa4608rzYZkCg7LevtMUeBKGkpihBhEmeTurMKeR9W9v6uxMdnXJLM6PBUADoYUFi6u4C3ud/wmVLl6lMgayB7oxgmyvj5RyC0rhaAEo1wL4ZjBMuzdkl1rSBQ8aikI0QjZbaNR6VA+OnFLYNUxSAVPDMA2g5J1GO1XtQCRCGeB87EKUUhst+Xe2xrNJDIfP7sqVgtFJLmHjBg+MukN8km0yyF60a+DvhP8rlBv1ESRGutyIIDIrANT8X+rMPBnj+Sm7kc6gbXFzIrxldn6VgZFlUfAQnMdX6cy4Wd+vQIDAQAB";
    public static final String RATEUS_FIRST_TIME = "rateus_first_time";
    public static final String RATEUS_FIRST_COMPLETE = "rateus_complete";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Voice getVoice() {
        Set<String> a = new HashSet<>();
        a.add("male");//here you can give male if you want to select male voice.
        Voice v = new Voice("en-us-x-sfg#male_3-local", new Locale("en", "UK"), 400, 200, true, a);
        return v;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Voice setFemaleVoice() {
        Set<String> a = new HashSet<>();
        a.add("female");
        Voice v = new Voice("en-gb-x-rjs#female_3-local", new Locale("en", "UK"), 400, 200, true, a);


        return v;
    }

    public static boolean containsWhiteSpace(final String testCode) {
        if (testCode != null) {
            for (int i = 0; i < testCode.length(); i++) {
                if (Character.isWhitespace(testCode.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<ConversationModel> convertJsonToList(String jsonStr) throws Exception {

        List<ConversationModel> modelList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONArray data = jsonObject.getJSONArray("conversationList:");
        for (int i = 0; i < data.length(); i++) {
            JSONObject c = data.getJSONObject(i);
            int id = c.getInt("id");
            String inputWord = c.getString("inputWord");
            String translatedWord = c.getString("translatedWord");
            String origin = c.getString("origin");
            String translatedWordLangCode = c.getString("translatedWordLangCode");
            String inputWordLangCode = c.getString("inputWordLangCode");

            ConversationModel conversationModel = new ConversationModel();
            conversationModel.setInputWord(inputWord);
            conversationModel.setTranslatedWord(translatedWord);
            conversationModel.setOrigin(origin);
            conversationModel.setTranslatedWordLangCode(translatedWordLangCode);
            conversationModel.setInputWordLangCode(inputWordLangCode);

            conversationModel.setId(id);
            modelList.add(conversationModel);

        }

        return modelList;
    }

    public static Camera.Parameters getCamParams(Activity activity, Camera.Parameters parms) {
        Camera.Parameters parameters = parms;
        List supportedPictureSizes = parms.getSupportedPictureSizes();
        for (int i = 0; i < supportedPictureSizes.size() - 1; i++) {
            int i2 = 0;
            while (i2 < (supportedPictureSizes.size() - 1) - i) {
                int i3 = i2 + 1;
                if (((Camera.Size) supportedPictureSizes.get(i2)).height > ((Camera.Size) supportedPictureSizes.get(i3)).height) {
                    Camera.Size size = (Camera.Size) supportedPictureSizes.get(i2);
                    supportedPictureSizes.set(i2, supportedPictureSizes.get(i3));
                    supportedPictureSizes.set(i3, size);
                }
                i2 = i3;
            }
        }
        if (supportedPictureSizes.size() > 1) {
            int i4 = 0;
            while (true) {
                if (i4 >= supportedPictureSizes.size()) {
                    break;
                } else if (((Camera.Size) supportedPictureSizes.get(i4)).height >= activity.getWindowManager().getDefaultDisplay().getWidth()) {
                    parameters.setPictureSize(((Camera.Size) supportedPictureSizes.get(i4)).width, ((Camera.Size) supportedPictureSizes.get(i4)).height);
                    break;
                } else {
                    i4++;
                }
            }
        }

        return parameters;

    }

//    public static String getTextFromClip(ClipboardManager mCM) {
//        if (mCM != null) {
//            ClipDescription clipDescription = mCM.getPrimaryClipDescription();
//            if (clipDescription != null) {
//                clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
//                if (mCM.hasPrimaryClip() && mCM.getPrimaryClip() != null) {
//                    ClipData mClip = mCM.getPrimaryClip();
//                    String newClip = "";
//
//                    int itemCount = mClip.getItemCount();
//                    if (itemCount > 0) {
//                        ClipData.Item item = mClip.getItemAt(0);
//
//                        if (item != null) {
//                            newClip = mClip.toString();
//                            if (!newClip.contains("NULL")) {
//                                checkText();
//
//                            } else {
//
//                            }
//                        }
//
//
//                    }
//
//                }
//            }
//        }
//    }

    private String checkText(ClipboardManager mCM) {

        String copiedWord = null;
        try {
            if (mCM != null) {
                CharSequence charSequence = mCM.getText();

                if (charSequence != null) {
                    copiedWord = charSequence.toString().trim();
                }


                if (copiedWord != null && !copiedWord.isEmpty()) {
                    return copiedWord;
                }


            } else {
                return copiedWord;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return copiedWord;

    }


}
