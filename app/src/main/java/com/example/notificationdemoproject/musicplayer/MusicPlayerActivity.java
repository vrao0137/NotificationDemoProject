package com.example.notificationdemoproject.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.notificationdemoproject.CustomMusicNotificationActivity;
import com.example.notificationdemoproject.R;
import com.example.notificationdemoproject.databinding.ActivityMusicPlayerBinding;
import com.example.notificationdemoproject.service.CreateNotifications;
import com.example.notificationdemoproject.service.OnClearFromRecentServices;
import com.example.notificationdemoproject.service.Playable;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener, Playable {
    private final String TAG = MusicPlayerActivity.class.getSimpleName();
    private ActivityMusicPlayerBinding binding;

    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    String sname;
    ArrayList<File> mySongs;

    NotificationManager notificationManager;
    boolean isPlay = false;

    TextView txtSName,txtSStart,txtSStop;
    SeekBar seekMusic;
    BarVisualizer visualizer;

    Thread updateSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize IDs-----------
        txtSName = binding.txtsn;
        txtSStart = binding.txtsstart;
        txtSStop = binding.txtsstop;
        seekMusic = binding.seekbar;
        visualizer = binding.blast;

        // SetOnclickListner
        binding.playbtn.setOnClickListener(this);
        binding.btnnext.setOnClickListener(this);
        binding.btnprev.setOnClickListener(this);
        binding.btnff.setOnClickListener(this);
        binding.btnfr.setOnClickListener(this);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        createChannel();
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        startService(new Intent(getBaseContext(), OnClearFromRecentServices.class));

        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos",0);
        txtSName.setSelected(true);

        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtSName.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        CreateNotifications.createNotifications(MusicPlayerActivity.this,mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() -1);
        binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
        binding.txtsn.setText(mySongs.get(position).getName());

      //  onTrackPlay();

        updateSeekbar = new Thread()
        {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while (currentPosition<totalDuration){
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(currentPosition);
                    }catch (InterruptedException | IllegalAccessError e){
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
                handler.postDelayed(this,delay);
            }
        },delay);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                binding.btnnext.performClick();
            }
        });

        int audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1){
            visualizer.setAudioSessionId(audiosessionId);
        }

    }

    public void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.imageview,"rotation", 0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";

        if (sec<10){
            time+="0";
        }
        time+=sec;

        return time;
    }

    private void createChannel(){
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CreateNotifications.CHANNEL_ID,"KOD Dev", NotificationManager.IMPORTANCE_LOW);

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
                case CreateNotifications.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotifications.ACTION_PLAY:
                    if (isPlay){
                        onTrackPause();
                    }else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotifications.ACTION_NEXT:
                    onTrackNext();
                    break;

            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playbtn:
                if (mediaPlayer.isPlaying()){
                    CreateNotifications.createNotifications(MusicPlayerActivity.this,mySongs.get(position),
                            R.drawable.ic_play, position, mySongs.size() -1);

                    binding.playbtn.setBackgroundResource(R.drawable.ic_play);
                    binding.txtsn.setText(mySongs.get(position).getName());

                    mediaPlayer.pause();
                    isPlay = true;
                }else {
                    CreateNotifications.createNotifications(MusicPlayerActivity.this,mySongs.get(position),
                            R.drawable.ic_pause, position, mySongs.size() -1);

                    binding.txtsn.setText(mySongs.get(position).getName());
                    binding.playbtn.setBackgroundResource(R.drawable.ic_pause);

                    mediaPlayer.start();
                    isPlay = false;
                }
                break;

            case R.id.btnnext:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName();
                txtSName.setText(sname);
                mediaPlayer.start();
                binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(binding.imageview);

                int audiosessionId = mediaPlayer.getAudioSessionId();
                if (audiosessionId != -1){
                    visualizer.setAudioSessionId(audiosessionId);
                }
                break;

            case R.id.btnprev:
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);

                Uri u1 = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(),u1);
                sname = mySongs.get(position).getName();
                txtSName.setText(sname);
                mediaPlayer.start();
                binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
                startAnimation(binding.imageview);

                int audiosessionIdPre = mediaPlayer.getAudioSessionId();
                if (audiosessionIdPre != -1){
                    visualizer.setAudioSessionId(audiosessionIdPre);
                }
                break;

            case R.id.btnff:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
                break;

            case R.id.btnfr:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null){
            visualizer.release();
        }else {

        }
        notificationManager.cancelAll();
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onTrackPrevious() {
        position --;
        CreateNotifications.createNotifications(MusicPlayerActivity.this,mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() -1);
        binding.txtsn.setText(mySongs.get(position).getName());
    }

    @Override
    public void onTrackPlay() {
        CreateNotifications.createNotifications(MusicPlayerActivity.this,mySongs.get(position),
                R.drawable.ic_play, position, mySongs.size() -1);
        binding.playbtn.setBackgroundResource(R.drawable.ic_play);
        binding.txtsn.setText(mySongs.get(position).getName());
        mediaPlayer.pause();
        isPlay = true;
    }

    @Override
    public void onTrackPause() {
        CreateNotifications.createNotifications(MusicPlayerActivity.this,mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() -1);
        binding.playbtn.setBackgroundResource(R.drawable.ic_pause);
        binding.txtsn.setText(mySongs.get(position).getName());
        mediaPlayer.start();
        isPlay = false;
    }

    @Override
    public void onTrackNext() {
        position ++;
        CreateNotifications.createNotifications(MusicPlayerActivity.this,mySongs.get(position),
                R.drawable.ic_pause, position, mySongs.size() -1);
        binding.txtsn.setText(mySongs.get(position).getName());
    }
}