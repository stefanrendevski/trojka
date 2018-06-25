package com.example.android.bikepower;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import br.com.felix.horizontalbargraph.HorizontalBar;
import br.com.felix.horizontalbargraph.model.BarItem;

public class StatisticsActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PERMISSION = 0;
    private HorizontalBar horizontal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        populate();
        ActionBar actionBar = this.getSupportActionBar();

        horizontal = findViewById(R.id.horizontal);
        if(horizontal!=null){
        }
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            horizontal.init(this).addAll(items()).build();
        }
    }
    public void populate(){
        if (!getPermmisions()) {
            return;
        }
    }
    public boolean getPermmisions(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            Snackbar.make(horizontal, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
        }
        return false;
    }
    public List items() {
        List<BarItem> list=new ArrayList<>();
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "statistics.json");
            FileInputStream stream = new FileInputStream(file);
            String jsonStr;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                jsonStr = Charset.defaultCharset().decode(bb).toString();
                JSONArray arr=new JSONArray(jsonStr);
                for(int i=0;i<arr.length();i++){
                    JSONObject jInside=arr.getJSONObject(i);
                    String key=jInside.keys().next();
                    list.add(new BarItem(key, Double.parseDouble(jInside.getString(key))));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
