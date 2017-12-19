package com.example.android.bikepower;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PowerCalculationActivity extends AppCompatActivity {

    private static final String TAG = PowerCalculationActivity.class.getSimpleName();

    TextView mBikePowerTextView;
    Button mStartRunButton;
    Button mStopRunButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_calculation);
        mBikePowerTextView = findViewById(R.id.tv_bike_power);
        mStartRunButton = findViewById(R.id.button_run_start);
        mStartRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBikePowerTextView.setText("This will start weather and location services");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.power_calculation_menu, menu);
        return true;
    }
}
