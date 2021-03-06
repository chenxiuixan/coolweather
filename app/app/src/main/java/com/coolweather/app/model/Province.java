package com.coolweather.app.model;

/**
 * Created by chenxiuxian on 2018/6/6.
 * 每个表在代码中有一个对应的实体类可以方便后续开发
 * 内容是生成数据库表对应字段的get和set方法
 */

public class Province {
    private int id;
    private String provinceName;
    private String provinceCode;
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getProvinceName(){
        return provinceName;
    }
    public void setProvinceName(String provinceName){
        this.provinceName = provinceName;
    }
    public String getProvinceCode(){
        return provinceCode;
    }
    public void setProvinceCode(String provinceCode){
        this.provinceCode = provinceCode;
    }
    public String toString() {
        return "Province{" +
                "id=" + id +
                ",provinceName='" + provinceName + '\'' +
                ", provinceCode=" + provinceCode +
                '}';
    }
}
