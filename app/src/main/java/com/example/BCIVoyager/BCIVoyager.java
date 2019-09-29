package com.example.BCIVoyager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.example.myapplication.R;

public class BCIVoyager extends AppCompatActivity {
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = findViewById(R.id.v_start);

        b.setOnClickListener(view ->  {

            Log.i("t", "Change activity");
            Intent myIntent = new Intent(getBaseContext(), Activity_Images.class);
            startActivityForResult(myIntent, 0);
        });
    }
}
