package Voyager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.R;


public class Launchscreen extends Activity {

    /*private final String btn_touch = getResources().getString(R.string.v_startVoyager);
    private final String btn_start = getResources().getString(R.string.v_startVoyager);
    private final String btn_relaunch = getResources().getString(R.string.v_restart);
    private final String btn_debug = getResources().getString(R.string.v_debug);
    private final String btn_quit = getResources().getString(R.string.v_quit);*/

    Button btn_touch = null;
    Button btn_start = null;
    Button btn_relaunch = null;
    Button btn_debug = null;
    Button btn_quit = null;
    Button btn_next = null;
    EditText txt_ip = null;
    EditText txt_port = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity4_launchxp);

        // On récupère toutes les vues dont on a besoin
        btn_next = (Button)findViewById(R.id.c_start);
        btn_debug = (Button)findViewById(R.id.c_demo);

        // On attribue un listener adapté aux vues qui en ont besoin
        btn_relaunch.setOnClickListener(razListener);
        txt_ip.addTextChangedListener(textWatcher);
        txt_port.addTextChangedListener(textWatcher);
        btn_next.setOnClickListener(envoyerListener);



        //setContentView(R.layout.activity_premiere_activite);
        //String applicationName = getResources().getString(R.string.app_name);
        String applicationName = getResources().getString(R.string.app_name);
        TextView text = new TextView(this);
        text.setText(R.string.welcomeMsg);
        setContentView(text);

        /*EditText editText = new EditText(this);
        editText.setHint(R.string.editText);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setLines(5);*/
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.premiere_activite, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}