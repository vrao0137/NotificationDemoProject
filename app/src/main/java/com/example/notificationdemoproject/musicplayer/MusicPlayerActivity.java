package com.example.notificationdemoproject.musicplayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notificationdemoproject.R;
import com.example.notificationdemoproject.databinding.ActivityMusicPlayerBinding;
import com.example.notificationdemoproject.musicplayer.service.NotificationService;
import com.example.notificationdemoproject.musicplayer.service.Playable;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener, Playable {
    private final String TAG = MusicPlayerActivity.class.getSimpleName();
    private ActivityMusicPlayerBinding binding;

    static MediaPlayer mediaPlayer;
    int position;
    private String sname;
    private ArrayList<File> mySongs;

    private NotificationManager notificationManager;
    boolean isPlay = false;

    private TextView txtSName, txtSStart, txtSStop;
    private SeekBar seekMusic;
    private BarVisualizer visualizer;

    private Thread updateSeekbar;

    NotificationService createNotifications;
    boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = new Intent(this, NotificationService.class);
        startService(intent);
        bindService(intent, boundServiceConnection, BIND_AUTO_CREATE);

        // Initialize IDs-----------
        initializeIds();

        // SetOnclickListner
        initializeSetOnClickListener();

        // InitializeMediaPlayer
        initializeMediaPlayer();

    }

    private void initializeIds() {
        txtSName = binding.txtsn;
        txtSStart = binding.txtsstart;
        txtSStop = binding.txtsstop;
        seekMusic = binding.seekbar;
        visualizer = binding.blast;
    }

    private void initializeSetOnClickListener() {
        binding.playbtn.setOnClickListener(this);
        binding.btnnext.setOnClickListener(this);
        binding.btnprev.setOnClickListener(this);
        binding.btnff.setOnClickListener(this);
        binding.btnfr.setOnClickListener(this);
    }

    private void initializeMediaPlayer() {
        /*Objects.requireNonNull(getSupportActionBar()).setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/

        createChannel();
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtSName.setSelected(true);

        initializeSongsList();
    }

    private void initializeSongsList() {
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtSName.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 1f);
        binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
        binding.txtsn.setText(mySongs.get(position).getName());

        upDatesSeekBar();
    }

    private void upDatesSeekBar() {
        updateSeekbar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while (currentPosition < totalDuration) {
                    try {
                        sleep(400);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(currentPosition);
                    } catch (InterruptedException | IllegalAccessError e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        seekMusic.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekMusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekMusic.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                        R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 1f);
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        txtSStop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtSStart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                binding.btnnext.performClick();
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }
    }

    private final ServiceConnection boundServiceConnection;

    {
        boundServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                NotificationService.MyBinder binderBridge = (NotificationService.MyBinder) service;
                createNotifications = binderBridge.currentService();
                isBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
                createNotifications = null;
            }
        };
    }

    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.imageview, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time += min + ":";

        if (sec < 10) {
            time += "0";
        }
        time += sec;

        return time;
    }

    private void createChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NotificationService.CHANNEL_ID, "Vishal", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action) {
                case NotificationService.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case NotificationService.ACTION_PLAY:
                    if (isPlay) {
                        onTrackPause();
                    } else {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playbtn:
                if (mediaPlayer.isPlaying()) {
                    NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                            R.drawable.ic_play, position, mySongs.size() - 1, mediaPlayer, 0f);

                    binding.playbtn.setBackgroundResource(R.drawable.ic_play);
                    binding.txtsn.setText(mySongs.get(position).getName());

                    mediaPlayer.pause();
                    isPlay = true;
                } else {
                    NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                            R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 1f);

                    binding.txtsn.setText(mySongs.get(position).getName());
                    binding.playbtn.setBackgroundResource(R.drawable.ic_pause);

                    mediaPlayer.start();
                    isPlay = false;
                }
                break;

            case R.id.btnnext:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position + 1) % mySongs.size());

                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtSName.setText(sname);
                mediaPlayer.start();

                binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(binding.imageview);

                NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                        R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 1f);

                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1) {
                    visualizer.setAudioSessionId(audiosessionId);
                }
                break;

            case R.id.btnprev:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);

                Uri u1 = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u1);
                sname = mySongs.get(position).getName();
                txtSName.setText(sname);
                mediaPlayer.start();

                binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(binding.imageview);

                NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                        R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 1f);

                int audiosessionIdPre = mediaPlayer.getAudioSessionId();
                if (audiosessionIdPre != -1) {
                    visualizer.setAudioSessionId(audiosessionIdPre);
                }
                break;

            case R.id.btnff:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
                break;

            case R.id.btnfr:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        notificationManager.cancelAll();
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onTrackPrevious() {

        mediaPlayer.stop();
        mediaPlayer.release();
        position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);

        Uri u1 = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), u1);
        sname = mySongs.get(position).getName();
        txtSName.setText(sname);
        mediaPlayer.start();

        binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(binding.imageview);

        NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 1f);

        int audiosessionIdPre = mediaPlayer.getAudioSessionId();
        if (audiosessionIdPre != -1) {
            visualizer.setAudioSessionId(audiosessionIdPre);
        }

    }

    @Override
    public void onTrackPlay() {
        NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                R.drawable.ic_play, position, mySongs.size() - 1, mediaPlayer, 1f);
        binding.playbtn.setBackgroundResource(R.drawable.ic_play);
        binding.txtsn.setText(mySongs.get(position).getName());
        mediaPlayer.pause();
        isPlay = true;
    }

    @Override
    public void onTrackPause() {
        NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 0f);
        binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
        binding.txtsn.setText(mySongs.get(position).getName());
        mediaPlayer.start();
        isPlay = false;
    }

    @Override
    public void onTrackNext() {
        mediaPlayer.stop();
        mediaPlayer.release();
        position = ((position + 1) % mySongs.size());

        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        sname = mySongs.get(position).getName();
        txtSName.setText(sname);
        mediaPlayer.start();

        binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(binding.imageview);

        NotificationService.createNotifications(MusicPlayerActivity.this, mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() - 1, mediaPlayer, 1f);

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

    }
}