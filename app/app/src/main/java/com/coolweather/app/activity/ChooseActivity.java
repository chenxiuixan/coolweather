package com.coolweather.app.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Logutil;
import com.coolweather.app.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxiuxian on 2018/6/6.
 * 遍历省市县的activity
 */

public class ChooseActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<Country> countryList;//县列表
    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市
    private int currentLevel;//当前选中的级别

    private boolean isFromWeatherActivity;

    public static String tag = "CXX";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView)findViewById(R.id.list_view);
        titleText = (TextView)findViewById(R.id.title_text);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        Logutil.i(tag, "initial all args");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE){
                    Logutil.i(tag, "currenLevel is LEVEL_PROVINCE:" + String.valueOf(currentLevel));
                    selectedProvince = provinceList.get(i);
                    Logutil.i(tag, "selectedProvince is:" + selectedProvince.toString());
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    Logutil.i(tag, "currenLevel is LEVEL_CITY:" + selectedCity.toString());
                    queryCountries();//加载县级数据
                } else if (currentLevel == LEVEL_COUNTRY){
                    Logutil.i(tag, "currenLevel is LEVEL_COUNTRY:");
                    String countryCode = countryList.get(i).getCountryCode();
                    Logutil.i(tag, "countryCode is:" + countryCode);
                    Intent intent = new Intent(ChooseActivity.this, WeatherActivity.class);
                    intent.putExtra("country_code",countryCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库中查询，如果没有查到再到服务器上查询
     */
    private void queryProvinces(){
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0){
            dataList.clear();
            Logutil.i(tag, "provinceList.size:" + String.valueOf(provinceList.size()));
            for (Province province :provinceList){
                Logutil.i(tag, "provinceList has :" + province.getProvinceName());
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
            Logutil.i(tag, "currentLevel is LEVEL_PROVINCE:" + currentLevel);
        }else{
            queryFromServer(null,"province");
        }
    }
    /**
     * 查询选中的省，优先从数据库中查询，如果没有查到再到服务器上查询
     */
    private void queryCities(){
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0){
            Logutil.i(tag, "cityList.size:" + cityList.size());
            dataList.clear();
            for (City city :cityList){
                dataList.add(city.getCityName());
                Logutil.i(tag, "cityList has:" + city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            Logutil.i(tag, "current titleText is province:" + selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
            Logutil.i(tag, "currentLevel is LEVEL_CITY:" + currentLevel);
        }else{
            Logutil.i(tag, "queryCities from server");
            Logutil.i(tag, "selectedProvince code is:" + selectedProvince.getProvinceCode());
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    /**
     * 查询选中的县，优先从数据库中查询，如果没有查到再到服务器上查询
     */
    private void queryCountries(){
        countryList = coolWeatherDB.loadCountries(selectedCity.getId());
        if (countryList.size() > 0){
            dataList.clear();
            for (Country country :countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTRY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"country");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县的结果
     * @param code
     * @param type
     */
    private void queryFromServer(final String code,final String type){
        Logutil.i(tag,"into queryFromServer");
        String address;
        Logutil.i(tag,"queryFromServer code is:" + code);
        Logutil.i(tag,"queryFromServer type is:" + type);
        if (!TextUtils.isEmpty(code)){
            Logutil.i(tag,"query province's cities");
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
            Logutil.i(tag,"address is:" + address);
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgreeDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("country".equals(type)){
                    result = Utility.handleCountriesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if (result){
                    //通过runOnUiThread()方法回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Logutil.i(tag,"HttpUtil error:" + e.toString());
                //通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进队对话框
     */
    private void showProgreeDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获mback，根据当前级别来判断，此时要返回市列表、省列表还是直接退出
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (currentLevel == LEVEL_COUNTRY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            if (isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
