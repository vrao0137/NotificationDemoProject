package com.example.notificationdemoproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.example.notificationdemoproject.databinding.ActivityCustomMusicNotificationBinding;
import com.example.notificationdemoproject.musicplayer.service.NotificationService;
import com.example.notificationdemoproject.messageservice.OnClearFromRecentServices;
import com.example.notificationdemoproject.musicplayer.service.Playable;

import java.util.ArrayList;
import java.util.List;

public class CustomMusicNotificationActivity extends AppCompatActivity implements Playable {
    private final String TAG = CustomMusicNotificationActivity.class.getSimpleName();
    private ActivityCustomMusicNotificationBinding binding;

    NotificationManager notificationManager;
    List<Track> trackList;

    int position = 0;
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomMusicNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        populateTracks();

        createChannel();
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        startService(new Intent(getBaseContext(), OnClearFromRecentServices.class));

        binding.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*CreateNotifications.createNotifications(CustomMusicNotificationActivity.this,
                        trackList.get(1), R.drawable.ic_baseline_pause_24,1,trackList.size() -1);*/
                if (isPlaying){
                    onTrackPause();
                }else {
                    onTrackPlay();
                }
            }
        });
    }

    // Populate list with tracks
    private void populateTracks(){
        trackList = new ArrayList<>();

        trackList.add(new Track("Track 1","Artist 1", R.drawable.t1));
        trackList.add(new Track("Track 2","Artist 2", R.drawable.t2));
        trackList.add(new Track("Track 3","Artist 3", R.drawable.t3));
        trackList.add(new Track("Track 4","Artist 4", R.drawable.t4));
        trackList.add(new Track("Track 5","Artist 5", R.drawable.t5));
    }

    private void createChannel(){
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NotificationService.CHANNEL_ID,"KOD Dev", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action){
                case NotificationService.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case NotificationService.ACTION_PLAY:
                    if (isPlaying){
                        onTrackPause();
                    }else {
                        onTrackPlay();
                    }
                    break;
                case NotificationService.ACTION_NEXT:
                    onTrackNext();
                    break;

            }
        }
    };

    @Override
    public void onTrackPrevious() {
       /* position --;
        CreateNotifications.createNotifications(CustomMusicNotificationActivity.this,trackList.get(position),
                R.drawable.ic_pause, position, trackList.size() -1);
        binding.title.setText(trackList.get(position).getTitle());*/
    }

    @Override
    public void onTrackPlay() {
       /* CreateNotifications.createNotifications(CustomMusicNotificationActivity.this,trackList.get(position),
                R.drawable.ic_pause, position, trackList.size() -1);
        binding.play.setImageResource(R.drawable.ic_pause);
        binding.title.setText(trackList.get(position).getTitle());
        isPlaying = true;*/
    }

    @Override
    public void onTrackPause() {
        /*CreateNotifications.createNotifications(CustomMusicNotificationActivity.this,trackList.get(position),
                R.drawable.ic_play, position, trackList.size() -1);
        binding.play.setImageResource(R.drawable.ic_play);
        binding.title.setText(trackList.get(position).getTitle());
        isPlaying = false;*/
    }

    @Override
    public void onTrackNext() {
//        position ++;
//        CreateNotifications.createNotifications(CustomMusicNotificationActivity.this,trackList.get(position),
//                R.drawable.ic_pause, position, trackList.size() -1);
//        binding.title.setText(trackList.get(position).getTitle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
        unregisterReceiver(broadcastReceiver);
    }
}