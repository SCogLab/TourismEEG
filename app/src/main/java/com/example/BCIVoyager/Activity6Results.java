package com.example.BCIVoyager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class Activity6Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity6_result);

        Intent i = getIntent();
        String res = i.getStringExtra("classement");
        TextView c1 = (TextView) findViewById(R.id.categ1);
        TextView c2 = (TextView) findViewById(R.id.categ2);
        TextView c3 = (TextView) findViewById(R.id.categ3);
        TextView c4 = (TextView) findViewById(R.id.categ4);
        TextView c5 = (TextView) findViewById(R.id.categ5);
        String cat = res.charAt(0)+"";
        if(cat.equals("j"))
            c1.setText(R.string.c1);
        if(cat.equals( "k"))
            c1.setText(R.string.c2);
        if(cat.equals( "l"))
            c1.setText(R.string.c3);
        if(cat.equals( "m"))
            c1.setText(R.string.c4);
        if(cat.equals( "n"))
            c1.setText(R.string.c5);

        cat = res.charAt(1)+"";

        if(cat.equals( "j"))
            c2.setText(R.string.c1);
        if(cat.equals( "k"))
            c2.setText(R.string.c2);
        if(cat.equals( "l"))
            c2.setText(R.string.c3);
        if(cat.equals( "m"))
            c2.setText(R.string.c4);
        if(cat.equals( "n"))
            c2.setText(R.string.c5);

        cat = res.charAt(2)+"";
        if(cat.equals( "j"))
            c3.setText(R.string.c1);
        if(cat.equals( "k"))
            c3.setText(R.string.c2);
        if(cat.equals( "l"))
            c3.setText(R.string.c3);
        if(cat.equals( "m"))
            c3.setText(R.string.c4);
        if(cat.equals( "n"))
            c3.setText(R.string.c5);

        cat = res.charAt(3)+"";
        if(cat.equals( "j"))
            c4.setText(R.string.c1);
        if(cat.equals( "k"))
            c4.setText(R.string.c2);
        if(cat.equals( "l"))
            c4.setText(R.string.c3);
        if(cat.equals( "m"))
            c4.setText(R.string.c4);
        if(cat.equals( "n"))
            c4.setText(R.string.c5);

        cat = res.charAt(4)+"";
        if(cat.equals( "j"))
            c5.setText(R.string.c1);
        if(cat.equals( "k"))
            c5.setText(R.string.c2);
        if(cat.equals( "l"))
            c5.setText(R.string.c3);
        if(cat.equals( "m"))
            c5.setText(R.string.c4);
        if(cat.equals( "n"))
            c5.setText(R.string.c5);

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
