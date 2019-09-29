package com.example.BCIVoyager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Activity4LaunchXP  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity4_launchxp);
        Button b = findViewById(R.id.launchXP);

        b.setOnClickListener(view ->  {

            Log.i("t", "Change activity");
            Intent myIntent = new Intent(getBaseContext(), Activity5Images.class);
            startActivityForResult(myIntent, 0);
        });
    }
}
