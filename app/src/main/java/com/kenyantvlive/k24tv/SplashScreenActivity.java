package com.kenyantvlive.k24tv;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
    Intent intent ;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        textView= findViewById(R.id.textView);
        textView.setAnimation(AnimationUtils.loadAnimation(SplashScreenActivity.this,R.anim.splashscreen_feelgoodvibes_animation));
        new Thread(new Runnable() {
            @Override
            public void run() {
                intent = new Intent(SplashScreenActivity.this,MainActivity.class);
                SystemClock.sleep(5000);
                startActivity(intent);
                finish();
            }
        }).start();

    }
}