package com.example.notificationdemoproject.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.notificationdemoproject.NotificationDetailsActivity;
import com.example.notificationdemoproject.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static android.telephony.AvailableNetworkInfo.PRIORITY_HIGH;
import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH;

public class MyMessagingServices extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("MyMessagingServices","MyMessagingServices:- "+remoteMessage.getFrom());
        Log.e("MyMessagingServices","remoteMessage:- "+remoteMessage.getData());

        /*Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String response = gson.toJson(remoteMessage.getData());*/

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Objects.requireNonNull(Objects.requireNonNull(remoteMessage.getNotification()).getTitle()).equalsIgnoreCase("Iphone")){
                    JSONObject jsonObjectBody = new JSONObject(Objects.requireNonNull(remoteMessage.getData().get("body")));
                    String bodyMessage = jsonObjectBody.getString("mBody");

                    JSONObject jsonObjectData = new JSONObject(Objects.requireNonNull(remoteMessage.getData().get("data")));
                    String dataImages = jsonObjectData.getString("mImage");

                    headsUpNotification(remoteMessage.getNotification().getTitle(), bodyMessage, dataImages);
                }else if (Objects.requireNonNull(remoteMessage.getNotification().getTitle()).equalsIgnoreCase("FOOD")){
                    JSONObject jsonObjectBody2 = new JSONObject(Objects.requireNonNull(remoteMessage.getData().get("body")));
                    String bodyMessage2 = jsonObjectBody2.getString("fBody");

                    JSONObject jsonObjectData2 = new JSONObject(Objects.requireNonNull(remoteMessage.getData().get("data")));
                    String dataImages2 = jsonObjectData2.getString("fImage");

                    customTriggerNotification(remoteMessage.getNotification().getTitle(), bodyMessage2, dataImages2);
                }
            }


          //  customTriggerNotification(remoteMessage.getNotification().getTitle(), bodyMessage2, dataImages2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*triggerNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(),
                remoteMessage.getNotification().getImageUrl());*/

    }

    private void headsUpNotification(String title, String message, String imageUri){

        Bitmap bmp2 = null;
        try {
            InputStream in2 = new URI(imageUri).toURL().openStream();
            bmp2 = BitmapFactory.decodeStream(in2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        NotificationCompat.BigPictureStyle style2 = new NotificationCompat.BigPictureStyle()
                .bigPicture(bmp2);
        NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "MyNotifications")
                .setSmallIcon(R.drawable.ic_baseline_notifications_none_24)
                .setStyle(style2)
                .setLargeIcon(bmp2)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                //.addAction(0,"OPEN", pendingNotificationIntent)
                .setAutoCancel(true)
                .setContentText(message)
                .setPriority(IMPORTANCE_HIGH)  //must give priority to High, Max which will considered as heads-up notification
                .setDefaults(Notification.DEFAULT_SOUND); // must requires VIBRATE permission;

        // I set the notification priority to max, but it is still at the bottom of the notification area.


        Intent notificationIntent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("notificationDetails", message);
        notificationIntent.putExtra("imageUri", imageUri);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        builder2.setContentIntent(pendingNotificationIntent);
        manager.notify(7, builder2.build());

        /*Bitmap bmp = null;
        try {
            InputStream in = new URI(imageUri).toURL().openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigPicture(bmp);
        //    .setSummaryText("Lunch new Iphone 13 in same price of Iphone 12...........");

        Intent notificationIntent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("notificationDetails", message);
        notificationIntent.putExtra("imageUri", imageUri);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyNotifications")
                .setSmallIcon(R.drawable.ic_baseline_notifications_none_24)
                .setStyle(style)
                .setLargeIcon(bmp)
                .setContentTitle(title)
                .setDefaults(Notification.DEFAULT_SOUND) // must requires VIBRATE permission
                .setPriority(Notification.PRIORITY_MAX) //must give priority to High, Max which will considered as heads-up notification
                .addAction(0,"OPEN", pendingNotificationIntent)
                .setAutoCancel(true)
                .setContentText(message);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(7, builder.build());*/
    }

    private void customTriggerNotification(String title2, String message2, String imageUri2){
        Bitmap bmp = null;
        try {
            InputStream in = new URI(String.valueOf(imageUri2)).toURL().openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigPicture(bmp);
        //    .setSummaryText("Lunch new Iphone 13 in same price of Iphone 12...........");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyNotifications")
                .setSmallIcon(R.drawable.ic_baseline_notifications_none_24)
                .setStyle(style)
                .setLargeIcon(bmp)
                .setContentTitle(title2)
                .setAutoCancel(true)
                .setContentText(message2);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(7, builder.build());

        Intent notificationIntent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
        notificationIntent.putExtra("title", title2);
        notificationIntent.putExtra("notificationDetails", message2);
        notificationIntent.putExtra("imageUri", imageUri2.toString());
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingNotificationIntent);
        manager.notify(7, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String token = s;
        Log.e("", "token:- " + token);
    }

    private void triggerNotification(String title, String message, Uri imageUri){

        Bitmap bmp = null;
        try {
            InputStream in = new URI(String.valueOf(imageUri)).toURL().openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigPicture(bmp);
            //    .setSummaryText("Lunch new Iphone 13 in same price of Iphone 12...........");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyNotifications")
                .setSmallIcon(R.drawable.ic_baseline_notifications_none_24)
                .setStyle(style)
                .setLargeIcon(bmp)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(message);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(7, builder.build());

        Intent notificationIntent = new Intent(getApplicationContext(), NotificationDetailsActivity.class);
        notificationIntent.putExtra("title", title);
        notificationIntent.putExtra("notificationDetails", message);
        notificationIntent.putExtra("imageUri", imageUri.toString());
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(),0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingNotificationIntent);
        manager.notify(7, builder.build());
    }
}
