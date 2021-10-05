package com.example.notificationdemoproject.musicplayer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionServices extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("TRACKS_TRACKS")
        .putExtra("actionname", intent.getAction()));
/*
        Intent intent1 = new Intent("TRACKS_TRACKS").putExtra("actionname",intent.getAction());
        context.sendBroadcast(intent1);*/
    }
}
