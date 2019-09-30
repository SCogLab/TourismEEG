package com.example.BCIVoyager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class Activity6Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity6_result);

        /*Button b = findViewById(R.id.launchXP);
        b.setOnClickListener(view ->  {
            Log.i("t", "Change activity");
            Intent myIntent = new Intent(getBaseContext(), Activity1Launchscreen.class);
            startActivityForResult(myIntent, 0);
        });*/

        Button btn_menu = findViewById(R.id.menuFloating);
        btn_menu.setOnClickListener(view ->  {
            Log.i("t", "Change activity");
            Intent myIntent = new Intent(getBaseContext(), Activity1Launchscreen.class);
            startActivityForResult(myIntent, 0);
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

}
