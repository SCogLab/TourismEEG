package com.example.BCIVoyager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

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

import java.io.IOException;
import static utils.AsyncUtils.executeAsync;
import static utils.MatrixUtils.invertFloatMatrix;

public class Activity5Images extends AppCompatActivity {

    ReadWriteCsv rwcsv;
    private EEGExport csv;
    long time;
    Map<String, List<Long>> timeImg = new HashMap<>();
    //Map<String, List<Long>> timeImgtmp = new HashMap<>();

    Integer nbImgCat = 19;  //Nombre d'image par catégorie. Toutes les catégories ont le meme nombre d'image

    Float cat1 = Float.parseFloat("0");
    Float cat2 = Float.parseFloat("0");
    Float cat3 = Float.parseFloat("0");
    Float cat4 = Float.parseFloat("0");
    Float cat5 = Float.parseFloat("0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity5_images);

        initConnectionStateListener();
        initEegListener();

        // Association du nombre d'image en fonction du choix de l'expérience
        Intent intent = getIntent();
        nbImgCat = intent.getIntExtra("nbimg",29);
        Toast.makeText(Activity5Images.this, "Démarrage de l'expérience ("+nbImgCat+ " img/c)", Toast.LENGTH_LONG).show();

        sdkClient = MbtClient.getClientInstance();
        sdkClient.setConnectionStateListener(bluetoothStateListener);
        isStreaming = true;
        time = System.currentTimeMillis();
        startStream(new StreamConfig.Builder(eegListener)
                .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)
                .useQualities()
                .create());


        csv = new EEGExport("eeg.csv");
        //ClassApparitionImage cai = new ClassApparitionImage("apparition.csv");
        rwcsv = new ReadWriteCsv("eeg0.csv");
        Log.e("Activity_Images", "Start " + System.currentTimeMillis());


        /**
         * Association des images avec leurs nom.
         * String : nom => permet de retrouver la catégorie
         * Integer : numero de referencement de l'image
         */
        Map<String, Integer> idImg = new HashMap<>();

        // Feed la Map
        //idImg = makeListName(idImg, "i", nbImgCat); // Categorie 0 Test

        idImg = makeListName(idImg, "j", nbImgCat); // Categorie 1 Beach
        timeImg.put("j", new ArrayList<>());
        idImg = makeListName(idImg, "k", nbImgCat); // Categorie 2 Family
        timeImg.put("k", new ArrayList<>());
        idImg = makeListName(idImg, "l", nbImgCat); // Categorie 3 Outdoor, nature
        timeImg.put("l", new ArrayList<>());
        idImg = makeListName(idImg, "m", nbImgCat); // Categorie 4 Snow, winter
        timeImg.put("m", new ArrayList<>());
        idImg = makeListName(idImg, "n", nbImgCat); // Categorie 5 Spa
        timeImg.put("n", new ArrayList<>());

        // TODO: 2019-09-09  Modifier ici pour ajouter les categories

        // Calcule de tous les temps d'attentes
        for (int i = 0; i < nbFullImg; ++i) {
            /*
             * Calcule un temps variable entre 300 et 500 ms pour l'affichage du "+"
             * Et sauvegarde dans tableau
             */
            tempsAttentes[i] = (int) ((500 - 300) * Math.random() + 300);
        }

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
        final TextView tv = findViewById(R.id.textView2);
        tv.setText("");
        // On change l'image
        //img.setImageResource(R.drawable.logo_zen);
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
                        // On lui met le signe "+" pour afficher une croix au centre
                        tv.setText("+");
                        // On rend invisible l'image
                        img.setVisibility(View.INVISIBLE);
                    }
                });


                // sleep permet de mettre en "pause" l'application
                try {
                    TimeUnit.MILLISECONDS.sleep(tempsAttentes[compteur++]);
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
                if (keys.size() == 0 || compteur == nbFullImg) {

                    //new Class_oscStream().sendOSC("compteur", compteur);
                    new Class_oscStream("1", "50").sendOSC("compteur", compteur);   // AJOUT D'UN DEVICE NAME !
                    //Log.i("#STOP",  "");

                    t.cancel(false);



                    Intent intent = new Intent(getBaseContext(), Activity6Results.class);
                    intent.putExtra("classement",classement);
                    startActivityForResult(intent, 0);


                    finish();
                    //timer.purge();
                    Date date = new Date();
                    Log.i("Date", date.toString());
                    // Permet de changer d'activity = factultatif
                    //Intent myIntent = new Intent(getBaseContext(), Activity1Launchscreen.class);
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
                //Class_oscStream tmp = new Class_oscStream();
                //tmp.sendOSC(randomKey);
                /*startStream(new StreamConfig.Builder(eegListener)
                        .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)
                        .create());*/
                //Log.i("AAAAAA","aaaaaaa");


               /* try {
                    //cai.writeCsv(String.valueOf(System.nanoTime()),randomKey.charAt(0)+"");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                timeImg.get(randomKey.charAt(0) + "").add(System.currentTimeMillis());

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // On re-affiche l'image
                        img.setVisibility(View.VISIBLE);
                        //  on change l' image
                        img.setImageResource(newImg.intValue());


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
                        tv.setText("");

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

    /*
    Compteur pour le tableau
     */
    int compteur = 0;
    /**
     * Nombre d'image par catégorie. Toutes les catégories ont le meme nombre d'image
     */
    //Integer nbImgCat = 29;
    // TODO: 2019-09-09 Ici ajouter modifer et mettre le bon nombre d'image
    /**
     * Nombre d'image totale (peut surement etre fait automatiquement)
     */
    final Integer nbFullImg = nbImgCat * 5;
    int[] tempsAttentes = new int[nbFullImg];

    /**
     * Pour le timer, on créer un pool de Thread (ici 1 car pas besoin de plus ...)
     */
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    /**
     * Je sais pas trop, mais c'est pour arreter le timer
     */
    ScheduledFuture<?> t;
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
    int sleepChiffre = 1000;
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
                Toast.makeText(getBaseContext(), error.getMessage() + (additionnalInfo != null ? additionnalInfo : ""), Toast.LENGTH_SHORT).show();
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
                    new Class_oscStream("1", "50").execute(mbtEEGPackets);         // AJOUT D'UN DEVICE NAME !

                    compterLesSecondes ++;

                    if(compterLesSecondes == 60){
                        compterLesSecondes = 0;
                        compteurCsv++;
                        rwcsv.setFile("eeg"+compteurCsv+".csv");
                        executeAsync(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    classement = classement("eeg"+(compteurCsv-1)+".csv");
                                    new Class_oscStream("1","50").sendOSC("classement", classement.charAt(0));     // AJOUT D'UN DEVICE NAME !
                                    Log.e("classement", classement);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }

                    queue.add(mbtEEGPackets.getChannelsData());
                    executeAsync(new Runnable() {
                        @Override
                        public void run() {
                            write();
                        }
                    });

                }
            }
        };
    }

    static String classement;
    int compterLesSecondes = 0;
    int compteurCsv = 0;
    Queue<ArrayList<ArrayList<Float>>> queue = new LinkedList<>();
    /**
     * Method used to stop a EEG raw data streaming in progress.
     */
    private void stopStream() {
        isStreaming = false;
        sdkClient.stopStream();
        Toast.makeText(getBaseContext(), "Stop streaming", Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(getBaseContext(), ActivitySettings.class);
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

    private void write() {

        ArrayList<ArrayList<Float>> tmp = queue.remove();
        for (int i = 0; i < tmp.get(0).size(); i = i + 2) {
            StringBuilder sb = new StringBuilder();
            time = time + 8;
            sb.append(time);
            sb.append(";");
            sb.append(tmp.get(0).get(i));
            sb.append(";");
            sb.append(tmp.get(1).get(i));

            rwcsv.writeCsv(sb.toString());
            //Log.d("ecriture","dans le csv");
        }
        //rwcsv.writeCsv("end");

    }
    public String classement(String fileName) throws IOException {
        StringBuilder result = new StringBuilder();
        //Float[] res = new Float[5];
        Log.d("timeImg", timeImg.toString());
        for(long i : timeImg.get("j"))
            cat1 += rwcsv.getGraph(fileName,i);
        timeImg.get("j").clear();

        for(long i : timeImg.get("k"))
            cat2 += rwcsv.getGraph(fileName,i);
        timeImg.get("k").clear();

        for(long i : timeImg.get("l"))
            cat3 += rwcsv.getGraph(fileName,i);
        timeImg.get("l").clear();

        for(long i : timeImg.get("m"))
            cat4 += rwcsv.getGraph(fileName,i);
        timeImg.get("m").clear();

        for(long i : timeImg.get("n"))
            cat5 += rwcsv.getGraph(fileName,i);
        timeImg.get("n").clear();

        /*res[0]=cat1;
        res[1]=cat2;
        res[2]=cat3;
        res[3]=cat4;
        res[4]=cat5;
        Arrays.sort(res);*/
        HashMap<Float, String> map = new HashMap<>();

        map.put(cat1, "j");
        map.put(cat2, "k");
        map.put(cat3, "l");
        map.put(cat4, "m");
        map.put(cat5, "n");

        TreeMap<Float, String> treeMap = new TreeMap<>(map);
        for(int i = 0; i < 5; ++i){
            Log.d("treeMap", treeMap.toString());
            result.append(treeMap.lastEntry().getValue());
            treeMap.remove(treeMap.lastKey());
        }
        return result.toString();
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


