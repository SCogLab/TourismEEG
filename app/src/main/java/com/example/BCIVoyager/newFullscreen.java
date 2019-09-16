package com.example.BCIVoyager;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import config.MbtConfig;
import config.StreamConfig;
import core.bluetooth.BtState;
import core.device.model.MbtDevice;
import core.eeg.storage.MbtEEGPacket;
import engine.MbtClient;
import engine.SimpleRequestCallback;
import engine.clientevents.BaseError;
import engine.clientevents.BluetoothStateListener;
import engine.clientevents.ConnectionStateListener;
import engine.clientevents.DeviceBatteryListener;
import engine.clientevents.EegListener;
import features.MbtDeviceType;
import features.MbtFeatures;

import static features.MbtFeatures.MELOMIND_DEVICE_NAME_PREFIX;
import static utils.MatrixUtils.invertFloatMatrix;

public class newFullscreen extends AppCompatActivity {
    private static String TAG = newFullscreen.class.getName();

    /**
     * Maximum number of raw EEG data to display on the graph.
     * As the sampling frequency is 250 Hz, 250 new points are added to the graph every second
     * The graph window displays 2 seconds of EEG streaming.
     */
    private static final int MAX_NUMBER_OF_DATA_TO_DISPLAY = 500;

    /**
     * Instance of SDK client used to access all the SDK features
     */
    private MbtClient sdkClient;


    /**
     * TextView used to display the connected headset name and QR code
     */
    private TextView connectedDeviceTextView;

    /**
     * Graph used to plot the EEG raw data in real time.
     * The graph window displays 2 seconds of EEG streaming.
     */
    private LineChart eegGraph;

    /**
     * Object used to hold all the curves to plot on the graph
     */
    private LineData eegLineData;

    /**
     * Object used to bundle all the triggers data to plot on the graph
     */
    private LineDataSet status;

    /**
     * Object used to bundle all the raw EEG data of the first channel (P3) to plot on the graph
     */
    private LineDataSet channel1;

    /**
     * Object used to bundle all the raw EEG data of the second channel (P4) to plot on the graph
     */
    private LineDataSet channel2;

    /**
     * Button used start or stop the real time EEG streaming.
     * A streaming is started if you click on this button whereas no streaming was in progress.
     * The current streaming is stopped if you click on this button whereas a streaming was in progress.
     */
    private Button startStopStreamingButton;

    /**
     * Button used to disconnect the connected headset.
     * It also disconnects audio if the headset is connected in Bluetooth for audio streaming.
     */
    private Button disconnectButton;

    /**
     * Button used to get the current battery charge level of the connected headset.
     */
    private Button readBatteryButton;

    /**
     * Boolean value stored for the current Bluetooth connection state of the SDK.
     *  newFullscreen#isConnected} is true if a headset is connected to the SDK, false otherwise.
     */
    private boolean isConnected = false;

    /**
     * Boolean value stored for the current EEG streaming state of the SDK.
     * { newFullscreen#isStreaming} is true if a EEG streaming from the headset to the SDK is in progress, false otherwise.
     * */
    private boolean isStreaming = false;

    /**
     * Listener used to receive a notification when the Bluetooth connection state changes
     * If you just want to know when a headset is connected or disconnected,
     * you can replace the BluetoothStateListener listener with a ConnectionStateListener<BaseError> listener.
     */
    private ConnectionStateListener<BaseError> bluetoothStateListener;

    /**
     * Listener used to retrieve the EEG raw data when a streaming is in progress
     */
    private EegListener<BaseError> eegListener;

    private String deviceName;
    private String deviceQrCode;


    /**
     * Method called by default when the Activity is started
     * It initializes all the views, SDK client, and permissions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_fullscreen);

        initConnectionStateListener();
        initEegListener();

        sdkClient = MbtClient.getClientInstance();
        sdkClient.setConnectionStateListener(bluetoothStateListener);
        //sdkClient.connectBluetooth(connectionConfigBuilder.create());

        initToolBar();
        //initConnectedDeviceTextView();
        initDisconnectButton();
        initReadBatteryButton();
        initStartStopStreamingButton();
        //initEegGraph();
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
                Toast.makeText(newFullscreen.this, error.getMessage()+ (additionnalInfo != null ? additionnalInfo : ""), Toast.LENGTH_SHORT).show();
                if(isStreaming) {
                    stopStream();
                    updateStreaming();
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
                    if(eegGraph!=null){
                        addDataToGraph(mbtEEGPackets.getChannelsData(), mbtEEGPackets.getStatusData());

                        StreamCasque appBCI = new StreamCasque();
                        appBCI.execute(mbtEEGPackets);


                    }
                }
            }
        };
    }

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
                notifyUser(error.getMessage()+(additionnalInfo != null ? additionnalInfo : ""));
            }
        };
    }

    /**
     * Method used to initialize the top tool bar view
     */
    public void initToolBar(){
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.light_blue)));
        }
    }

    /**
     * Method called to initialize the TextView used to display the connected headset name and QR code
     */
    /*private void initConnectedDeviceTextView() {
        connectedDeviceTextView = findViewById(R.id.deviceNameTextView);
        sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {

            /**
             * Callback used to get the connected headset informations
             * @param connectedDevice is the connected headset
             * /
            @Override
            public void onRequestComplete(MbtDevice connectedDevice) {
                if (connectedDevice != null){
                    String deviceName = connectedDevice.getSerialNumber();
                    String deviceQrCode = connectedDevice.getExternalName();
                    connectedDeviceTextView.setText(deviceName + " | " + deviceQrCode);
                }
            }
        });
    }*/

    /**
     * Method called to initialize the Button used to disconnect the connected headset on a click.
     * A click on this button also disconnects audio if the headset is connected in Bluetooth for audio streaming.
     */
    private void initDisconnectButton() {
        /*disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
        if(isStreaming)
            stopStream();

        sdkClient.disconnectBluetooth();
    }

    /**
     * Method called to initialize the Button used to get the battery charge level of the connected headset
     */
    private void initReadBatteryButton() {
        /*readBatteryButton = findViewById(R.id.readBatteryButton);
        readBatteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
        sdkClient.readBattery(new DeviceBatteryListener<BaseError>() {

            /**
             * Callback used to get the battery level of the connected headset
             * @param newLevel is the current battery charge level
             */
            @Override
            public void onBatteryChanged(String newLevel) {
                notifyUser("Current battery level : "+newLevel+" %");
            }

            /**
             * Callback used to receive a notification if the battery reading operation is aborted because the SDK returned an error
             */
            @Override
            public void onError(BaseError error, String additionnalInfo) {
                notifyUser(getString(R.string.error_read_battery));
            }
        });
    }

    /**
     * Method called to initialize the Button used start or stop the real time EEG streaming.
     * A streaming is started if you click on this button whereas no streaming was in progress.
     * The current streaming is stopped if you click on this button whereas a streaming was in progress.
     */
    private void initStartStopStreamingButton(){
        /*startStopStreamingButton = findViewById(R.id.startStopStreamingButton);
        startStopStreamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

        });*/
        if(!isStreaming) {
            startStream(new StreamConfig.Builder(eegListener)
                    .setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD)
                    .create());
        }else { //streaming is in progress : stopping streaming
            stopStream(); // set false to isStreaming et null to the eegListener
        }
        updateStreaming(); //update the UI text in both case according to the new value of isStreaming
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

    /**
     * Method called to update the text of the stream button according to the streaming state
     * The stream button text is changed into "Stop Streaming" if streaming is started
     * or into "Start Streaming" if streaming is stopped
     */
    private void updateStreaming(){
        startStopStreamingButton.setText((isStreaming ? R.string.stop_streaming : R.string.start_streaming));
    }

    /**
     * Method called to initialize the Graph used to plot the raw EEG data
     */
    /*public void initEegGraph(){
        eegGraph = findViewById(R.id.eegGraph);

        status = new LineDataSet(new ArrayList<Entry>(MbtConfig.getEegPacketLength()), getString(R.string.status));
        channel1 = new LineDataSet(new ArrayList<Entry>(250), getString(R.string.channel_1));
        channel2 = new LineDataSet(new ArrayList<Entry>(250), getString(R.string.channel_2));

        status.setDrawValues(false);
        status.disableDashedLine();
        status.setDrawCircleHole(false);
        status.setDrawCircles(false);
        status.setColor(Color.GREEN);
        status.setDrawFilled(true);
        status.setFillColor(Color.GREEN);
        status.setFillAlpha(40);
        status.setAxisDependency(YAxis.AxisDependency.RIGHT);

        channel1.setDrawValues(false);
        channel1.disableDashedLine();
        channel1.setDrawCircleHole(false);
        channel1.setDrawCircles(false);
        channel1.setColor(Color.rgb(3,32,123));
        channel1.setAxisDependency(YAxis.AxisDependency.LEFT);

        channel2.setDrawValues(false);
        channel2.disableDashedLine();
        channel2.setDrawCircleHole(false);
        channel2.setDrawCircles(false);
        channel2.setColor(Color.rgb(99,186,233));
        channel2.setAxisDependency(YAxis.AxisDependency.LEFT);

        eegLineData = new LineData();

        eegLineData.addDataSet(channel1);
        eegLineData.addDataSet(channel2);
        eegLineData.addDataSet(status);

        eegGraph.setData(eegLineData);

        XAxis xAxis = eegGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour

        eegGraph.setDoubleTapToZoomEnabled(false);
        eegGraph.setAutoScaleMinMaxEnabled(true);
        eegGraph.getAxisLeft().setDrawGridLines(false);
        eegGraph.getAxisLeft().setDrawLabels(true);
        eegGraph.getAxisRight().setDrawLabels(true);
        eegGraph.getAxisRight().setDrawGridLines(false);
        eegGraph.getXAxis().setDrawGridLines(false);

        eegGraph.invalidate();
    }*/

    /**
     * Method called to add the entries to the graph every second
     * @param channelData the matrix of raw EEG data of the last second
     * @param statusData the list of triggers
     */
    private void addDataToGraph(ArrayList<ArrayList<Float>> channelData, ArrayList<Float> statusData) {

        LineData data = eegGraph.getData();
        if (data != null) {

            if(channelData.size()< MbtFeatures.getNbChannels(MbtDeviceType.MELOMIND)){
                throw new IllegalStateException("Incorrect matrix size, one or more channel are missing");
            }else{
                if(channelsHasTheSameNumberOfData(channelData)){
                    for(int currentEegData = 0; currentEegData< channelData.get(0).size(); currentEegData++){ //for each number of eeg data
                        //plot the EEG signal
                        for (int currentChannel = 0; currentChannel < MbtFeatures.getNbChannels(MbtDeviceType.MELOMIND) ; currentChannel++){
                            data.addEntry(new Entry(data.getDataSets().get(currentChannel).getEntryCount(), channelData.get(currentChannel).get(currentEegData) *1000000),currentChannel);
                        }
                        if(statusData != null) //plot the triggers
                            data.addEntry(new Entry(data.getDataSets().get(data.getDataSetCount()-1).getEntryCount(), statusData.get(currentEegData).isNaN() ? Float.NaN : statusData.get(currentEegData)), data.getDataSetCount()-1);

                    }
                }else{
                    throw new IllegalStateException("Channels do not have the same amount of data");
                }

            }
            data.notifyDataChanged();
            eegGraph.notifyDataSetChanged();// let the chart know it's data has changed
            eegGraph.setVisibleXRangeMaximum(MAX_NUMBER_OF_DATA_TO_DISPLAY);// limit the number of visible entries : The graph window displays 2 seconds of EEG streaming.
            eegGraph.moveViewToX((data.getEntryCount()/2));// move to the latest entry : previous entries are saved so that you can scroll on the left to visualize the previous seconds of acquisition.

        }else{
            throw new IllegalStateException("Graph not correctly initialized");
        }
    }

    /**
     * Method called to check that all the channels contains the same number of EEG data
     * @param channelData the matrix of raw EEG data of the last second
     * @return true if the channels contains the same number of EEG data, false otherwise
     */
    private boolean channelsHasTheSameNumberOfData(ArrayList<ArrayList<Float>> channelData){
        boolean hasTheSameNumberOfData = true;

        int size = channelData.get(1).size();
        for (int i = 0 ; i < MbtFeatures.getNbChannels(MbtDeviceType.MELOMIND) ; i++){
            if(channelData.get(i).size() != size){
                hasTheSameNumberOfData = false;
            }
        }
        return hasTheSameNumberOfData;
    }

    /**
     * Method used to notify the user by showing a temporary message on the foreground
     * @param message is the temporary message to show
     */
    private void notifyUser(String message){
        Toast.makeText(newFullscreen.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Method called by default when the Android device back buttton is clicked.
     * All the listeners are set to null to avoid memory leaks.
     */
    @Override
    public void onBackPressed() {
        sdkClient.disconnectBluetooth();
        eegListener = null;
        bluetoothStateListener = null;
        sdkClient.setConnectionStateListener(null);
        sdkClient.setEEGListener(null);
        returnOnPreviousActivity();
    }

    /**
     * Method called to return on the {@link BCIVoyager} when the {@link newFullscreen} is closed
     */
    private void returnOnPreviousActivity(){
        notifyUser(getString(R.string.disconnected_headset));
        eegListener = null;
        bluetoothStateListener = null;
        finish();
        Intent intent = new Intent(newFullscreen.this, BCIVoyager.class);
        //intent.putExtra(BCIVoyager.PREVIOUS_ACTIVITY_EXTRA, newFullscreen.TAG);
        startActivity(intent);
    }
}


