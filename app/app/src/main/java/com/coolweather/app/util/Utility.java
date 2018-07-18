package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by chenxiuxian on 2018/6/6.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public static String tag = "CXX";
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
        if (!TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0){
                for (String p: allProvinces){
                    String [] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到Province表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的市级数据
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0){
                for (String c: allCities){
                    String [] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存储到City表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的县级数据
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleCountriesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCountries = response.split(",");
            if (allCountries != null && allCountries.length > 0){
                for (String c: allCountries){
                    String [] array = c.split("\\|");
                    Country country = new Country();
                    country.setCountryCode(array[0]);
                    country.setCountryName(array[1]);
                    country.setCityId(cityId);
                    coolWeatherDB.saveCountry(country);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析出的数据存储到本地
     */
    public static void handleWeatherResponse(Context context, String response){
        try{
            Logutil.i(tag, "handleWeatherResponse begin");
            JSONObject jsonObject = new JSONObject(response);
            Logutil.i(tag,"get JSONObject");
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            Logutil.i(tag,"get weatherinfo");
            String cityName = weatherInfo.getString("city");
            Logutil.i(tag, "handleWeatherResponse get cityName:" + cityName);
            String weatherCode = weatherInfo.getString("cityid");
            Logutil.i(tag, "handleWeatherResponse get weatherCode:" + weatherCode);
            String temp1 = weatherInfo.getString("temp1");
            Logutil.i(tag, "handleWeatherResponse get temp1:" + temp1);
            String temp2 = weatherInfo.getString("temp2");
            Logutil.i(tag, "handleWeatherResponse get temp2:" + temp2);
            String weatherDesp = weatherInfo.getString("weather");
            Logutil.i(tag, "handleWeatherResponse get weatherDesp:" + weatherDesp);
            String publishTime = weatherInfo.getString("ptime");
            Logutil.i(tag, "handleWeatherResponse get publishTime:" + publishTime);
            saveWeatherInfo(context, cityName, weatherCode,temp1, temp2, weatherDesp, publishTime);

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        Logutil.i(tag, "saveWeatherInfo");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name",cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp",weatherDesp);
        editor.putString("publish_time",publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }
}
