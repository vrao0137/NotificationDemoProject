package com.example.notificationdemoproject.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.notificationdemoproject.R;

import java.io.File;

public class CreateNotifications extends Service {
    public static final String CHANNEL_ID = "channel1";

    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";

    public static Thread updateProgress;
  //  static MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(getApplicationContext(),"My Music");
    MyBinder myBinder = new MyBinder();


    public static void createNotifications(Context context, File file, int playbutton, int pos, int size, MediaPlayer mediaPlayer) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context,"My Music");

        /*MediaSessionCompat mediaSession = new MediaSessionCompat(context, "TAG");
        MediaSessionCompat.Token token = mediaSession.getSessionToken();*/

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.music);

        PendingIntent pendingIntentPrevious;
        int drw_previous;
        if (pos == 0) {
            pendingIntentPrevious = null;
            drw_previous = 0;
        } else {
            Intent intentPrevious = new Intent(context, NotificationActionServices.class)
                    .setAction(ACTION_PREVIOUS);
            pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                    intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_previous = R.drawable.ic_previouse;
        }

        Intent intentPlay = new Intent(context, NotificationActionServices.class)
                .setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntentNext;
        int drw_next;
        if (pos == size) {
            pendingIntentNext = null;
            drw_next = 0;
        } else {
            Intent intentNext = new Intent(context, NotificationActionServices.class)
                    .setAction(ACTION_NEXT);
            pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                    intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_next = R.drawable.ic_next;
        }

        /*NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        Notification notification1 = notificationBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.ic_baseline_music)
                .setContentTitle(file.getName())
                .setLargeIcon(icon)
                .addAction(drw_previous, "Previous", pendingIntentPrevious)
                .addAction(playbutton, "Play", pendingIntentPlay)
                .addAction(drw_next, "Next", pendingIntentNext)
                .setTicker(file.getName())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(token))
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        notificationManagerCompat.notify(1, notification1);*/

        int totalDuration = mediaPlayer.getDuration();

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_music)
                .setContentTitle(file.getName())
                .setLargeIcon(icon)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(drw_previous, "Previous", pendingIntentPrevious)
                .addAction(playbutton, "Play", pendingIntentPlay)
                .addAction(drw_next, "Next", pendingIntentNext)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setOngoing(true);
              //  .setProgress(totalDuration, 0, false);

        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder().putLong(MediaMetadataCompat.METADATA_KEY_DURATION,mediaPlayer.getDuration()).build());
        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING,
                mediaPlayer.getCurrentPosition(),0.5f)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO).build());

        notificationManagerCompat.notify(1, notification.build());

        /*updateProgress = new Thread() {
            @Override
            public void run() {
                int currentPosition = 0;

                while (currentPosition < totalDuration) {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();

                        notification.setProgress(totalDuration, currentPosition, false)
                                .setOngoing(false);
                        notificationManagerCompat.notify(1, notification.build());
                    } catch (InterruptedException | IllegalAccessError e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        updateProgress.start();*/

    }

    private static void startForeground(int id, NotificationCompat.Builder notification) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return myBinder;
    }

    public class MyBinder extends Binder {
        public CreateNotifications currentService() {
            return CreateNotifications.this;
        }
    }
}
