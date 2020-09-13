package com.coolweather.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * DATE:2020/9/13 0013
 * TIME:下午 10:10
 * Author:chenxiuxian
 */
public class MemoryAnalyzerCorrectActivity extends Activity {
    // 1静态变量内存泄露:textView 会持有 Activity 的引用，而静态 view 的生命周期和类是一样长 ，这样导致 Activity 不能被回收。
    private Context context;
    private TextView tv;
    // 2未关闭资源对象内存泄露--2.1没有注销广播导致的内存泄漏
    // 2.1动态方式注册可以监听网络变化的广播
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    // 2.2没有关闭输入输出流
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1静态变量内存泄露
        context = this;
        tv = (TextView)findViewById(R.id.tv);
        // 2.1没有注销广播导致的内存泄漏
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
        // 2.2没有关闭输入输出流
        editText = (EditText)findViewById(R.id.editText);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            Toast.makeText(context, "network changes", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
        String inputText = editText.getText().toString();
        save(inputText);
        Log.d("MemoryAnalyzerActivity","输入的text是:" + inputText);
    }

    public void save(String inputText){
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try{
            out = openFileOutput("data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (writer != null){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
