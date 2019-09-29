package com.example.BCIVoyager;

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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.Date;

import static utils.AsyncUtils.executeAsync;

public class ProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity3_calibration);
        //ProgressBar pb = findViewById(R.id.progressBar);
        Toast.makeText(ProgressActivity.this, "Test capteurs en cours...", Toast.LENGTH_LONG).show();

        initConnectionStateListener();
        initEegListener();
        Log.d("1", "ProgressActivity");
        sdkClient = MbtClient.getClientInstance();
        sdkClient.setConnectionStateListener(bluetoothStateListener);
        isStreaming = true;
        startStream(new StreamConfig.Builder(eegListener)
                .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)
                .useQualities()
                .create());

    }

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
                Toast.makeText(ProgressActivity.this, error.getMessage() + (additionnalInfo != null ? additionnalInfo : ""), Toast.LENGTH_SHORT).show();
                if (isStreaming) {
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
                //if(invertFloatMatrix(mbtEEGPackets.getChannelsData()) != null)
                //mbtEEGPackets.setChannelsData(invertFloatMatrix(mbtEEGPackets.getChannelsData()));

                Log.d("newActivity", "ProgressActivity");
                //int cpt = 0;
                if (isStreaming) {
                    executeAsync(new Runnable() {
                        @Override
                        public void run() {
                            Float p3 = mbtEEGPackets.getQualities().get(0);
                            Float p4 = mbtEEGPackets.getQualities().get(1);
                            Log.d("P3", p3.toString());
                            Log.d("P4", p4.toString());
                            Log.d("tag", "");
                            if (p3 >= 0.5 || p4 >= 0.5) {
                                cptCapteur = cptCapteur + 1;
                                Log.e("ProgressActivity", "cpt++");

                            } else {
                                Log.e("ProgressActivity", "cpt --");
                                if (cptCapteur > 0)
                                    cptCapteur--;
                            }

                            if (cptCapteur == 10) {
                                //Toast.makeText(ProgressActivity.this, "Test capteur ok !", Toast.LENGTH_LONG).show();
                                final Intent intent = new Intent(ProgressActivity.this, Activity_Images.class);
                                startActivity(intent);
                                finish();
                            }
                            if (d.getTime() - temps > 1000) {
                                Toast.makeText(ProgressActivity.this, "Verifier la bonne pose des capteurs.", Toast.LENGTH_LONG).show();
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        }


                        //}
                    });
                }
            }

            ;
        };
    }
/*Log.d("P3", p3.toString());
                    Log.d("P4", p4.toString());
                    Log.d("cptP3", cptCapteur+"");
                    Log.d("tag",(p3>=0.5)+"");*/

    // variables pour tests...
    Date d = new Date();
    long temps = d.getTime();
    int cptCapteur = 0;

    /**
     * test pour les capteurs
     *
     * @param p3
     * @param p4
     */
    private void checkCompteur(Float p3, Float p4) {

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
        bluetoothStateListener = new BluetoothStateListener() {
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
        Intent intent = new Intent(ProgressActivity.this, Launchscreen.class);
        //intent.putExtra(HomeActivity.PREVIOUS_ACTIVITY_EXTRA, BCIVoyageur_Fullscreen.TAG);
        startActivity(intent);
        finish();
    }

    /**
     * Method used to start a EEG raw data streaming.
     * Some parameters related to the streaming can be configured using the StreamConfig builder.
     *
     * @param streamConfig is the streaming configuration
     */
    private void startStream(StreamConfig streamConfig) {
        isStreaming = true;
        sdkClient.startStream(streamConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sdkClient.setEEGListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sdkClient.setEEGListener(eegListener);
    }
}