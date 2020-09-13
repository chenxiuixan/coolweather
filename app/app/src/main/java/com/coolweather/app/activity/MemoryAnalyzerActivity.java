package com.coolweather.app.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * DATE:2020/9/8 0013
 * TIME:下午 9:01
 * Author:chenxiuxian
 */
public class MemoryAnalyzerActivity extends Activity implements View.OnClickListener{
    // 1静态变量内存泄露:textView 会持有 Activity 的引用，而静态 view 的生命周期和类是一样长 ，这样导致 Activity 不能被回收。
    private static Context context;
    private static TextView tv;
    // 2未关闭资源对象内存泄露--2.1没有注销广播导致的内存泄漏
    // 2.1动态方式注册可以监听网络变化的广播
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    // 2.2没有关闭输入输出流
    private EditText editText;
    // 2.3没有释放bitmap
    private ImageView imageView;

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
        // 2.3没有释放bitmap
        imageView = (ImageView)findViewById(R.id.imageView);
        Button button_load = (Button)findViewById(R.id.button_load);
        button_load.setOnClickListener(this);
    }

    class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            Toast.makeText(context, "network changes", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(networkChangeReceiver);// 没有注销广播导致的内存泄漏
        String inputText = editText.getText().toString();
        save(inputText);
        Log.d("MemoryAnalyzerActivity","输入的text是:" + inputText);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_load:
                InputStream inputStream = null;
                Bitmap bitmap = null;
                try {
                    inputStream = getUrlInputStream("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1600020847033&di=8a0c1e0eeadfc532dd3fdbe8bc4bd814&imgtype=0&src=http%3A%2F%2Fgss0.baidu.com%2F-4o3dSag_xI4khGko9WTAnF6hhy%2Fzhidao%2Fpic%2Fitem%2Fac4bd11373f082028ec4b1404dfbfbedab641b7e.jpg");
                    bitmap = getBitmap(inputStream);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void save(String inputText){
        FileOutputStream out;
        BufferedWriter writer;
        try{
            out = openFileOutput("data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(inputText);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // 根据网络URL获取输入流
    public InputStream getUrlInputStream(String strUrl) throws IOException {
        URL url = new URL(strUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream inputStream = conn.getInputStream();
        if (inputStream != null) {
            return inputStream;
        } else {
            Log.i("inputStream", "输入流对象为空");
            return null;
        }
    }

    // 将输入流转化为Bitmap流
    public Bitmap getBitmap(InputStream inputStream) {
        Bitmap bitmap = null;
        if (inputStream != null) {
            bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } else {
            Log.i("test", "输入流对象in为空");
            return null;
        }
    }
}
