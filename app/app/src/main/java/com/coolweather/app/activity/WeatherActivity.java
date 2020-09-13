package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Logutil;
import com.coolweather.app.util.Utility;

/**
 * Created by chenxiuxian on 2018/6/13.
 */

public class WeatherActivity extends Activity implements View.OnClickListener{

    public static String tag = "CXX";
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;//用于城市名称
    private TextView publishText;//用于显示发布时间
    private TextView weatherDespText;//用于显示天气描述信息
    private TextView temp1Text;//用于显示气温1
    private TextView temp2Text;//用于显示气温2
    private TextView currentDateText;//用于显示当前日期

    private Button switchCity; //切换城市按钮
    private Button refreshWeather;//更新天气按钮
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView)findViewById(R.id.city_name);
        publishText = (TextView)findViewById(R.id.publish_text);
        weatherDespText = (TextView)findViewById(R.id.weather_desp);
        temp1Text = (TextView)findViewById(R.id.temp1);
        temp2Text = (TextView)findViewById(R.id.temp2);
        currentDateText = (TextView)findViewById(R.id.currend_date);

        //从intent中获取县级代号，如果可以取到就去调用queryWeatherCode
        String countryCode = getIntent().getStringExtra("country_code");
        Logutil.i(tag, "WeatherActivity get country_code:" + countryCode);
        if (!TextUtils.isEmpty(countryCode)){
            //有县级代号时就去查询天气
            publishText.setText("同步中....");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);
        }else{
            //没有就直接显示本地天气
            showWeather();
        }

        switchCity = (Button)findViewById(R.id.switch_city);
        refreshWeather = (Button)findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this,ChooseActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);//使用PreferenceManager获取SharedPreferences对象，参数是Context参数
                String weatherCode = prefs.getString("weather_code","");//使用get方法来读取数据，2个参数，1是键，2是默认值，如果传入的键找不到对应的值时返回默认值
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 查询县级代号所对应的天气代号
     * @param countryCode
     */
    private void queryWeatherCode(String countryCode) {
        Logutil.i(tag, "queryWeatherCode:" + countryCode);
        String address = "http://www.weather.com.cn/data/list3/city" + countryCode + ".xml";
        queryFromServer(address,"countryCode");
    }

    private void queryWeatherInfo(String weatherCode){
        Logutil.i(tag, "queryWeatherInfo:" + weatherCode);
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address,"weatherCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     * @param address
     * @param type
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                if ("countryCode".equals(type)){
                    if (!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代号
                        String [] array = response.split("\\|");
                        if (array != null && array.length ==2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if ("weatherCode".equals(type)){
                 //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);//使用PreferenceManager获取SharedPreferences对象，参数是Context参数
        cityNameText.setText(prefs.getString("city_name",""));//使用get方法来读取数据，2个参数，1是键，2是默认值，如果传入的键找不到对应的值时返回默认值
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天" + prefs.getString("publish_time","") + "发布");
        currentDateText.setText(prefs.getString("current_date",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


}
