package com.example.android.bikepower;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.logging.Logger;

public class PowerCalculationActivity extends AppCompatActivity {

    private static final String TAG = PowerCalculationActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final int REQUEST_CHECK_SETTINGS = 1;

    TextView mBikePowerTextView;
    Button mStartRunButton;
    Button mStopRunButton;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_calculation);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mBikePowerTextView = findViewById(R.id.tv_bike_power);
        mStartRunButton = findViewById(R.id.button_run_start);
        mStartRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBikePowerTextView.setText("This will start weather and location services");
                getLocationUpdate();
            }
        });
        mStopRunButton = findViewById(R.id.button_run_stop);
        mStopRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBikePowerTextView.setText("This will stop and reset weather and location services");
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                Log.d("RequestPermissionResult", "inside requestPermissionResult");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                }
            }
        }
    }

    public  void requestLocationPermission() {
        int permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mBikePowerTextView.setText("Getting current location...");
                Log.v("OnSuccessListener", "inside onSuccessListener()");
                if (location != null) {
                    mBikePowerTextView.setText("Got current location!!!!");
                    mBikePowerTextView.setText(location.toString());
                }
            }
        });
    }

    public void getLocationUpdate() {
        checkLocationSettings();
        requestLocationPermission();
    }

    public void checkLocationSettings() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(request);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Not really sure what to do here
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(PowerCalculationActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore this error, yo!!
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.power_calculation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }else{
            Intent startStatisticsActivity = new Intent(this, StatisticsActivity.class);
            startActivity(startStatisticsActivity);
            return true;
        }
//        return super.onOptionsItemSelected(item);
    }
}
