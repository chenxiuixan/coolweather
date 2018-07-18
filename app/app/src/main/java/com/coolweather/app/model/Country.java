package com.coolweather.app.model;

/**
 * Created by chenxiuxian on 2018/6/6.
 * 每个表在代码中有一个对应的实体类可以方便后续开发
 */

public class Country {
    private int id;
    private String countryName;
    private String countryCode;
    private int cityId;
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getCountryName(){
        return countryName;
    }
    public void setCountryName(String countryName){
        this.countryName = countryName;
    }
    public String getCountryCode(){
        return countryCode;
    }
    public void setCountryCode(String countryCode){
        this.countryCode = countryCode;
    }
    public int getCityId(){
        return cityId;
    }
    public void setCityId(int cityId){
        this.cityId = cityId;
    }
}
