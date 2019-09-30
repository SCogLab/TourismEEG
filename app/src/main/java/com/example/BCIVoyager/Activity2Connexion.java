package com.example.BCIVoyager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import config.ConnectionConfig;
import core.bluetooth.BtState;
import core.device.model.MbtDevice;
import engine.MbtClient;
import engine.SimpleRequestCallback;
import engine.clientevents.BaseError;
import engine.clientevents.BluetoothStateListener;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

//import com.example.myapplication.R;

import static features.MbtFeatures.MELOMIND_DEVICE_NAME_PREFIX;
import static features.MbtFeatures.QR_CODE_NAME_PREFIX;

public class Activity2Connexion extends AppCompatActivity {

    private final static int SCAN_DURATION = 20000; //Maximum duration allocated to find a headset
    public final static String PREVIOUS_ACTIVITY_EXTRA = "PREVIOUS_ACTIVITY_EXTRA"; //Extra key used to share data to the next started activity
    private MbtClient sdkClient; //Instance of SDK client used to access all the SDK features
    private String deviceNameField; //Device name field used to enter a specific headset name on the application for Bluetooth connection
    private String deviceName; //Device name value stored from the value of the {link HomeActivity#deviceNameField}
    private String deviceQrCodeField; //Device QR code field used to enter a specific headset QR code on the application for Bluetooth connection
    private String deviceQrCode; //Device QR code value stored from the value of the {link HomeActivity#deviceQrCodeField}
    private Switch connectAudioSwitch; //Switch used to enable or disable Bluetooth audio connection.

    private Toast toast; //Toast used to notify the user by displaying a temporary message on the foreground of the screen
    private Button scanButton; //Button used to initiate the Bluetooth connection with a Melomind headset on click

    /**
     * Boolean value stored from the value of the {link HomeActivity#connectAudioSwitch} :
     * Audio Bluetooth connection is enabled if {link HomeActivity#connectAudio} is true.
     * Audio Bluetooth connection is disabled if {link HomeActivity#connectAudio} is false.
     */
    private boolean connectAudio = false;

    /**
     * Boolean value stored for Bluetooth connection cancel :
     * A Bluetooth connection in progress can be cancelled by the user within the {link HomeActivity#SCAN_DURATION} duration by clicking on the {link HomeActivity#scanButton}
     * If no Bluetooth connection is in progress, clicking on the {link HomeActivity#scanButton} starts a Bluetooth connection
     */
    private boolean isCancelled = false;

    /**
     * Boolean value stored for Bluetooth connection error :
     * A Bluetooth connection in progress can be cancelled by the SDK if it returns an error
     */
    private boolean isErrorRaised = false;

    /**
     * Listener used to receive a notification when the Bluetooth connection state changes
     * If you just want to know when a headset is connected or disconnected,
     * you can replace the BluetoothStateListener listener with a ConnectionStateListener<BaseError> listener.
     */
    private BluetoothStateListener bluetoothStateListener = new BluetoothStateListener() {
        /**
         * Callback used to receive a notification when the Bluetooth connection state changes
         */
        @Override
        public void onNewState(BtState newState) {
            if (newState.equals(BtState.READING_SUCCESS)) {
                sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {
                    @Override
                    public void onRequestComplete(final MbtDevice melomindDevice) {
                        if (melomindDevice != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDeviceName(melomindDevice);
                                    showDeviceQrCode(melomindDevice);
                                }
                            });
                        }
                    }
                });
            }
        }

        /**
         * Callback used to receive a notification when the Bluetooth connection is aborted if the SDK returns an error
         */
        @Override
        public void onError(BaseError error, String additionnalInfo) {
            isErrorRaised = true;
            updateScanning(false);
            toast = Toast.makeText(Activity2Connexion.this, error.getMessage() + (additionnalInfo != null ? additionnalInfo : ""), Toast.LENGTH_LONG);
            toast.show();
        }

        /**
         * Callback used to receive a notification when the Bluetooth connection is established
         */
        @Override
        public void onDeviceConnected() {
            toast.cancel();
            deinitCurrentActivity();
        }

        /**
         * Callback used to receive a notification when a connected headset is disconnected
         */
        @Override
        public void onDeviceDisconnected() {
            if (!toast.getView().isShown())
                notifyUser(getString(R.string.no_connected_headset));
            if (isCancelled)
                updateScanning(false);
        }
    };

    /**
     * Method call to display the name of the connecting headset in the device name field
     */
    private void showDeviceName(final MbtDevice melomindDevice) {
        deviceName = melomindDevice.getSerialNumber();
        deviceNameField = deviceName.replace(MELOMIND_DEVICE_NAME_PREFIX, "");

    }

    /**
     * Method called to display the QR code of the connecting headset in the QR code field
     */
    private void showDeviceQrCode(final MbtDevice melomindDevice) {
        deviceQrCode = melomindDevice.getExternalName();
        deviceQrCodeField = deviceQrCode.replace(QR_CODE_NAME_PREFIX, "");

    }

    /**
     * Method called by default when the Activity is started
     * It initializes all the views, SDK client, and permissions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_connexion);

        //toast = Toast.makeText(HomeActivity.this, "", Toast.LENGTH_LONG);
        sdkClient = MbtClient.init(getApplicationContext());
        isCancelled = false;

        if (getIntent().hasExtra(Activity2Connexion.PREVIOUS_ACTIVITY_EXTRA)) {
            if (getIntent().getStringExtra(Activity2Connexion.PREVIOUS_ACTIVITY_EXTRA) != null)
                sdkClient.setConnectionStateListener(bluetoothStateListener);
        }
        toast = Toast.makeText(Activity2Connexion.this, "", Toast.LENGTH_LONG);

        //initToolBar();
        //initDeviceNameField();
        //initDeviceNamePrefix();
        //initDeviceQrCodeField();
        //initDeviceQrCodePrefix();
        //initConnectAudioSwitch();
        initScanButton();
        initPermissions();
    }


    /**
     * Method used to initialize the scan button
     */
    private void initScanButton() {
        scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyUser(getString(R.string.scan_in_progress));
                //deviceNamePrefix = String.valueOf(deviceNamePrefixSpinner.getSelectedItem()); //get the prefix chosen by the user in the Spinner
                deviceName = deviceNameField; //get the name entered by the user in the EditText

                //deviceQrCodePrefix = String.valueOf(deviceQrCodePrefixSpinner.getSelectedItem()); //get the prefix chosen by the user in the Spinner
                deviceQrCode = deviceQrCodeField; //get the name entered by the user in the EditText

                //connectAudio = connectAudioSwitch.isChecked();

                if (isCancelled) //Scan in progress : a second click means that the user is trying to cancel the scan
                    cancelScan();

                else // Scan is not in progress : starting a new scan in order to connect to a Mbt Device
                    startScan();
                if (!isErrorRaised)
                    updateScanning(!isCancelled);

            }
        });
    }

    /**
     * Method used to initialize the required application permissions :
     * A system popup appears on the foreground if the permissions are not granted
     * /!\ Bluetooth Low Energy requires Location permission to find an available device
     */
    private void initPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (!hasPermissions(getApplicationContext(), PERMISSIONS))
            ActivityCompat.requestPermissions(Activity2Connexion.this, PERMISSIONS, PERMISSION_ALL);
    }

    /**
     * Method used to check if required permissions are granted :
     * it returns true if permissions are granted, false otherwise
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method used to start a Bluetooth scan if order to find an available headset and initiate connection
     * If a device name is entered, the SDK connects the corresponding headset.
     * If no device name is entered, the SDK connects the first found available headset.
     * The SDK stops the scan after {link HomeActivity#SCAN_DURATION} seconds if no headset is found
     */
    private void startScan() {
        isErrorRaised = false;
        ConnectionConfig.Builder connectionConfigBuilder = new ConnectionConfig.Builder(bluetoothStateListener)
                .deviceName((deviceName != null && (deviceName.equals(MELOMIND_DEVICE_NAME_PREFIX)) ? //if no name has been entered by the user, the default device name is the headset prefix
                        null : deviceName)) //null is given in parameters if no name has been entered by the user
                .deviceQrCode(((deviceQrCode != null) && (deviceQrCode.equals(QR_CODE_NAME_PREFIX))) ? //if no QR code has been entered by the user, the default device name is the headset prefix
                        null : deviceQrCode)
                .maxScanDuration(SCAN_DURATION);
        if (connectAudio)
            connectionConfigBuilder.connectAudioIfDeviceCompatible();

        sdkClient.connectBluetooth(connectionConfigBuilder.create());
    }

    /**
     * Method used to cancel a Bluetooth scan or connection in progress
     */
    private void cancelScan() {
        sdkClient.cancelConnection();
    }

    /**
     * Method used to update the scanning state boolean and the Scan button text
     * The Scan button text is changed into into "Cancel" if scanning is launched
     * or into "Find a device" if scanning is cancelled
     */
    private void updateScanning(boolean isCancelled) {
        this.isCancelled = isCancelled;
        if (!isCancelled)
            toast.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            scanButton.setBackgroundColor((isCancelled ? Color.LTGRAY : getColor(R.color.light_blue)));

        scanButton.setText((isCancelled ? R.string.cancel : R.string.scan));
    }

    /**
     * Method used to notify the user by showing a temporary message on the foreground
     *
     * @param message is the temporary message to show
     */
    private void notifyUser(String message) {
        toast.setText("");
        toast.show();
        toast.setText(message);
        toast.show();
    }

    /**
     * Method called by default when the Android device back buttton is clicked :
     * the listener is set to null to avoid memory leaks
     */
    @Override
    public void onBackPressed() {
        bluetoothStateListener = null;
    }

    /**
     * Method called when the {link HomeActivity} is closed
     */
    private void deinitCurrentActivity() {
        bluetoothStateListener = null;
        final Intent intent = new Intent(Activity2Connexion.this, Activity3Calibration.class);
        startActivity(intent);
        finish();
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
