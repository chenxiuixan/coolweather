package com.coolweather.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolweather.app.R;

/**
 * DATE:2020/9/15 0015
 * TIME:上午 12:29
 * Author:chenxiuxian
 */
public class BitmapActivity extends Activity {
    private ImageView iv;
    private TextView tv;
    private Button left,right;
    private int times;
    private int angle;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        times = 1;
        angle = 1;
        iv = (ImageView) findViewById(R.id.iv);
        tv = (TextView) findViewById(R.id.tv);
        left = (Button) findViewById(R.id.left);
        left.setText("向左转");
        right = (Button) findViewById(R.id.right);
        right.setText("向右转");
        final Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.a); //自己引入一张图片a.png
        final int width = bmp.getWidth();
        final int height = bmp.getHeight();
        iv.setImageBitmap(bmp);
    }
}
