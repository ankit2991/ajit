package com.translate.languagetranslator.freetranslation.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.translate.languagetranslator.freetranslation.phrasebook.phrasebookTranslation.ItemModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

object AssetsRepo {


    fun getJsonDataFromAsset(context: Context, fileName: String="DataModel.json",category : String,lang: String, subHeading: String): ArrayList<ItemModel>? {
        var jsonString: String = ""
        var jsonObj :JSONObject=JSONObject()
        val selectedCategoryList: ArrayList<ItemModel> = ArrayList()
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText()
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        jsonObj = JSONObject(jsonString)


//        var jsonArraylist = Gson().fromJson<ArrayList<ItemModel>>(jsonObj.getJSONObject(category).getJSONArray("data").toString(),object :TypeToken<ArrayList<ItemModel>>(){}.type)
//        selectedCategoryList.addAll(Gson().fromJson<ArrayList<ItemModel>>(jsonObj.getJSONObject(category).getJSONArray("data").toString(),object :TypeToken<ArrayList<ItemModel>>(){}.type))

//        Log.e("jsondata","data"+jsonObj.getJSONObject(category).getJSONArray("data").toString())
//        Log.e("ArraySize===>",selectedCategoryList.toString())

        //Work start  here
        val itemModelJsonArray = JSONArray(jsonObj.getJSONObject(category).getJSONArray("data").toString()) // Extension Function call here

        for (i in 0 until itemModelJsonArray.length()){
            val itemModel = ItemModel()
            val itemModelJSONObject = itemModelJsonArray.getJSONObject(i)
//            itemModel.heading = itemModelJSONObject.getString("heading")
            itemModel.heading = itemModelJSONObject.getJSONObject("heading").getString(subHeading)
//            itemModel.subheading =itemModelJSONObject.get("subheading")

            val jArray = itemModelJSONObject.getJSONArray(subHeading) as JSONArray?
            if (jArray != null) {
                for (j in 0 until jArray.length()) {
                    itemModel.subheading.add(jArray.getString(j))
                }
            }

            val langArray = itemModelJSONObject.getJSONArray(lang) as JSONArray?
            if (langArray != null) {
                for (l in 0 until langArray.length()) {
                    itemModel.translated.add(langArray.getString(l))

                }
            }


//            facilityModel.Province = facilityJSONObject.getString("Province")
//            facilityModel.Subdistrict = facilityJSONObject.getString("Facility")
//            facilityModel.code = facilityJSONObject.getInt("code")
//            facilityModel.gps_latitude = facilityJSONObject.getDouble("gps_latitude")
//            facilityModel.gps_longitude = facilityJSONObject.getDouble("gps_longitude")

            selectedCategoryList.add(itemModel)
        }

        return  selectedCategoryList
    }

    public fun loadAssets(context: Context,filePath:String):InputStream{
      return context.assets.open(filePath)
        // load image as Drawable
    }


}