package com.kenyantvlive.k24tv;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor prefsEditor;
    Toolbar toolbar;
    //Firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListenerRegistration listenerRegistration;
    public static ArrayList<String> linksFromTxt = new ArrayList<>();
    public static ArrayList<String> linksAfterUpdate = new ArrayList<>();
    public static ArrayList<String> typesFromTxt = new ArrayList<>();
    public static ArrayList<String> typesAfterUpdate = new ArrayList<>();
    //Ad
    String playAction ="playK24Tv";
    Intent playIntent = new Intent().setAction(playAction);
    //Firebase Analytics
    FirebaseAnalytics firebaseAnalytics;
    //UI
    ImageView ivWatchLive;
    ImageView ivReportProblem;
    ImageView ivShareApp;
    ImageView ivFollowUs;
    ImageView ivAllKenyanChannels;
    ImageView ivDisclaimer;
    AdView bannerAd;

    public static boolean isShowingAds;

    public InterstitialAd interstitialAd;
    //Consent form
    private ConsentInformation consentInformation;
    // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showConsentForm();

        //SharedPrefs
        sharedPreferences= getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
        prefsEditor= sharedPreferences.edit();
        if(sharedPreferences.getBoolean("isFirstTime",true))
        {
            createInitialTxt();
            createInitialTypesTxt();
            prefsEditor.putBoolean("isFirstTime",false);
            prefsEditor.apply();
        }

        bannerAd = findViewById(R.id.mainBannerAd);
        toolbar = findViewById(R.id.toolbar);
        ivWatchLive = findViewById(R.id.ivWatchLive);
        ivReportProblem = findViewById(R.id.ivReportProblem);
        ivShareApp = findViewById(R.id.ivShareApp);
        ivDisclaimer = findViewById(R.id.ivDisclaimer);
        ivFollowUs = findViewById(R.id.ivFollowUs);
        ivAllKenyanChannels = findViewById(R.id.ivAllKenyanradio);

        setSupportActionBar(toolbar);
        //BannerAd
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(adRequest);

        linksTxtToArrayList();
        typesTxtToArrayList();

        setOnclickListeners();

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    private void setOnclickListeners() {
        ivWatchLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartPlay(linksFromTxt.get(0),typesFromTxt.get(0),"K24 Tv");
            }
        });
        ivReportProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appVersion="App Version: ";
                try {
                    appVersion+= getApplicationInfo().loadLabel(getPackageManager()).toString();
                    appVersion+= getPackageManager().getPackageInfo(getPackageName(),0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String deviceName="Device Name: "+ Build.MANUFACTURER+" "+Build.MODEL;
                String androidVersion = "Android Version: "+Build.VERSION.RELEASE;

                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse("mailto:"));
                mailIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{"smoothradioapp@gmail.com"});
                mailIntent.putExtra(Intent.EXTRA_TEXT,appVersion+"\n"+deviceName+"\n"+androidVersion+"\n"+"\n");
                mailIntent.putExtra(Intent.EXTRA_SUBJECT,"K24 TV APP FEEDBACK");

                try {
                    if(mailIntent.resolveActivity(getPackageManager())!=null){
                        startActivity(Intent.createChooser(mailIntent,"Send Mail..."));
                    }
                }
                catch (ActivityNotFoundException e)
                {
                    Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ivAllKenyanChannels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.smoothradio.radio"));
                startActivity(intent);
            }
        });
        ivDisclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent disclaimerIntent = new Intent(MainActivity.this,DisclaimerActivity.class);
                startActivity(disclaimerIntent);
            }
        });
        ivFollowUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent fbIntent = new Intent(Intent.ACTION_VIEW);
                fbIntent.setData(Uri.parse("https://web.facebook.com/Smooth-Radio-App-Kenya-102378815380103"));
                startActivity(fbIntent);
            }
        });
        ivShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.kenyatvchannelslive.tvkenya"));
                startActivity(intent);
            }
        });
    }

    void loadAd()
    {
        AdRequest interstitialAdRequest = new AdRequest.Builder().build();
        InterstitialAd.load(MainActivity.this, "ca-app-pub-9799428944156340/7449378550", interstitialAdRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                MainActivity.this.interstitialAd=interstitialAd;
                MainActivity.this.interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        isShowingAds=true;
                        MainActivity.this.interstitialAd=null;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        isShowingAds=false;
                        prefsEditor.putString("adLastShownTime",new SimpleDateFormat("yyyy.MM.dd hh:mm:ss aa", Locale.getDefault()).format(Calendar.getInstance().getTime()));
                        prefsEditor.commit();

                        sendBroadcast(playIntent);
                    }
                });
            }
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                MainActivity.this.interstitialAd=null;
                MainActivity.this.isShowingAds=false;
            }
        });
    }
    public void showAd()
    {
        loadAd();
        Boolean show = shouldShowAd();
        if (MainActivity.this.interstitialAd != null) {
            if(show)
            {
                MainActivity.this.interstitialAd.show(MainActivity.this);
                isShowingAds=true;
            }
        }
        else
        {
            MainActivity.this.interstitialAd=null;
            MainActivity.this.isShowingAds=false;
        }
    }

    Boolean shouldShowAd()
    {
        final int waitTime = 1;
        long numOfMinutes=waitTime;
        try {
            String CurrentTime= sharedPreferences.getString("adLastShownTime","0");

            String FinalDate= new SimpleDateFormat("yyyy.MM.dd hh:mm:ss aa", Locale.getDefault()).format(Calendar.getInstance().getTime());
            Date date1;
            Date date2;
            SimpleDateFormat dates = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss aa",Locale.getDefault());
            date1 = dates.parse(CurrentTime);
            date2 = dates.parse(FinalDate);
            long difference = Math.abs(date1.getTime() - date2.getTime());
            numOfMinutes = TimeUnit.MILLISECONDS.toMinutes(difference);
            //differenceInHours= TimeUnit.MILLISECONDS.toHours(difference)%60;
        } catch (Exception exception) {
            //Toast.makeText(MainActivity.this, "Unable to find difference", Toast.LENGTH_SHORT).show();
        }
        return numOfMinutes >= waitTime;
    }

    void StartPlay(String link,String type,String name)
    {
        checkInternet();
        Intent intent;
        if(type.equals("n"))
        {
            intent = new Intent(MainActivity.this, StreamActivity.class);
        }
        else
        {
            intent = new Intent(MainActivity.this, WebViewActivity.class);
        }
        intent.putExtra("link",link);
        intent.putExtra("name",name);
        startActivity(intent);

        showAd();
        sendFirebaseAnalytics();
    }
    void sendFirebaseAnalytics()
    {
        String event= "watch_event";
        Bundle bundle = new Bundle();
        bundle.putString("station_name", "k24tv");
        firebaseAnalytics.logEvent(event, bundle);///////////////////////////////////////////////////////////////////////////////
    }
    void checkInternet()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
        if(!connected)
        {
            Toast.makeText(MainActivity.this, "Check Internet", Toast.LENGTH_SHORT).show();
        }
    }


    void linksTxtToArrayList()
    {
        try{
            // Toast.makeText(MainActivity.this, "reading", Toast.LENGTH_SHORT).show();
            linksFromTxt.clear();
            File file = new File(getFilesDir(), "file.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedReader br = new BufferedReader(reader);
            for (int index = 0; index <1; index++) {
                linksFromTxt.add(br.readLine());
            }
            //Toast.makeText(MainActivity.this, "file read succeful", Toast.LENGTH_SHORT).show();
        }

        catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(MainActivity.this, "ioexcept", Toast.LENGTH_SHORT).show();
        }
    }
    void typesTxtToArrayList()
    {
        try{
            // Toast.makeText(MainActivity.this, "reading", Toast.LENGTH_SHORT).show();
            typesFromTxt.clear();
            File file = new File(getFilesDir(), "type.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedReader br = new BufferedReader(reader);
            for (int index = 0; index <1; index++) {
                typesFromTxt.add(br.readLine());
            }
            //Toast.makeText(MainActivity.this, "file read succeful", Toast.LENGTH_SHORT).show();
        }

        catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(MainActivity.this, "ioexcept", Toast.LENGTH_SHORT).show();
        }
    }

    void linksTxtToArrayListAfterUpdate()
    {
        try{
            // Toast.makeText(MainActivity.this, "reading", Toast.LENGTH_SHORT).show();
            linksAfterUpdate.clear();
            File file = new File(getFilesDir(), "file.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedReader br = new BufferedReader(reader);
            for (int index = 0; index <1; index++) {
                linksAfterUpdate.add(br.readLine());
            }
            //Toast.makeText(MainActivity.this, "file read succeful", Toast.LENGTH_SHORT).show();
        }

        catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(MainActivity.this, "ioexcept", Toast.LENGTH_SHORT).show();
        }
    }
    void typesTxtToArrayListAfterUpdate()
    {
        try{
            // Toast.makeText(MainActivity.this, "reading", Toast.LENGTH_SHORT).show();
            typesAfterUpdate.clear();
            File file = new File(getFilesDir(), "type.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedReader br = new BufferedReader(reader);
            for (int index = 0; index <1; index++) {
                typesAfterUpdate.add(br.readLine());
            }
            //Toast.makeText(MainActivity.this, "file read succeful", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(MainActivity.this, "ioexcept", Toast.LENGTH_SHORT).show();
        }
    }

    void createInitialTxt()
    {
        File file = new File(getFilesDir(),"file.txt");
        BufferedWriter writer= null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.write("https://5f864cb734bf0.streamlock.net/k24edge/myStream/playlist.m3u8");//0
            writer.newLine();

//            //Toast.makeText(this, "initial list used", Toast.LENGTH_SHORT).show();
        }

        catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(), "no write", Toast.LENGTH_LONG).show();
        }
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void createInitialTypesTxt()
    {
        File file = new File(getFilesDir(),"type.txt");
        BufferedWriter writer= null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.write("n");//0
            writer.newLine();
        }

        catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(), "no write", Toast.LENGTH_LONG).show();
        }
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(listenerRegistration!=null)
        {listenerRegistration.remove();}
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                listenerRegistration = db.collection("station").orderBy("index").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {

                        if (error != null) {
                            //Toast.makeText(MainActivity.this, "Error updating from server", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null&& !value.isEmpty()) {
                            File links = new File(getFilesDir(),"file.txt");
                            File types = new File(getFilesDir(),"type.txt");
                            BufferedWriter linksWriter= null;
                            BufferedWriter typesWriter= null;

                            try {
                                linksWriter = new BufferedWriter(new FileWriter(links));
                                typesWriter = new BufferedWriter(new FileWriter(types));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            for (DocumentSnapshot document : value.getDocuments()) {
                                try {
                                    String link=document.getString("link");
                                    String type=document.getString("type");
                                    if(link==null)
                                    {
                                        linksWriter.write("");
                                        linksWriter.newLine();
                                    }
                                    else
                                    {
                                        linksWriter.write(link);
                                        linksWriter.newLine();
                                    }
                                    if(type==null)
                                    {
                                        typesWriter.write("w");
                                        typesWriter.newLine();
                                    }
                                    else
                                    {
                                        typesWriter.write(type);
                                        typesWriter.newLine();
                                    }
                                }

                                catch (IOException e) {
                                    e.printStackTrace();
                                    //Toast.makeText(getApplicationContext(), "no write", Toast.LENGTH_LONG).show();
                                }
                            }
                            try {
                                linksWriter.flush();
                                linksWriter.close();
                                typesWriter.flush();
                                typesWriter.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //txtToArrayList();
                            linksTxtToArrayListAfterUpdate();
                            typesTxtToArrayListAfterUpdate();
                            if ((!linksFromTxt.equals(linksAfterUpdate))||(!typesFromTxt.equals(typesAfterUpdate)))
                            {
                                linksFromTxt.clear();
                                linksFromTxt.addAll(linksAfterUpdate);
                                typesFromTxt.clear();
                                typesFromTxt.addAll(typesAfterUpdate);

                                Toast.makeText(MainActivity.this, "station updated", Toast.LENGTH_SHORT).show();
                            }
                        }
//                        else {
//                            //Toast.makeText(MainActivity.this, "Error updating from server", Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
            }
        }).start();

    }

    void showConsentForm() {

        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.

                                }

                                // Consent has been gathered.
                                if (consentInformation.canRequestAds()) {
                                    initializeMobileAdsSdk();
                                }
                            }
                    );
                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                });

        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk();
        }
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        MobileAds.initialize(this);
        //interstitial Ad

        AdRequest interstitialAdRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-9799428944156340/7449378550", interstitialAdRequest, new InterstitialAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                MainActivity.this.interstitialAd=interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                MainActivity.this.interstitialAd=null;
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration!=null)
        {listenerRegistration.remove();}
        deleteCache(MainActivity.this);
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}