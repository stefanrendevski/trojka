package com.example.android.bikepower;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;

public class PowerCalculationActivity extends AppCompatActivity {

    private static final String TAG = PowerCalculationActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final String PREFERENCE_NAME = "user_preferences";
    private SharedPreferences userSettings;

    TextView mBikePowerTextView;
    Button mStartRunButton;
    Button mStopRunButton;
    FusedLocationProviderClient mFusedLocationClient;
    Location mCurrentLocation;
    LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_calculation);
        userSettings = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mBikePowerTextView = findViewById(R.id.tv_bike_power);
        mStartRunButton = findViewById(R.id.button_run_start);
        mStartRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBikePowerTextView.setText("Waiting for location services...");
                startLocationUpdates();
            }
        });
        mStopRunButton = findViewById(R.id.button_run_stop);
        mStopRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdates();
            }
        });

        // Ova mozhe da se izvadi vo posebna klasa
        mLocationCallback = new LocationCallback() {
            double weight = userSettings.getFloat(getString(R.string.pref_user_mass_key), 0);
            double bikeWeight = userSettings.getFloat(getString(R.string.pref_bike_mass_key), 0);
            double totalWeight= weight + bikeWeight;
            double frontalArea = userSettings.getFloat(getString(R.string.pref_user_frontal_area_key), 0);
            double dragCoefficient = userSettings.getFloat(getString(R.string.pref_drag_coefficient_key), 0);
            double driveTrainLoss = 3;
            double airDensity =  1.226;

            double Fgravity = (9.8067 * Math.sin(Math.atan(0/100))) * totalWeight;
            double Frolling = (9.8067 * Math.cos(Math.atan(0/100))) * (totalWeight * 0.005);

            int energySaved = 0;
            double previousLongitude = 0;
            double previousLatitude = 0;
            Date previoustTimestamp;

            public double getDistance(double latitude, double longitude) {
                double R = 6371e3;
                double f1 = Math.toRadians(previousLatitude);
                double f2 = Math.toRadians(latitude);
                double d1 = Math.toRadians(latitude - previousLatitude);
                double d2 = Math.toRadians(longitude - previousLongitude);
                double a = (Math.sin(d1/2) * Math.sin(d1/2)) + (Math.cos(f1) * Math.cos(f2) * Math.sin(d2/2) * Math.sin(d2/2));
                double c = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
                double distance = (R * c);
                return distance;
            }

            @Override
            // Ovoj metod se povikuva sekogash koga ima promena na lokacijata,
            // shto znachi deka logikata za presmetuvanje na energija treba da se implementira ovde.
            // So toa shto treba da se ima pristap do poslednata poznata lokacija pred
            // novite promeni, za da mozhe da se presmeta kolkavo rastojanie izminal korisnikot,
            // i da se akumulira rezultatot vo nekoja promenliva/temporary storage.
            public void onLocationResult(LocationResult result) {

                Log.d("AUTO LOCATION UPDATE", result.toString());
                if (result == null) {
                    mBikePowerTextView.setText("Result is null");
                    return;
                }

                for (Location location : result.getLocations()) {
                    mCurrentLocation = location;
                    double latitude = mCurrentLocation.getLatitude();
                    double longitude = mCurrentLocation.getLongitude();
                    Date timestamp = new Date();

                    if(previousLongitude == 0 && previousLatitude == 0){
                        previousLongitude = longitude;
                        previousLatitude = latitude;
                        previoustTimestamp = new Date();
                    }
                    //testing purposes i didn't have a real device ( mimicking change in longitude and latitude)
//                else {
//                    previousLatitude -= 1;
//                    previousLongitude -= 1;
//                }
                    if(previousLatitude != latitude || previousLongitude != longitude){
                        double distance = getDistance(latitude, longitude);
                        long seconds = (timestamp.getTime() - previoustTimestamp.getTime())/1000;
                        double velocity = distance/seconds;
//                    double velocity = 12.5; this for testing 45km/h
                        double Fdrag = ((0.5 * dragCoefficient) * (frontalArea * airDensity)) * Math.pow(velocity, 2);
                        double powerProduced = (Math.pow((1 - (driveTrainLoss/100)), -1) * (Fgravity + Frolling + Fdrag)) * velocity;
                        energySaved += powerProduced;
                        Log.d("Fgravity", ""+Fgravity);
                        Log.d("Frolling", ""+Frolling);
                        Log.d("Fdrag", ""+Fdrag);
                        Log.d("Velocity", ""+velocity);
                        Log.d("Seconds", ""+seconds);
                        Log.d("Power produced", ""+powerProduced);
                        Log.d("Distance travelled", ""+distance);
                        mBikePowerTextView.setText("Power produced: " + energySaved + " watts");
                        previousLongitude = longitude;
                        previousLatitude = latitude;
                    }
                }
            }
        };
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
                Log.v("OnSuccessListener", "inside onSuccessListener()");
                if (location != null) {
                    mCurrentLocation = location;
                }
            }
        });
    }

    public void getLocationUpdate() {
        checkLocationSettings();
        requestLocationPermission();
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (mCurrentLocation == null) {
            getLocationUpdate();
        }
        mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
    }

    public void stopLocationUpdates() {
        mCurrentLocation = null;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void checkLocationSettings() {
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(getLocationRequest());
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

    public LocationRequest getLocationRequest() {
        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return request;
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

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }
}
