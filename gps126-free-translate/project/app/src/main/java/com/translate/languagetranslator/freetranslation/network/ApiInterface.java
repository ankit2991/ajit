package com.translate.languagetranslator.freetranslation.network;


import com.translate.languagetranslator.freetranslation.models.countriesData.Location;
import com.translate.languagetranslator.freetranslation.models.dictionaryModel.Dictionary;
import com.translate.languagetranslator.freetranslation.models.translationModel.TranslationResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiInterface {




    @POST("language/translate/v2")
    Call<TranslationResponse> getGoogleTranslation(
            @Query("q") String q,
            @Query("target") String target,
            @Query("format") String format,
            @Query("source") String source,
            @Query("key") String key
    );


    @POST
    Call<TranslationResponse> getTranslationPojo(@Url String url);

    @GET
    Call<List<Dictionary>> getDictionaryData(@Url String url);

    @GET
    Call<ResponseBody> getWordMeaning(@Url String url);

    @GET("/json")
    Call<Location> getLocationDetails();

}
