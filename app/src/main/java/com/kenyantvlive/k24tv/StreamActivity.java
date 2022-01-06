package com.kenyantvlive.k24tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.TransitionManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class StreamActivity extends AppCompatActivity {
    //UI
    StyledPlayerView playerView;
    ExoPlayer player;
    String name;
    TextView tvStationName;
    ImageView ivFullscreen;
    ImageView ivPlayPause;
    static int playerHeight;
    //AudioFocus
    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    Intent playPauseIntent = new Intent();
    PLayPauseReceiver pLayPauseReceiver;
    final String playPauseAction ="playPauseTv";
    PlayReceiver playReceiver;
    final String playAction ="playK24Tv";
    //States
    boolean isFullscreen=false;
    Boolean isPlaying=false;
    int attemptedReconnections=0;
    //Play
    EventListener eventListener= new EventListener();
    String link;
    //Ads
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);


        mAdView= findViewById(R.id.adView);
        playerView = findViewById(R.id.player_view);
        tvStationName= findViewById(R.id.tvStationName);
        ivFullscreen =playerView.findViewById(R.id.exo_fullscreen_icon);
        ivPlayPause= playerView.findViewById(R.id.exo_pause);


        link= getIntent().getStringExtra("link");
        name= getIntent().getStringExtra("name");

        //BannerAd
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        setStatusColor();

        ivFullscreen.setOnClickListener(new FullscreenOnclickListener());
        ivPlayPause.setOnClickListener(new PlayPauseOnclickListener());
        tvStationName.setText(name);
        //Receivers
        IntentFilter playPauseFilter = new IntentFilter();
        playPauseFilter.addAction(playPauseAction);
        pLayPauseReceiver = new PLayPauseReceiver();
        registerReceiver(pLayPauseReceiver,playPauseFilter);//play/pauseAudioFocus

        IntentFilter playFilter = new IntentFilter();
        playFilter.addAction(playAction);
        playReceiver = new PlayReceiver();
        registerReceiver(playReceiver,playFilter);//PlayFromAd
        //Audio Focus
        playPauseIntent.setAction(playPauseAction);
        audioManager= (AudioManager) getSystemService(AUDIO_SERVICE);
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int i) {
                if(i==AudioManager.AUDIOFOCUS_GAIN)
                {
                    player.play();
                }
                else if(i==AudioManager.AUDIOFOCUS_LOSS)
                {
                    sendBroadcast(playPauseIntent);
                }
                else if(i==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
                {
                    sendBroadcast(playPauseIntent);
                }
            }
        };

        preparePlayer();
    }
    void preparePlayer()
    {
        if (player != null) {
            player.release();}
        player = new ExoPlayer.Builder(this).build();
        player.setMediaItem(MediaItem.fromUri(Uri.parse(link)));
        player.prepare();
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.addListener(eventListener);
        if(!Application.isShowingAds)
        {
            player.setPlayWhenReady(true);
            isPlaying=true;
        }
        ivPlayPause.setBackgroundResource(R.drawable.exo_ic_play_circle_filled);
    }
    class PlayPauseOnclickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(isPlaying)
            {
                ivPlayPause.setBackgroundResource(R.drawable.exo_ic_play_circle_filled);
                player.pause();
                isPlaying=false;
            }
            else
            {
                ivPlayPause.setBackgroundResource(R.drawable.exo_ic_pause_circle_filled);
                player.play();
                isPlaying=true;
            }
        }
    }

    class FullscreenOnclickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {

            if(isFullscreen)
            {
                playerView.getLayoutParams().height = playerHeight;//
                playerView.requestLayout();//restore height portrait when exit full screen with error

                showStatusNavigation();
                ivFullscreen.setBackgroundResource(R.drawable.exo_controls_fullscreen_enter);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                isFullscreen=false;
            }
            else
            {
                playerHeight=playerView.getHeight();//get height to restore portrait when exit full screen

                playerView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                playerView.requestLayout();// wrap video content to fill when in fullscreen.

                removeStatusNavigation();
                ivFullscreen.setBackgroundResource(R.drawable.exo_controls_fullscreen_exit);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                isFullscreen=true;
            }
            layoutTransitionAnimation();
        }
    }
    class PLayPauseReceiver extends BroadcastReceiver
    { @Override
    public void onReceive(Context context, Intent intent) {
        if (isPlaying)
        {
            player.pause();
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);

            ivPlayPause.setBackgroundResource(R.drawable.exo_ic_play_circle_filled);
            isPlaying=false;
            //update UI
            Toast.makeText(context, name+" paused", Toast.LENGTH_SHORT).show();
        }
        else
        {
            audioManager.requestAudioFocus(onAudioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            player.play();
            ivPlayPause.setBackgroundResource(R.drawable.exo_ic_pause_circle_filled);
            isPlaying=true;
        }
    }
    }
    class PlayReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            player.play();
            ivPlayPause.setBackgroundResource(R.drawable.exo_ic_pause_circle_filled);
        }
    }
    class EventListener implements Player.Listener {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (isPlaying) {
                playerView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                playerView.requestLayout();
                layoutTransitionAnimation();// wrap video content

                //playerView.
                StreamActivity.this.isPlaying = true;
                audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                ivPlayPause.setBackgroundResource(R.drawable.exo_ic_pause_circle_filled);
                attemptedReconnections=0;
            } else  {
                StreamActivity.this.isPlaying = false;
                audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                ivPlayPause.setBackgroundResource(R.drawable.exo_ic_play_circle_filled);
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            if(!isFullscreen)
            {
                playerView.getLayoutParams().height = playerView.getHeight();/// set height to dp not pixels
                playerView.requestLayout();//prevents fullscreen on error
                layoutTransitionAnimation();
            }

            if(error.errorCode==PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
                    || error.errorCode==PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
                    ||error.errorCode==PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS)
            {
                Toast.makeText(StreamActivity.this, "station unreachable", Toast.LENGTH_SHORT).show();
            }
            else if(error.errorCode==PlaybackException.ERROR_CODE_UNSPECIFIED)
            {
                Toast.makeText(StreamActivity.this, "unexpected error", Toast.LENGTH_SHORT).show();
            }
            attemptReconnection();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFullscreen)
        {removeStatusNavigation();}
        else
        {showStatusNavigation();}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.removeListener(eventListener);
            player.release();
        }
        unregisterReceiver(pLayPauseReceiver);
        unregisterReceiver(playReceiver);
    }

    @Override
    public void onBackPressed() {
        if(isFullscreen)
        {
            ivFullscreen.setBackgroundResource(R.drawable.exo_controls_fullscreen_enter);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            showStatusNavigation();
            isFullscreen=false;
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
        else
        {
            finish();
        }
    }

    void attemptReconnection()
    {
        attemptedReconnections++;
        if(attemptedReconnections<2)
        {
            preparePlayer();
        }
        else
        {
            Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
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
    void layoutTransitionAnimation()
    {
        TransitionManager.beginDelayedTransition(findViewById(R.id.llStream));
    }
    void setStatusColor()
    {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.black));
    }
}