package com.example.BCIVoyager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class Activity6Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity6_result);
        Button b = findViewById(R.id.launchXP);

        b.setOnClickListener(view ->  {
            Log.i("t", "Change activity");
            Intent myIntent = new Intent(getBaseContext(), Activity1Launchscreen.class);
            startActivityForResult(myIntent, 0);
        });
    }
}
