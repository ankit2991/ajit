
package com.translate.languagetranslator.freetranslation.models.translationModel;


import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
@Keep
public class TranslationResponse {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    @SerializedName("error")
    @Expose
    private Error_ error;

    public Error_ getError() {
        return error;
    }

    public void setError(Error_ error) {
        this.error = error;
    }
}
