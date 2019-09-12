package com.example.CogLab;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import config.StreamConfig;
import core.eeg.storage.MbtEEGPacket;
import engine.MbtClient;
import engine.clientevents.BaseError;
import engine.clientevents.BluetoothStateListener;
import engine.clientevents.EegListener;
import features.MbtFeatures;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static utils.MatrixUtils.invertFloatMatrix;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BCIVoyager_Fullscreen extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    /**
     * permet d'envoyer des données
     */
    public void makeOSC(String imgName){
        String categorie = imgName.substring(0,1);


    }
    /**
     * Retourne une liste de tous les noms des images associé à leurs ID.
     * @param prefixe début du nom ( ex : pour "i00001" le prefixe est "i")
     * @param nbImages nombre d'images de la categorie
     * @return
     */
    public Map<String, Integer> makeListName(Map<String, Integer> res,String prefixe, Integer nbImages){

        // !!!!!!!!!!!
        // Ici on commence à partir de 1 donc i = 1 et i <+=
        // !!!!!!!!!!!
        String s;
        int ID;
        for(int i = 1; i <= nbImages; ++i){
            s = prefixe;
            if( i < 10){
                s+= "0000";
            }else if (i < 100){
                s+= "000";
            }else {
                s+= "00";
            }
            s += i;

            ID = getResources().getIdentifier(s , "drawable", getPackageName());
            res.put(s,ID);
        }

        return res;
    }
    /**
     * Nombre d'image totale (peut surement etre fait automatiquement)
     */
    final Integer nbFullImg = 8; // Pas besoin ... ?
    /**
     * Nombre d'image par catégorie. Toutes les catégories ont le meme nombre d'image
     */
    Integer nbImgCat = 29;
    // TODO: 2019-09-09 Ici ajouter modifer et mettre le bon nombre d'image
    /**
     * Pour le timer, on créer un pool de Thread (ici 1 car pas besoin de plus ...)
     */
    static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
    /**
     * Je sais pas trop, mais c'est pour arreter le timer
     */
    static ScheduledFuture<?> t;

    /**
     * Sleep time de l'etape 2 : image
     */
    int sleepImg = 1000;
    /**
     * Sleep time de l'etape 3 : Black screen
     */
    int sleepBS = 500;
    /**
     * Sleep time de l'etape 4 : chiffre final
     */
    int sleepChiffre = 1500;



    /**
     * Instance of SDK client used to access all the SDK features
     */
    private MbtClient sdkClient;
    /**
     * Listener used to receive a notification when the Bluetooth connection state changes
     * If you just want to know when a headset is connected or disconnected,
     * you can replace the BluetoothStateListener listener with a ConnectionStateListener<BaseError> listener.
     */
    private BluetoothStateListener bluetoothStateListener;

    private oscClass osc = new oscClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Code genere automatiquement
        super.onCreate(savedInstanceState);

        setContentView(R.layout.demo_fullscreen);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);

        // ... fin ...
        //initConnectionStateListener();
        initEegListener();
        sdkClient = MbtClient.getClientInstance();
        //sdkClient.setConnectionStateListener(bluetoothStateListener);

        /**
         * Association des images avec leurs nom.
         * String : nom => permet de retrouver la catégorie
         * Integer : numero de referencement de l'image
         */
        Map<String, Integer> idImg = new HashMap<>();

        // Feed la Map
        idImg = makeListName(idImg, "i", nbImgCat); // Categorie 0 Test

        idImg = makeListName(idImg, "j", nbImgCat); // Categorie 1 Beach
        idImg = makeListName(idImg, "k", nbImgCat); // Categorie 2 Family
        idImg = makeListName(idImg, "l", nbImgCat); // Categorie 3 Outdoor, nature
        idImg = makeListName(idImg, "m", nbImgCat); // Categorie 4 Snow, winter
        idImg = makeListName(idImg, "n", nbImgCat); // Categorie 5 Spa

        // TODO: 2019-09-09  Modifier ici pour ajouter les categories 
        
        /**
         * Copie de idImg car on a besoin d'une variable final pour la passer dans le Timer
         */
        final Map<String, Integer> idImgfinal = idImg;

        /**
         * Notre image View
         */
        final ImageView img;
        // On recherche l'image dans l'activity
        img = findViewById(R.id.image);
        // On change l'image
        img.setImageResource(R.drawable.logo);


        //Boolean boundPositiv = true;

        //while(boundPositiv) {
        /**
         * Timer permettant la mise en marche du défilement des images
         */
        final Timer timer = new Timer("Timer");
        /**
         * Action a faire pendant le timer
         */
        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                // runOnUiThread(new Runnable()..) => Cette ligne permet d'effecctuer des modifications
                // en direct sur l'application
                // Ici c'est modifier l'image de l'imageView

                // **************
                // Etape 1
                // "+" au milieu de l'ecran
                // **************
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // On récupère la textView
                        TextView tv =  findViewById(R.id.textView2);
                        // On lui met le signe "+" pour afficher une croix au centre
                        tv.setText("+");
                        // On rend invisible l'image
                        img.setVisibility(View.INVISIBLE);                    }
                });
                /**
                 * Calcule un temps variable entre 300 et 500 ms pour l'affichage du "+"
                 */
                int sleepTime = (int)((500-300)*Math.random()+300);

                // sleep permet de mettre en "pause" l'application
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                // **************
                // Etape 2
                // Affichage d'une image
                // **************

                /**
                 * Random pour effectuer la selection aleatoire
                 */
                Random random = new Random();
                /**
                 * Liste des cles de la map
                 */
                List<String> keys = new ArrayList<>(idImgfinal.keySet());
                Log.i("#taille map", (keys.size() == 0)+"");

                // Condition d'arret du timer
                if (keys.size() == 0) {
                    Log.i("#STOP",  "");

                    t.cancel(false);
                    //timer.purge();

                    // Permet de changer d'activity = factultatif
                    Intent myIntent = new Intent(getBaseContext(), BCIVoyager.class);
                    startActivityForResult(myIntent, 0);
                }
                /**
                 * Nouveau nom d'image de la cle
                 */
                String randomKey = keys.get(random.nextInt(keys.size()));
                /**
                 * reference de l'image obtenu grace au nom
                 */
                final Integer newImg = idImgfinal.get(randomKey);
                // On l'enleve pour eviter de l'avoir en double
                idImgfinal.remove(randomKey);

                osc.sendOSC(randomKey);
                startStream(new StreamConfig.Builder(eegListener)
                        .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)
                        .create());
                Log.i("AAAAAA","aaaaaaa");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //  on change l' image
                        img.setImageResource(newImg.intValue());
                        // On re-affiche l'image
                        img.setVisibility(View.VISIBLE);

                    }
                });

                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // sleep de l'etape 2
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepImg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopStream();

                // **************
                // Etape 3
                // black screen
                // **************

                // Maintenant Black screen
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // affichage du black screen, differente façon sont possible pour le faire
                        img.setImageResource(R.drawable.black);

                    }
                });
                // Sleep time du black screen
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepBS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /**
                 * Création du nombre a afficher
                 */
                int nombreAffiche = (int) ((Math.random()*8) + 1);
                // Si c'est 5 on met 4...
                if(nombreAffiche == 5){
                    nombreAffiche = 4;
                }
                /**
                 * nombre a afficher, en final pour le changer en "direct"
                 */
                final int tmp = nombreAffiche;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // On trouve la text view
                        TextView tv =  findViewById(R.id.textView2);
                        // On affiche le nombre
                        tv.setText(tmp+"");
                        // On rend invisible l'image
                        img.setVisibility(View.INVISIBLE);
                    }
                });

                // Sleep time
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepChiffre);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
   /* TimerTask repeatedTask = new TimerTask() {
        public void run() { */



        // 2 variables pour lancer le timer : cf documentation

        long delay = 5000L;
        long period = 500L;
        //timer.scheduleAtFixedRate(repeatedTask, delay, period);
        t = executor.scheduleAtFixedRate(repeatedTask,delay,period, TimeUnit.MILLISECONDS);


    }
    /**
     * Boolean value stored for the current EEG streaming state of the SDK.
     * is true if a EEG streaming from the headset to the SDK is in progress, false otherwise.
     * */
    private boolean isStreaming = false;
    /**
     * Listener used to retrieve the EEG raw data when a streaming is in progress
     */
    private EegListener<BaseError> eegListener;

    /**
     * Method called to initialize the Button used start or stop the real time EEG streaming.
     * A streaming is started if you click on this button whereas no streaming was in progress.
     * The current streaming is stopped if you click on this button whereas a streaming was in progress.
     */
    private void startStopStream(){

                if(!isStreaming) {
                    startStream(new StreamConfig.Builder(eegListener)
                            .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)

                            .create());
                }else { //streaming is in progress : stopping streaming
                    stopStream(); // set false to isStreaming et null to the eegListener
                }
            }
    /**
     * Method called to initialize the EEG raw data listener.
     * This listener provides a callback used to receive a notification when a new packet of EEG data is received
     */
    private void initEegListener() {
        eegListener = new EegListener<BaseError>() {
            /**
             * Callback used to receive a notification if the EEG streaming is aborted because the SDK returned an error
             */
            @Override
            public void onError(BaseError error, String additionnalInfo) {
                Toast.makeText(BCIVoyager_Fullscreen.this, error.getMessage()+ (additionnalInfo != null ? additionnalInfo : ""), Toast.LENGTH_SHORT).show();
                if(isStreaming) {
                    stopStream();
                }
            }

            /**
             * Callback used to receive a notification when a new packet of EEG data is received and retrieve its values.
             * The EEG data are returned as a MbtEEGPacket Object that contains a matrix of EEG data acquired during a time interval equals to the notification period.
             * Each column of the matrix contains all the EEG data values acquired by one channel during the whole period.
             * For example, one line of the matrix contains 2 EEG data as it has 2 channels of acquisition.
             * To get the matrix of EEG data, you need to call the following getter :
             * mbtEEGPackets.getChannelsData()
             * Note : Unit is microvolt.
             * The matrix need to be inverted to plot the EEG data on 2 differents lines on the graph.
             */
            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {
                if(invertFloatMatrix(mbtEEGPackets.getChannelsData()) != null)
                    mbtEEGPackets.setChannelsData(invertFloatMatrix(mbtEEGPackets.getChannelsData()));

                if(isStreaming){
                    osc.execute(mbtEEGPackets);
                }
            }
        };
    }

    /**
     * Method used to start a EEG raw data streaming.
     * Some parameters related to the streaming can be configured using the StreamConfig builder.
     * @param streamConfig is the streaming configuration
     */
    private void startStream(StreamConfig streamConfig){
        isStreaming = true;
        sdkClient.startStream(streamConfig);
    }
    /**
     * Method used to stop a EEG raw data streaming in progress.
     *
     */
    private void stopStream(){
        isStreaming = false;
        sdkClient.stopStream();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
