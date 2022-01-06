package com.kenyantvlive.k24tv;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DisclaimerActivity extends AppCompatActivity {
    TextView tvDisclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);
        tvDisclaimer= findViewById(R.id.tvDisclaimer);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        tvDisclaimer.setText("This app (K24 Tv) is NOT endorsed, produced by or affiliated with any of the featured Kenyan Television Station. \n\n" +
                "All trademarks and copyrights of the content are properties of their respective owners.\n\n" +
                "Please do support the featured tv stations by downloading, installing and using their respective official applications.\n\n" +
                "Should you be the owner or an authorized legal representative of the owner of any of the content in the app and not happy about it, \n\n" +
                "please do inform us by sending us an email smoothradioapp@gmail.com and we will gladly remove the content which you are not happy with.");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}