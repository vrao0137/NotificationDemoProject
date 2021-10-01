package com.example.notificationdemoproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.notificationdemoproject.databinding.ActivityMainBinding;
import com.example.notificationdemoproject.databinding.ActivityNotificationDetailsBinding;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class NotificationDetailsActivity extends AppCompatActivity {
    private final String TAG = NotificationDetailsActivity.class.getSimpleName();
    private ActivityNotificationDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String notificationDetails = intent.getStringExtra("notificationDetails");

        Uri imageUri = Uri.parse(intent.getStringExtra("imageUri"));
        Log.e(TAG, "imageUri:- " + imageUri);

        binding.txvTitle.setText(title);
        binding.txvNotificationDetails.setText(notificationDetails);
        Glide.with(this)
                .load(imageUri)
                .into(binding.ivGetNotificationImage);
    }
}