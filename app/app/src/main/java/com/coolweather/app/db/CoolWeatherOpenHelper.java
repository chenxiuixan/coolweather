package com.coolweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chenxiuxian on 2018/6/6.
 * 把三条建表语句定义成常量，然后在onCreate()中去执行建表
 * 每张表在代码中最好能有一个对应的实体类，这样会非常方便后续的开发工作，这些类放在model的包下
 */
/*SQLiteOpenHelper是一个抽象类，使用时需要自己创建一个帮助类来继承它，重写onCreate()和onUpgrade()方法*/
public class CoolWeatherOpenHelper extends SQLiteOpenHelper {
    //Province表建表语句
    public static final String CREATE_PROVINCE = "create table Province(id integer primary key autoincrement,province_name text,province_code text)";
    //City表建表语句
    public static final String CREATE_CITY = "create table City(id integer primary key autoincrement,city_name text,city_code text,province_id integer)";
    //Country表建表语句
    public static final String CREATE_COUNTRY = "create table Country(id integer primary key autoincrement,country_name text,country_code text,city_id integer)";

    public static final String test = "create table Province("
            + "id integer primary key autoincrement,"
            + "province_name text,"
            + "province_code tet)";
    //使用参数少的构造方法，1是Context，2是数据库名，3是允许查询数据库的时候返回一个自定义的Cursor，一般都传入null，4是当前数据库版本号，可用于对数据库进行升级操作
    //onCreate方法中执行创建表的动作
    public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PROVINCE);//创建Province表
        sqLiteDatabase.execSQL(CREATE_CITY);//创建City表
        sqLiteDatabase.execSQL(CREATE_COUNTRY);//创建Country表
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
