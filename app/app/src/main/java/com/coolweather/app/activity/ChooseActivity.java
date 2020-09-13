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
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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

    /*
    * 先判断是否从WeatherActivity返回来，即从具体城市天气展示界面返回来
    * 在判断是否选中了某个城市
    * 如果没有选中某个
    * */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);//使用PreferenceManager获取SharedPreferences对象，参数是Context参数
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){//使用get方法来读取数据，2个参数，1是键，2是默认值，如果传入的键找不到对应的值时返回默认值
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        /*初始化控件的实例，初始化ArrayAdapter，设置为ListView的适配器，获取CoolWeatherDB的实例，给ListView设置点击事件，
        最后调用queryProvinces()，从这里开始加载省级数据*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView)findViewById(R.id.list_view);
        titleText = (TextView)findViewById(R.id.title_text);
        /*ListView是用于展示大量数据的，数组中的数据无法直接传递给ListView，需要借助适配器。
        * ArrayAdapter可以通过泛型指定要适配的数据类型，如：private ArrayAdapter<String> adapter指定数据类型为String
        * 在构造函数中将要适配的数据传入即可，如adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataList);
        * 构造函数中依次传入当前上下文、ListView子项布局、要适配的数据
        * android.R.layout.simple_list_item_1是android内置的布局文件，里面只有一个TextView，可以简单显示一段文本
        * 调用ListView的setAdapter()方法将构建好的适配器传递进去*/
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        Logutil.i(tag, "initial all args");
        //为ListView注册一个监听器，当点击ListView中的任何一个子项时会回调onItemClick方法，这个方法可以通过position参数判断用户点击的是哪一个子项
        //onItemClick(AdapterView<?> parent, View view,int position, long id)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE){
                    //打印出来的log是：currenLevel is LEVEL_PROVINCE:0
                    Logutil.i(tag, "currenLevel is LEVEL_PROVINCE:" + String.valueOf(currentLevel));
                    selectedProvince = provinceList.get(i);
                    //打印出来的log是：selectedProvince is:Province{id=34,provinceName='台湾', provinceCode=34}
                    Logutil.i(tag, "selectedProvince is:" + selectedProvince.toString());
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    //打印出来的log是：selectedCity is:com.coolweather.app.model.City@40a70a7
                    Logutil.i(tag, "selectedCity is:" + selectedCity.toString());
                    queryCountries();//加载县级数据
                } else if (currentLevel == LEVEL_COUNTRY){
                    Logutil.i(tag, "currenLevel is LEVEL_COUNTRY:" + String.valueOf(currentLevel));
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
     * 首先使用CoolWeatherDB的loadProvinces()从数据库中读取省级数据
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
            //即queryFromServer(selectedProvince.getProvinceCode(),"city")方法或queryFromServer(selectedCity.getCityCode(),"country")方法
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
            Logutil.i(tag,"address is:" + address);
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";//如果传入code为空，即queryFromServer(null,"province")方法
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
