package com.coolweather.app.model;

/**
 * Created by chenxiuxian on 2018/6/6.
 * 每个表在代码中有一个对应的实体类可以方便后续开发
 * 内容是生成数据库表对应字段的get和set方法
 */

public class City {
    private int id;
    private String cityName;
    private String cityCode;
    private int provinceId;
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getCityName(){
        return cityName;
    }
    public void setCityName(String cityName){
        this.cityName = cityName;
    }
    public String getCityCode(){
        return cityCode;
    }
    public void setCityCode(String cityCode){
        this.cityCode = cityCode;
    }
    public int getProvinceId(){
        return provinceId;
    }
    public void setProvinceId(int provinceId){
        this.provinceId = provinceId;
    }
}
