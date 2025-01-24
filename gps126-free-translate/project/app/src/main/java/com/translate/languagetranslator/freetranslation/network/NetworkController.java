package com.translate.languagetranslator.freetranslation.network;

import com.translate.languagetranslator.freetranslation.models.countriesData.Location;
import com.translate.languagetranslator.freetranslation.models.dictionaryModel.Dictionary;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class NetworkController {

    public static Call<List<Dictionary>> callDictionary(String url) {
        return ApiClient.geDictionaryRetrofit().create(ApiInterface.class).getDictionaryData(url);
    }

    public static Call<ResponseBody> callTDictionary(String url) {
        return ApiClient.geDictionaryRetrofit().create(ApiInterface.class).getWordMeaning(url);
    }
    public static Call<Location> callLocationApi() {
        return ApiClient.getLocationRetrofit().create(ApiInterface.class).getLocationDetails();
    }
}
