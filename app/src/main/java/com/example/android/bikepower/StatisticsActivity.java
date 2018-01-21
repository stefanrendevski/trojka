package com.example.android.bikepower;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import br.com.felix.horizontalbargraph.HorizontalBar;
import br.com.felix.horizontalbargraph.model.BarItem;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ActionBar actionBar = this.getSupportActionBar();

        HorizontalBar horizontal = findViewById(R.id.horizontal);
        if(horizontal!=null){
        }
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            horizontal.init(this).addAll(items()).build();
        }
    }
    public List items(){
        List<BarItem> list=new ArrayList<>();
        list.add(new BarItem("17/01/2018",105d));
        list.add(new BarItem("18/01/2018",170d));
        list.add(new BarItem("19/01/2018",200d));
        list.add(new BarItem("20/01/2018",135d));
        list.add(new BarItem("21/01/2018",100d));
        list.add(new BarItem("22/01/2018",300d));
        return list;
    };
}
