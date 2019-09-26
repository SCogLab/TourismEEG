package com.example.BCIVoyager;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import core.device.model.MbtDevice;
import core.eeg.storage.MbtEEGPacket;
import features.MbtDeviceType;
import features.MbtFeatures;

import static utils.MatrixUtils.invertFloatMatrix;

/**
 * Export Melomind EEG Data to Json with the json model below (from NeuroJS + metadata) :
 *
 *
 * {
 *   "metric": String // E.g. EEG, ACCL, etc (optional)
 *   "source": String // E.g. Muse, OpenBCI, etc (optional)
 *   "sampleRate": Number // E.g. 250. always in hz (optional)
 *   "mock": Boolean // E.g. false (optional)
 *   "buffer": [ // List of samples
 *     {
 *       "timestamp": Date,
 *       "data": [ // List of channels
 *         {
 *           "id": String // E.g. FP1, FP2, F3, F4, C3, C4, etc (optional)
 *           "value": Number
 *         },
 *         ...
 *       ]
 *     }
 *   ]
 * }
 *
 *
 */
public class EEGJSONExport extends AsyncTask<MbtEEGPacket, Void, Void> {

    private String melomind_headset_name;

    private String batLvl;

    //base directory to retrieve the external storage
    private String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

    //the application eeg subdirectory where the files will be saved
    private String eegSubDir = "EEGJson";

    private File fileExported;
    private String fileSuffix;
    private String fileName;

    //Metadata informations



    private String eegChannelsNumber;

    private String eegManufacturer;

    private String measureType ="EEG"; // could be EEG (default) or Audio

    private String eegScheme; //10-20

    private String eegReference;

    private String eegAcuisitionsLocations;

    private String eegExternalName;

    private String eegHardwareAddress;

    private String eegGround;

    private String eegBandPassFilter;

    private String eegNotchFilter;

    private String eegGain;

    private String eegCapManufacturer;

    private String eegSerialNumber;

    private String creationDate;

    //metadata system informations
    //retrieve system record info to populate recorder_info
    private String systemInfos;

    //retrieve technical device infos from sdk  : P300, Triggers, notch_filter, etc
    private String recorderInfos;

    //recoding Notes from user, experimenter

    //recording note from experimenter
    private String recordingNote = "";

    //installing user activity note
    private String note = "";

    //Triggers Data
    //retrieve StatusData if P300_ENABLE
    private ArrayList<Float> statusData;

    //qualities data
    private ArrayList<Float> qualitiesData;

    //eclecrodeName
    private String electrodeName;

    //json exporter for http communication
    //private JsonObject jsonExport;

    //the data buffer property
    //private JsonArray jsontData;

    //the timestamp
    //private JsonObject jsonTimeStamp;

    //the eegPacketData
    //private JsonArray jsonEEGPacketData;

    //the electrode object
    //private JsonObject jsonElectrodeName; //or id P3 P4 for the Melomind Headset

    //The electrode data array value
    //private JsonObject jsonElectrodeData; //from eegPacket.getChannelsData.get(num_electrode).get(currentEEGData)

    //the device object from sdkClient
    private MbtDevice device;

    /**
     * An EEG to Json exporter for the Melomind
     * @param deviceName
     * @param battLvl
     * @param recorder_info
     */

    EEGJSONExport(String deviceName, String battLvl, String recorder_info) {
        melomind_headset_name = deviceName; // to manage distinct headset messages
        batLvl = battLvl;
        recorderInfos = recorder_info;
    }


    @Override
    protected Void doInBackground(MbtEEGPacket... mbtEEGPackets) {

        fileSuffix = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());

        fileName = "melo_"+melomind_headset_name+"_"+fileSuffix+".json";

        //Test if subfolder exists and if not create
        File folder = new File(baseDir + File.separator + eegSubDir);

        if(!folder.exists()){
            folder.mkdir();
        }

        try {
            //check if file exist (change every 60 seconds otherwise button_pressed or manual)
            fileExported = new File(folder + File.separator + fileName);

                if (!fileExported.exists()) {
                    fileExported.createNewFile();
                    Log.d("EEGJsonExport", "File created : " + fileExported);
                }

            }catch(IOException ioe){
                Log.d("EEGJsonExport", "Creating File failed");
            }

            JsonObject jsonExport = new JsonObject();

            //adding Metadata Headset informations
            jsonExport.addProperty("source", melomind_headset_name);
            jsonExport.addProperty("type", measureType);
            jsonExport.addProperty("manufacturer", eegManufacturer);
            jsonExport.addProperty("channelsnumber",eegChannelsNumber);
            jsonExport.addProperty("source_id",eegSerialNumber);
            jsonExport.addProperty("samplerate", device.getSampRate());
            jsonExport.addProperty("mock", false);
            jsonExport.addProperty("batteryLevel",batLvl);

            //the content of the packet
            //JsonArray jsonData = new JsonArray();

            JsonObject jsonObjectBuffer = new JsonObject();

            JsonObject jsonTimeStamp = new JsonObject();

            JsonObject jsonElectrodeName = new JsonObject();

            JsonObject jsonElectrodeData = new JsonObject();

          for(MbtEEGPacket eegPacket : mbtEEGPackets) {

                eegPacket.setChannelsData(invertFloatMatrix(eegPacket.getChannelsData()));

                JsonArray jsonBuffer = new JsonArray(); //buffer array (Json buffer, array(data : array(electrodes,value)
                JsonArray jsonData = new JsonArray();

                jsonTimeStamp.addProperty("timestamp",eegPacket.getTimeStamp());

                for(int dataSize =0; dataSize<eegPacket.getChannelsData().size(); dataSize++) {

                    //put the data in a float[]
                    for (int currentChannel = 0; currentChannel < MbtFeatures.getNbChannels(MbtDeviceType.MELOMIND); currentChannel++) {

                        if (currentChannel == 0) {
                            electrodeName = "P3";
                        } else {
                            electrodeName = "P4";
                        }

                        jsonElectrodeName.addProperty("id",electrodeName);
                        jsonElectrodeData.addProperty("value",eegPacket.getChannelsData().get(currentChannel).toString());
                        jsonData.add(jsonElectrodeName);
                        jsonData.add(jsonElectrodeData);
                    }

                    jsonBuffer.add(jsonTimeStamp);
                    jsonBuffer.add(jsonData);
                 }
             }

            jsonExport.add("buffer",jsonObjectBuffer);
             Log.d("EEGJsonExport", "JSon object :" + jsonExport.toString());
            return null;
    }

    //write to .jsonfile
    private void writeToFile(JsonObject jsonExport, File jsonFile) throws IOException{

        try(FileWriter file = new FileWriter(jsonFile)) {
                file.write(jsonExport.toString());
        }
    }


    //pretty print
    private String toString(JsonObject jsonString) {

       return jsonString.toString();

    }

    public String getEegChannelsNumber() {
        return eegChannelsNumber;
    }

    public void setEegChannelsNumber(String eegChannelsNumber) {
        this.eegChannelsNumber = eegChannelsNumber;
    }

    public String getEegManufacturer() {
        return eegManufacturer;
    }

    public void setEegManufacturer(String eegManufacturer) {
        this.eegManufacturer = eegManufacturer;
    }

    public String getMeasureType() {
        return measureType;
    }

    public void setMeasureType(String measureType) {
        this.measureType = measureType;
    }

    public String getEegScheme() {
        return eegScheme;
    }

    public void setEegScheme(String eegScheme) {
        this.eegScheme = eegScheme;
    }

    public String getEegReference() {
        return eegReference;
    }

    public void setEegReference(String eegReference) {
        this.eegReference = eegReference;
    }

    public String getEegAcuisitionsLocations() {
        return eegAcuisitionsLocations;
    }

    public void setEegAcuisitionsLocations(String eegAcuisitionsLocations) {
        this.eegAcuisitionsLocations = eegAcuisitionsLocations;
    }

    public String getEegExternalName() {
        return eegExternalName;
    }

    public void setEegExternalName(String eegExternalName) {
        this.eegExternalName = eegExternalName;
    }

    public String getEegHardwareAddress() {
        return eegHardwareAddress;
    }

    public void setEegHardwareAddress(String eegHardwareAddress) {
        this.eegHardwareAddress = eegHardwareAddress;
    }

    public String getEegGround() {
        return eegGround;
    }

    public void setEegGround(String eegGround) {
        this.eegGround = eegGround;
    }

    public String getEegBandPassFilter() {
        return eegBandPassFilter;
    }

    public void setEegBandPassFilter(String eegBandPassFilter) {
        this.eegBandPassFilter = eegBandPassFilter;
    }

    public String getEegNotchFilter() {
        return eegNotchFilter;
    }

    public void setEegNotchFilter(String eegNotchFilter) {
        this.eegNotchFilter = eegNotchFilter;
    }

    public String getEegGain() {
        return eegGain;
    }

    public void setEegGain(String eegGain) {
        this.eegGain = eegGain;
    }

    public String getEegCapManufacturer() {
        return eegCapManufacturer;
    }

    public void setEegCapManufacturer(String eegCapManufacturer) {
        this.eegCapManufacturer = eegCapManufacturer;
    }

    public String getEegSerialNumber() {
        return eegSerialNumber;
    }

    public void setEegSerialNumber(String eegSerialNumber) {
        this.eegSerialNumber = eegSerialNumber;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getSystemInfos() {
        return systemInfos;
    }

    public void setSystemInfos(String systemInfos) {
        this.systemInfos = systemInfos;
    }

    public String getRecorderInfos() {
        return recorderInfos;
    }

    public void setRecorderInfos(String recorderInfos) {
        this.recorderInfos = recorderInfos;
    }

    public String getRecordingNote() {
        return recordingNote;
    }

    public void setRecordingNote(String recordingNote) {
        this.recordingNote = recordingNote;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ArrayList<Float> getStatusData() {
        return statusData;
    }

    public void setStatusData(ArrayList<Float> statusData) {
        this.statusData = statusData;
    }

    public ArrayList<Float> getQualitiesData() {
        return qualitiesData;
    }

    public void setQualitiesData(ArrayList<Float> qualitiesData) {
        this.qualitiesData = qualitiesData;
    }

    public String getElectrodeName() {
        return electrodeName;
    }

    public void setElectrodeName(String electrodeName) {
        this.electrodeName = electrodeName;
    }

    public MbtDevice getDevice() {
        return device;
    }

    public void setDevice(MbtDevice device) {
        this.device = device;
    }
}
