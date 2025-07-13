package com.kenyantvlive.k24tv;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class WebViewActivity extends AppCompatActivity {
    //UI
    WebView webView;
    ChromeClient webChromeClient;
    View mCustomView;
    FrameLayout customViewContainer;
    WebChromeClient.CustomViewCallback customViewCallback;
    WebViewClient webViewClient;
    String link;
    String name;
    ProgressDialog progressDialog;
    TextView tvStationName;
    //Ad
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);


        webView = findViewById(R.id.webView);
        tvStationName= findViewById(R.id.tvStationName);
        mAdView= findViewById(R.id.adView);
        customViewContainer = findViewById(R.id.customViewContainer);

        link= getIntent().getStringExtra("link");
        name=getIntent().getStringExtra("name");

        progressDialog =new ProgressDialog(this);
        progressDialog.setMessage("Preparing "+name);
        progressDialog.setCancelable(true);
        progressDialog.show();

        //BannerAd
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setStatusColor();

        webChromeClient = new ChromeClient();
        webViewClient = new WebViewClient();

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setWebChromeClient(webChromeClient);
        //webView.getSettings().setAppCacheEnabled(true);
        webView.setWebViewClient(webViewClient);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        webView.loadUrl(link);
        webView.requestFocus();

        tvStationName.setText(name);

        getWindow().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
//        webView.pauseTimers();//Affects Ads
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.resumeTimers();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.clearCache(true);
        webView.destroy();
        webView=null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }
        if (mCustomView != null) {
            webChromeClient.onHideCustomView();
        }
        else
        {
            finish();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
        if (mCustomView != null) {
            webChromeClient.onHideCustomView();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class ChromeClient extends WebChromeClient {

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public void onShowCustomView(View view,CustomViewCallback callback) {
            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            webView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;
            removeStatusNavigation();
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();    //To change body of overridden methods use File | Settings | File Templates.
            showStatusNavigation();
            if (mCustomView == null)
                return;

            webView.setVisibility(View.VISIBLE);
            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            mCustomView = null;
        }
    }

    class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressDialog.dismiss();
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //handler.proceed(); // Ignore SSL certificate errors
            handler.cancel();
        }

    }
    void removeStatusNavigation()
    {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
    void showStatusNavigation()
    {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE);
    }
    void setStatusColor()
    {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.black));
    }
}