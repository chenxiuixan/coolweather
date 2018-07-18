package com.coolweather.app.util;

/**
 * Created by chenxiuxian on 2018/6/6.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
