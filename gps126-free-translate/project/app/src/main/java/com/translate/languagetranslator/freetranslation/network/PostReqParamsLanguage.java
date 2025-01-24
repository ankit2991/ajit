package com.translate.languagetranslator.freetranslation.network;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PostReqParamsLanguage {

    String langaugeTo;
    String from;

  public   boolean notSaveHis;

   public boolean isReverseLanguage=false;

   public String sourceLanCode;
   public String destLanCode;



    String encodedTxt;
    boolean isAutoDetected=false;
    String apiKey0 = "trnsl.1.1.20181025T073441Z.23c0cd502f09db20.3d65718631c0034e435aa8241937e01f65bd3c96";


    String apiKey = "AIzaSyDBBjUaEoXXuEvb0S3JSaCo_qp07R9scV4";


    public boolean isAutoDetected() {
        return isAutoDetected;
    }

    public void setAutoDetected(boolean autoDetected) {
        isAutoDetected = autoDetected;
    }

    public PostReqParamsLanguage(String langaugeTo, String from, String Txt, boolean isAutoDetected,String sourceLanCode,String destLangcode) {

        this.langaugeTo = langaugeTo;
        this.from = from;

        this.apiKey = apiKey;

        this.encodedTxt=Txt;
        this.isAutoDetected=isAutoDetected;

        this.sourceLanCode=sourceLanCode;
        this.destLanCode=destLangcode;

    }



    public String getEncodedTxt() {
        return encodedTxt;
    }

    public void setEncodedTxt(String encodedTxt) {
        this.encodedTxt = encodedTxt;
    }



    public String getLangaugeTo() {
        return langaugeTo;
    }

    public void setLangaugeTo(String langaugeTo) {
        this.langaugeTo = langaugeTo;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }





    public String getURl(){
        //String urlStr = "https://www.googleapis.com/language/translate/v2?key=" + key + "&q=" + encodedText + "&target=" + to + "&source=" + from;
        String urlStr;

       // isAutoDetected=true;


        if(isAutoDetected()){
            urlStr = "https://www.googleapis.com/language/translate/v2?key=" + getApiKey() + "&q=" + getEncodedString(getEncodedTxt()) + "&target=" + getLangaugeTo();
        }else {
            urlStr = "https://www.googleapis.com/language/translate/v2?key=" + getApiKey() + "&q=" + getEncodedTxt() + "&target=" + getLangaugeTo()+ "&source=" + getFrom();
        }
        return urlStr;
    }





    public String getEncodedString(String textToTranslate) {
        String encodedText = "";
        try {
            encodedText = URLEncoder.encode(textToTranslate, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedText;
    }





}
