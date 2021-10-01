package com.example.notificationdemoproject.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.SeekBar;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.notificationdemoproject.R;

import java.io.File;

public class CreateNotifications {
    public static final String CHANNEL_ID = "channel1";

    public static final String ACTION_PREVIOUS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";

    public static Notification notification;

    public static void createNotifications(Context context, File file, int playbutton, int pos, int size, MediaPlayer mediaPlayer){
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "TAG");

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.music);

        PendingIntent pendingIntentPrevious;
        int drw_previous;
        if (pos == 0){
            pendingIntentPrevious = null;
            drw_previous = 0;
        }else {
            Intent intentPrevious = new Intent(context,NotificationActionServices.class)
                    .setAction(ACTION_PREVIOUS);
            pendingIntentPrevious = PendingIntent.getBroadcast(context,0,
                    intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_previous = R.drawable.ic_previouse;
        }

        Intent intentPlay = new Intent(context,NotificationActionServices.class)
                .setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context,0,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pendingIntentNext;
        int drw_next;
        if (pos == size){
            pendingIntentNext = null;
            drw_next = 0;
        }else {
            Intent intentNext = new Intent(context,NotificationActionServices.class)
                    .setAction(ACTION_NEXT);
            pendingIntentNext = PendingIntent.getBroadcast(context,0,
                    intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
            drw_next = R.drawable.ic_next;
        }

        /*updateSeekbar = new Thread()
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
        };*/

        /*seekMusic.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekMusic.getProgressDrawable().setColorFilter(context.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekMusic.getThumb().setColorFilter(context.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
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
        });*/

        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_music)
                .setContentTitle(file.getName())
                .setLargeIcon(icon)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(drw_previous, "Previous", pendingIntentPrevious)
                .addAction(playbutton, "Play", pendingIntentPlay)
                .addAction(drw_next, "Next", pendingIntentNext)
                .setProgress(100,10,false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setColor(Color.WHITE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();


        notificationManagerCompat.notify(1, notification);

        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int progressCurrent = 0; progressCurrent<progressMax; progressCurrent +=10){
                    SystemClock.sleep(1000);
                    builder.setProgress(progressMax,progressCurrent,false);
                    notificationManagerCompat.notify(1, builder.build());
                }
            }
        });
        thread.start();*/
    }

}
