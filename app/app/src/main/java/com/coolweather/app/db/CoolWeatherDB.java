package com.coolweather.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxiuxian on 2018/6/6.
 * 封装常用的数据库操作
 * CoolWeatherDB是一个单例类，把构造方法私有化，提供一个getInstance()方法来获取它的实例，保证全局范围内只有一个实例。
 * 提供六组方法：存储、读取省、城市、县的数据
 */

public class CoolWeatherDB {
    //数据库名称
    public static final String DB_NAME = "cool_weather";
    //数据库版本号
    public static final int VERSION = 1;
    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;
    //将构造方法私有化
    //每次调用coolWeatherDB这个对象时都会先执行这个构造函数，构造函数中初始化帮助类，第一次调用时构造函数调用CoolWeatherOpenHelper创建一个数据库cool_weather名称的数据库，并且调用
    //CoolWeatherOpenHelper的onCreate()方法来创建Province表、City表、Country表
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db = dbHelper.getWritableDatabase();//打开一个现有的数据库，如果数据不存在则创建一个
    }
    //获取CoolWeatherDB实例
    public synchronized static CoolWeatherDB getInstance(Context context){
        if (coolWeatherDB == null){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }
    /**
     * 把Province实例存储到数据库
    */
    public void saveProvince(Province province){
        if (province != null){
            ContentValues values = new ContentValues();
            values.put("province_name",province.getProvinceName());
            values.put("province_code",province.getProvinceCode());
            db.insert("Province",null,values);
        }
    }
    /**
     * 从数据库读取全国所有的省份信息
     */
    public List<Province> loadProvinces(){
        List<Province> list = new ArrayList<>();
        /*调用SQLiteDatabase的query方法来查询数据。只是用第一个参数指明去查询Province表，表示查询这张表中的所有数据
        * cursor.moveToFirst()将数据指针移动到第一行的位置，然年后进入循环去遍历每一行的数据
        * cursor.getColumnIndex()方法获取某一列在表中对应的位置索引
        * 最后要关闭*/
        Cursor cursor = db.query("Province",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while (cursor.moveToNext());
            if (cursor != null){
                cursor.close();
            }
        }
        return list;
    }

    /**
     * @param city
     * 把City实例存储到数据库
     */
    public void saveCity(City city){
        if(city != null){
            ContentValues values = new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getProvinceId());
            db.insert("City",null,values);
        }
    }

    /**
     * @param provinceId
     * @return
     * 从数据库中读取某省下所有城市信息
     */
    public List<City> loadCities(int provinceId){
        /*调用SQLiteDatabase的query方法来查询数据。第一个参数指明去查询City表，约束条件是province_id等于传入的provinceId
        * cursor.moveToFirst()将数据指针移动到第一行的位置，然年后进入循环去遍历每一行的数据
        * cursor.getColumnIndex()方法获取某一列在表中对应的位置索引
        * 最后要关闭*/
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("City",null,"province_id = ?",new String[]{String.valueOf(provinceId)},null,null,null);
        if (cursor.moveToFirst()){
            do{
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            }while (cursor.moveToNext());
            if (cursor != null){
                cursor.close();
            }
        }
        return list;
    }
    /**
     * @param country
     * 将Country实例存储到数据库
     */
    public void saveCountry(Country country){
        if(country != null){
            ContentValues values = new ContentValues();
            values.put("country_name",country.getCountryName());
            values.put("country_code",country.getCountryCode());
            values.put("city_id",country.getCityId());
            db.insert("Country",null,values);
        }
    }
    /**
     * @param cityId
     * @return
     * 从数据库中读取某城市下所有县城信息
     */
    public List<Country> loadCountries(int cityId){
        List<Country> list = new ArrayList<>();
        /*调用SQLiteDatabase的query方法来查询数据。第一个参数指明去查询City表，约束条件是province_id等于传入的provinceId
        * cursor.moveToFirst()将数据指针移动到第一行的位置，然年后进入循环去遍历每一行的数据
        * cursor.getColumnIndex()方法获取某一列在表中对应的位置索引
        * 最后要关闭*/
        Cursor cursor = db.query("Country",null,"city_id = ?",new String[]{String.valueOf(cityId)},null,null,null);
        if (cursor.moveToFirst()){
            do{
                Country country = new Country();
                country.setId(cursor.getInt(cursor.getColumnIndex("id")));
                country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
                country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
                country.setCityId(cityId);
                list.add(country);
            }while (cursor.moveToNext());
            if (cursor != null){
                cursor.close();
            }
        }
        return list;
    }
}
