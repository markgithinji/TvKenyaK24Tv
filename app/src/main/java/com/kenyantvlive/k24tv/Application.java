package com.kenyantvlive.k24tv;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class Application extends android.app.Application {
    public static MainActivity mainActivity;
    public static boolean isShowingAds;
    public static InterstitialAd interstitialAd;
    @Override
    public void onCreate() {
        super.onCreate();
        MobileAds.initialize(this);
        //interstitial Ad

        AdRequest interstitialAdRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-9799428944156340/7449378550", interstitialAdRequest, new InterstitialAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                Application.this.interstitialAd=interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Application.this.interstitialAd=null;
            }
        });
    }
}
