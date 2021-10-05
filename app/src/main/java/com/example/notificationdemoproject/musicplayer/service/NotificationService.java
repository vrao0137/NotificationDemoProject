package com.example.notificationdemoproject.musicplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.notificationdemoproject.R;
import java.io.File;

public class NotificationService extends Service{
    static Context mContext;
    public static final String CHANNEL_ID = "channel1";
    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";

    MyBinder myBinder = new MyBinder();

    static MediaPlayer mMediaPlayer;
    static Float playBackSpeed;
    static MediaSessionCompat mediaSessionCompat;

    static int position;
    static int sSize;

    static int drw_previous;
    static int drw_next;
    static File fFile;
    static boolean isPlay = false;


    public static void createNotifications(Context context, File file, int playbutton, int pos, int size, MediaPlayer mediaPlayer, Float plyBackSpeed) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        mediaSessionCompat = new MediaSessionCompat(context,"My Music");
        mMediaPlayer = mediaPlayer;
        playBackSpeed = plyBackSpeed;
        position = pos;
        mContext = context;
        sSize = size;
        fFile = file;

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.music);

        getPendingIntentPrevious();
        getPendingIntentNext();
        getPendingIntentPlay();

        mContext.registerReceiver(innerBroadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        /*LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));*/

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_music)
                .setContentTitle(fFile.getName())
                .setLargeIcon(icon)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(drw_previous, "Previous", getPendingIntentPrevious())
                .addAction(playbutton, "Play", getPendingIntentPlay())
                .addAction(drw_next, "Next", getPendingIntentNext())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()));

        notificationManagerCompat.notify(1, notification.build());

        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder().putLong(MediaMetadataCompat.METADATA_KEY_DURATION,mediaPlayer.getDuration()).build());
        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING,
                mediaPlayer.getCurrentPosition(),playBackSpeed).setActions(PlaybackStateCompat.ACTION_SEEK_TO).build());
        updateSeekbar();

    }

    public static void updateSeekbar(){
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                mMediaPlayer.seekTo((int) pos);
                mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder().putLong(MediaMetadataCompat.METADATA_KEY_DURATION,mMediaPlayer.getDuration()).build());
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING,
                        mMediaPlayer.getCurrentPosition(),playBackSpeed).setActions(PlaybackStateCompat.ACTION_SEEK_TO).build());
            }
        });
    }


    protected static PendingIntent getPendingIntentPrevious(){
        PendingIntent pendingIntentPrevious;
        if (position == 0) {
            pendingIntentPrevious = null;
            drw_previous = 0;
        } else {
            Intent intentPrevious = new Intent(mContext, NotificationActionServices.class).setAction(ACTION_PREVIOUS);
            pendingIntentPrevious = PendingIntent.getBroadcast(mContext, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_previous = R.drawable.ic_previouse;
        }
        return pendingIntentPrevious;
    }

    protected static PendingIntent getPendingIntentPlay(){
        Intent intentPlay = new Intent(mContext, NotificationActionServices.class)
                .setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(mContext, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntentPlay;
    }

    protected static PendingIntent getPendingIntentNext(){
        PendingIntent pendingIntentNext;
        if (position == sSize) {
            pendingIntentNext = null;
            drw_next = 0;
        } else {
            Intent intentNext = new Intent(mContext, NotificationActionServices.class).setAction(ACTION_NEXT);
            pendingIntentNext = PendingIntent.getBroadcast(mContext, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_next = R.drawable.ic_next;
        }
        return pendingIntentNext;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public NotificationService currentService() {
            return NotificationService.this;
        }
    }

    static BroadcastReceiver innerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            switch (action) {
                case NotificationService.ACTION_PLAY:
                    if (isPlay) {
                        onTrackPause();
                    } else {
                        onTrackPlay();
                    }
                    break;
            }
        }
    };

    public static void onTrackPause() {
        NotificationService.createNotifications(mContext, fFile,
                R.drawable.ic_pause, position, sSize - 1, mMediaPlayer, 1f);
        mMediaPlayer.start();
        isPlay = false;
        Log.e("","R.drawable.ic_pause"+R.drawable.ic_pause);
    }

    public static void onTrackPlay() {
        NotificationService.createNotifications(mContext, fFile,
                R.drawable.ic_play, position, sSize - 1, mMediaPlayer, 0f);
        mMediaPlayer.pause();
        isPlay = true;
        Log.e("","R.drawable.ic_play"+R.drawable.ic_play);
    }

}
