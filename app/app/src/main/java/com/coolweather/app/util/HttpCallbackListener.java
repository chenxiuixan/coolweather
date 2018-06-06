package com.coolweather.app.util;

/**
 * Created by chenxiuxian on 2018/6/6.
 */

public interface HttpCallbackListener {
    void onFinish(String respond);
    void onError(Exception e);
}
