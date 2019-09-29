package com.example.BCIVoyager;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class Activity1Launchscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity1_launchscreen);
        Button b1 = findViewById(R.id.c_start);
        Button b2 = findViewById(R.id.c_demo);
        Button b3 = findViewById(R.id.c_setting);

        b1.setOnClickListener(view ->  {
            Log.i("t", "Lancement de la connexion");
            Intent myIntent = new Intent(getBaseContext(), Activity2Connexion.class);
            startActivityForResult(myIntent, 0);
        });

        b2.setOnClickListener(view ->  {
            Log.i("t", "Lancement de la démo Melomind");
            Intent myIntent = new Intent(getBaseContext(), DemoSDK.class);
            startActivityForResult(myIntent, 0);
        });

        b3.setOnClickListener(view ->  {
            Log.i("t", "Accès aux paramètres de l'application");
            Intent myIntent = new Intent(getBaseContext(), ActivitySettings.class);
            startActivityForResult(myIntent, 0);
        });
    }
}
