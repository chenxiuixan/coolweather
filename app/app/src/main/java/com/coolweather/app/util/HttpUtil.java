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
            public void run() {//一般使用开启线程来发起网络请求
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();//获取HttpURLConnection实例
                    connection.setRequestMethod("GET");//设置发送Http请求的方法
                    connection.setConnectTimeout(8000);//设置连接超时的豪秒数
                    connection.setReadTimeout(8000);//设置读取超时的豪秒数
                    InputStream in = connection.getInputStream();//使用getInputStream来获取服务器返回的输入流
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
                        connection.disconnect();//将http连接关掉
                    }
                }
            }
        }).start();
    }
}
