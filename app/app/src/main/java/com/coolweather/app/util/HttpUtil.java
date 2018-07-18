package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chenxiuxian on 2018/6/6.
 * 全国省市县的数据都是从服务端获取的，加入和服务器的交互
 */

public class HttpUtil {
    public static final String tag = "CXX";
    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null){
                        Logutil.i(tag,"server respond:" + line);
                        response.append(line);
                    }
                    if (listener != null){
                        //回调onFinish()方法
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e){
                    if (listener != null){
                        //onError()方法
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
