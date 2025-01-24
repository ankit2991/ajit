package com.messaging.textrasms.manager.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Classifier {

    private static final int STRENGTH = 1;
    private static final int HAM = 0;
    private static final int SPAM = 1;
    private static final double PrC = 0.5;
    Context context;
    DBHelper mDb;

    public Classifier(Context context) {
        this.context = context;
        mDb = new DBHelper(context);
    }

    public boolean isSpam(String sms) {

        float[] categoryWordProbability = {1, 1};
        String[] words = splitWord(sms);
        int[] allWords = {mDb.allWordCount(HAM), mDb.allWordCount(SPAM)};


        for (int c = 0; c < words.length; c++) {
            if (words[c].length() < 3)
                continue;

            for (int i = 0; i < 2; i++) {
                int allWordsInCategory = allWords[i];

                int wordInCategory = mDb.thisWordCount(words[c], i);
                int n = mDb.wordCount(words[c]);
                Log.d("compare", "totalword: " + sms + ">>>" + words[c] + ">>>" + allWords[i]);

                float probability = (float) wordInCategory / allWordsInCategory;
                categoryWordProbability[i] *= (float) (STRENGTH * PrC + n * probability) / (STRENGTH + n);

            }

        }


        Log.d("compare", "isSpam: " + sms + ">>>" + categoryWordProbability[SPAM] + ">>>" + categoryWordProbability[HAM] + ">>");

        return categoryWordProbability[SPAM] > categoryWordProbability[HAM];
    }

    public String[] splitWord(String sms) {
        Log.d("splitWord", "splitWord: " + sms);
        String[] words = TextUtils.split(sms, "\\W+");
        Set<String> temp = new HashSet<String>(Arrays.asList(words));
        String[] unique = temp.toArray(new String[temp.size()]);

        return unique;
    }

}