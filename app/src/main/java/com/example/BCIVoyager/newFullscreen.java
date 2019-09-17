package com.example.BCIVoyager;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Voyager.Launchscreen;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import config.StreamConfig;
import core.bluetooth.BtState;
import core.eeg.storage.MbtEEGPacket;
import engine.MbtClient;
import engine.clientevents.BaseError;
import engine.clientevents.BluetoothStateListener;
import engine.clientevents.EegListener;
import features.MbtFeatures;

import static utils.MatrixUtils.invertFloatMatrix;

public class newFullscreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_fullscreen);

        initConnectionStateListener();
        initEegListener();

        sdkClient = MbtClient.getClientInstance();
        sdkClient.setConnectionStateListener(bluetoothStateListener);
        isStreaming = true;
        startStream(new StreamConfig.Builder(eegListener)
                .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)
                .create());

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
        Date date = new Date();


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
                        TextView tv = findViewById(R.id.textView2);
                        // On lui met le signe "+" pour afficher une croix au centre
                        tv.setText("+");
                        // On rend invisible l'image
                        img.setVisibility(View.INVISIBLE);
                    }
                });
                /**
                 * Calcule un temps variable entre 300 et 500 ms pour l'affichage du "+"
                 */
                int sleepTime = (int) ((500 - 300) * Math.random() + 300);

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

                /*
                 * Random pour effectuer la selection aleatoire
                 */
                Random random = new Random();
                /*
                 * Liste des cles de la map
                 */
                List<String> keys = new ArrayList<>(idImgfinal.keySet());
                //Log.i("#taille map", (keys.size() == 0)+"");

                // Condition d'arret du timer
                if (keys.size() == 0) {
                    //Log.i("#STOP",  "");

                    t.cancel(false);
                    //timer.purge();
                    Date date = new Date();
                    Log.i("Date", date.toString());
                    // Permet de changer d'activity = factultatif
                    //Intent myIntent = new Intent(getBaseContext(), BCIVoyager.class);
                    //startActivityForResult(myIntent, 0);
                }
                /*
                 * Nouveau nom d'image de la cle
                 */
                String randomKey = keys.get(random.nextInt(keys.size()));
                /*
                 * reference de l'image obtenu grace au nom
                 */
                final Integer newImg = idImgfinal.get(randomKey);
                // On l'enleve pour eviter de l'avoir en double
                idImgfinal.remove(randomKey);
                oscStream tmp = new oscStream();
                tmp.sendOSC(randomKey);
                /*startStream(new StreamConfig.Builder(eegListener)
                        .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)
                        .create());*/
                //Log.i("AAAAAA","aaaaaaa");
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
                //stopStream();

                // **************
                // Etape 3
                // black screen
                // **************

                // Maintenant Black screen
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // affichage du black screen, differente façon sont possible pour le faire
                        //img.setImageResource(R.drawable.black);
                        img.setVisibility(View.INVISIBLE);

                    }
                });
                // Sleep time du black screen
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepBS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*
                 * Création du nombre a afficher
                 */
                int nombreAffiche = (int) ((Math.random() * 8) + 1);
                // Si c'est 5 on met 4...
                if (nombreAffiche == 5) {
                    nombreAffiche = 4;
                }
                /*
                 * nombre a afficher, en final pour le changer en "direct"
                 */
                final int finalNbr = nombreAffiche;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // On trouve la text view
                        TextView tv = findViewById(R.id.textView2);
                        // On affiche le nombre
                        tv.setText(finalNbr + "");
                        // On rend invisible l'image
                        //img.setVisibility(View.INVISIBLE);
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
        t = executor.scheduleAtFixedRate(repeatedTask, delay, period, TimeUnit.MILLISECONDS);


    }


    /**
     * Retourne une liste de tous les noms des images associé à leurs ID.
     * @param prefixe début du nom ( ex : pour "i00001" le prefixe est "i")
     * @param nbImages nombre d'images de la categorie
     * @return Map créée
     */
    public Map<String, Integer> makeListName(Map<String, Integer> res, String prefixe, Integer nbImages) {

        // !!!!!!!!!!!
        // Ici on commence à partir de 1 donc i = 1 et i <+=
        // !!!!!!!!!!!
        String s;
        int ID;
        for (int i = 1; i <= nbImages; ++i) {
            s = prefixe;
            if (i < 10) {
                s += "0000";
            } else if (i < 100) {
                s += "000";
            } else {
                s += "00";
            }
            s += i;

            ID = getResources().getIdentifier(s, "drawable", getPackageName());
            res.put(s, ID);
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
    /**
     * Boolean value stored for the current EEG streaming state of the SDK.
     * {link DeviceActivity#isStreaming} is true if a EEG streaming from the headset to the SDK is in progress, false otherwise.
     */
    private boolean isStreaming = false;
    /**
     * Listener used to retrieve the EEG raw data when a streaming is in progress
     */
    private EegListener<BaseError> eegListener;
    private void initEegListener() {
        eegListener = new EegListener<BaseError>() {
            /**
             * Callback used to receive a notification if the EEG streaming is aborted because the SDK returned an error
             */
            @Override
            public void onError(BaseError error, String additionnalInfo) {
                Toast.makeText(newFullscreen.this, error.getMessage()+ (additionnalInfo != null ? additionnalInfo : ""), Toast.LENGTH_SHORT).show();
                if(isStreaming) {
                    stopStream();
                    //updateStreaming();
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
                    // if(eegGraph!=null){
                    //addDataToGraph(mbtEEGPackets.getChannelsData(), mbtEEGPackets.getStatusData());

                    oscStream appBCI = new oscStream();
                    appBCI.execute(mbtEEGPackets);

                    //}
                }
            }
        };
    }

    /**
     * Method used to stop a EEG raw data streaming in progress.
     */
    private void stopStream() {
        isStreaming = false;
        sdkClient.stopStream();
    }

    private boolean isConnected = false;

    /**
     * Method called to initialize the connection state listener.
     * This listener provides a callback used to receive a notification when the Bluetooth connection state changes
     */
    private void initConnectionStateListener() {
        bluetoothStateListener = new BluetoothStateListener(){
            /**
             * Callback used to receive a notification when the Bluetooth connection state changes
             */
            @Override
            public void onNewState(BtState newState) {

            }

            /**
             * Callback used to receive a notification when the Bluetooth connection is established
             */
            @Override
            public void onDeviceConnected() {
                isConnected = true;
            }

            /**
             * Callback used to receive a notification when a connected headset is disconnected
             */
            @Override
            public void onDeviceDisconnected() {
                isConnected = false;
                returnOnPreviousActivity();
            }

            /**
             * Callback used to receive a notification if the Bluetooth connection is aborted because the SDK returned an error
             */
            @Override
            public void onError(BaseError error, String additionnalInfo) {
                //notifyUser(error.getMessage()+(additionnalInfo != null ? additionnalInfo : ""));
            }
        };
    }
    /**
     * Method called to return on the {link HomeActivity} when the {link DeviceActivity} is closed
     */
    private void returnOnPreviousActivity() {
        //notifyUser(getString(R.string.disconnected_headset));
        eegListener = null;
        bluetoothStateListener = null;
        finish();
        Intent intent = new Intent(newFullscreen.this, Launchscreen.class);
        //intent.putExtra(HomeActivity.PREVIOUS_ACTIVITY_EXTRA, BCIVoyageur_Fullscreen.TAG);
        startActivity(intent);
        finish();
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

}


