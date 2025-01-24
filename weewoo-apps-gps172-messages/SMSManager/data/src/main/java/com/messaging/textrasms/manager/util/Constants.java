package com.messaging.textrasms.manager.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {

    public static final String[] CATEGORIES = {"ham_count", "spam_count"};

    public static final String EXTRA_REMINDER_NAME = "extra_reminder_name";
    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String KEY_ID = "id";
    public static final String KEY_FROM = "sender_id";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date";

    public static final String KEY_REMINDER_ID = "reminder_id";
    public static final String KEY_REMINDER_TITLE = "reminder_title";
    public static final String KEY_REMINDER_TIME = "reminder_time";
    public static final String KEY_REMINDER_COLOR = "reminder_color";
    public static final String KEY_CONTACT_NAME = "contact_name";
    public static final String KEY_CONTACT_NUMBER = "contact_number";
    public static final String KEY_CONTACT_ID = "contact_id";

    public static final String EXTRA_MOBILE_NUMBER = "mobile_number";
    public static final String EXTRA_CALL_DURATION = "call_duration";
    public static final String EXTRA_IS_MISSED_CALL = "is_missed_call";
    public static final String ACTION_SHOULD_DISPLAY_PAYMENT = "ACTION_SHOULD_DISPLAY_PAYMENT";

    public static ArrayList<String> loadSpamWords(Context context, String name) {
        ArrayList<String> SpamWord = new ArrayList<String>();
        String line, word = "";
        int weight = 0;
        try {
            BufferedReader reader;
            final InputStream file = context.getAssets().open(name);
            reader = new BufferedReader(new InputStreamReader(file));
            while ((line = reader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreElements()) {

                    word = st.nextElement().toString();
                }
                SpamWord.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(SpamWord);
        return SpamWord;
    }

    public static ArrayList<String> loadTransactionWords(Context context, String name) {
        ArrayList<String> SpamWord = new ArrayList<String>();
        String line, word = "";
        try {
            BufferedReader reader;
            final InputStream file = context.getAssets().open(name);
            reader = new BufferedReader(new InputStreamReader(file));
            while ((line = reader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreElements()) {

                    word = st.nextElement().toString();
                    Log.d("loadSpamWords", "loadSpamWords: " + word + ">>" + line);
                }
                SpamWord.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(SpamWord);
        return SpamWord;
    }

    public static ArrayList<String> loadPromotionWords(Context context, String name) {
        ArrayList<String> SpamWord = new ArrayList<String>();
        String line, word = "";
        try {
            BufferedReader reader;
            final InputStream file = context.getAssets().open(name);
            reader = new BufferedReader(new InputStreamReader(file));
            while ((line = reader.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreElements()) {

                    word = st.nextElement().toString();
                }
                SpamWord.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(SpamWord);
        return SpamWord;
    }


    public static boolean parsevalues(String title, String body_val) {
        String smsDto = body_val;
        Pattern regEx_name = Pattern.compile("[a-zA-Z0-9]{2}(-|)[a-zA-Z0-9]{6}");
        Pattern regEx_account = Pattern.compile("[0-9]*[Xx\\*]*[0-9]*[Xx\\*]+[0-9]{3,}");

        Matcher matcher = regEx_name.matcher(title);

        Matcher m = regEx_account.matcher(smsDto);
        if (m.find()) {
            return matcher.find();

        } else return false;
    }

}