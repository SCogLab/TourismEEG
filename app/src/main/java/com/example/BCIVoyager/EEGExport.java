package com.example.BCIVoyager;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import core.eeg.storage.MbtEEGPacket;
import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;

import android.os.Build;

import com.google.gson.JsonObject;

import static utils.MatrixUtils.invertFloatMatrix;

public class EEGExport {

    private String melomind_headset_name;

    private String batLvl;

    //base directory to retrieve the external storage
    private String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

    //the application eeg subdirectory where the files will be saved
    private String eegSubDir = "EEG";

    private File fileExported;
    private String fileSuffix;
    private String fileName;

    //retrieve system record info to populate recorder_info
    private String systemInfos;

    //retrieve technical device infos from sdk  : P300, Triggers, notch_filter, etc
    private String recorderInfos;

    //recording note from experimenter
    private String recordingNote = "";

    //installing user activity note
    private String note = "";

    private String category = "";

    //retrieve StatusData if P300_ENABLE
    private ArrayList<Float> statusData;

    private CsvWriter csvWriter;

    //json exporter for http communication
    private JsonObject jsonExport;

    /**
     * An EEG to CSV (or edf or fif or json) exporter
     *
     * @param deviceName
     * @param battLvl
     */
    EEGExport(String deviceName, String battLvl, String recorder_info) {
        melomind_headset_name = deviceName; // to manage distinct headset messages
        batLvl = battLvl;
        recorderInfos = recorder_info;
    }

    EEGExport() {
    }

    public void createCSV(String fileName) {
        File folder = new File(baseDir + File.separator + eegSubDir);
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            //check if file exist (change every 60 seconds otherwise button_pressed or manual)
            fileExported = new File(folder + File.separator + fileName);

            if (!fileExported.exists()) {
                fileExported.createNewFile();
                Log.d("EEGExport", "File created : " + fileExported);
            }

        } catch (IOException ioe) {
            Log.d("EEGExport", "Creating File failed");
        }
        csvWriter = new CsvWriter();
        csvWriter.setFieldSeparator(',');
        //csvWriter.setTextDelimiter('\'');
        csvWriter.setLineDelimiter("\r\n".toCharArray());
        //csvWriter.setAlwaysDelimitText(true);


        try (CsvAppender csvAppender = csvWriter.append(fileExported, StandardCharsets.UTF_8)) {
            // header
            csvAppender.appendLine("timestamp", "P3", "P4", "Category");
        } catch (IOException ioe) {
            Log.d("EEExport", "A File Error occurs");
        }
    }

    public String arrayFloatToString(Float[] tab) {
        StringBuilder str = new StringBuilder();
        str.append('[');
        for (Float f : tab) {
            str.append(f);
            str.append(",");
        }
        str.deleteCharAt(str.length() - 1);
        str.append(']');

        return str.toString();
    }


    public void writeCSV(String timestamp, Float[] p3, Float[] p4, String category) { //String.valueOf(eegPacket.getTimeStamp())
        csvWriter = new CsvWriter();
        csvWriter.setFieldSeparator(',');
        //csvWriter.setTextDelimiter('\'');
        csvWriter.setLineDelimiter("\r\n".toCharArray());
        //csvWriter.setAlwaysDelimitText(true);

        try (CsvAppender csvAppender = csvWriter.append(fileExported, StandardCharsets.UTF_8)) {
            csvAppender.appendLine(timestamp);
            csvAppender.appendField(arrayFloatToString(p3));
            csvAppender.appendField(arrayFloatToString(p4));
            csvAppender.appendField(category);
        } catch (IOException ioe) {
            Log.d("EEExport", "A File Error occurs");
        }
    }

    //@Override
    protected Void doInBackground(MbtEEGPacket... mbtEEGPackets) {

        fileSuffix = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());

        fileName = "melo_" + melomind_headset_name + "_" + fileSuffix + ".csv";

        //Test if subfolder exists and if not create
        File folder = new File(baseDir + File.separator + eegSubDir);

        if (!folder.exists()) {
            folder.mkdir();
        }

        try {
            //check if file exist (change every 60 seconds otherwise button_pressed or manual)
            fileExported = new File(folder + File.separator + fileName);

            if (!fileExported.exists()) {
                fileExported.createNewFile();
                Log.d("EEGExport", "File created : " + fileExported);
            }

        } catch (IOException ioe) {
            Log.d("EEGExport", "Creating File failed");
        }


        csvWriter = new CsvWriter();
        csvWriter.setFieldSeparator(',');
        //csvWriter.setTextDelimiter('\'');
        csvWriter.setLineDelimiter("\r\n".toCharArray());
        //csvWriter.setAlwaysDelimitText(true);


        try (CsvAppender csvAppender = csvWriter.append(fileExported, StandardCharsets.UTF_8)) {
            // header
            csvAppender.appendLine("timestamp", "P3", "P4", "Category", "battLvl", "note", "recorder_info", "system_info", "recording_note", "headset_info");

            for (MbtEEGPacket eegPacket : mbtEEGPackets) {

                eegPacket.setChannelsData(invertFloatMatrix(eegPacket.getChannelsData()));

                //Log.d("EEGExport","number of P3 measures"+eegPacket.getChannelsData().get(0).size());

                //append first line to get the global timestamp provided by the Melomind sdk
                csvAppender.appendLine(String.valueOf(eegPacket.getTimeStamp()));


                for (int currentEegData = 0; currentEegData < eegPacket.getChannelsData().get(0).size(); currentEegData++) {


                    // 2nd line in split operations
                    //csvAppender.appendField(String.valueOf(eegPacket.getTimeStamp()));
                    csvAppender.appendField(String.valueOf(System.nanoTime()));
                    csvAppender.appendField(String.valueOf(eegPacket.getChannelsData().get(0).get(currentEegData) * 1000000)); //P3
                    csvAppender.appendField(String.valueOf(eegPacket.getChannelsData().get(1).get(currentEegData) * 1000000)); //P4
                    csvAppender.appendField(getCategory());

                        /*statusData = eegPacket.getStatusData();

                        if (statusData != null) {
                            csvAppender.appendField(statusData.get(currentEegData).isNaN() ? String.valueOf(Float.NaN) : String.valueOf(statusData.get(currentEegData)));
                        } else {
                            csvAppender.appendField(String.valueOf(Float.NaN));
                        }*/

                    csvAppender.appendField(batLvl);
                    csvAppender.appendField(note);
                    csvAppender.appendField(getRecorderInfos());
                    csvAppender.appendField(getSytemInfos());
                    csvAppender.appendField(recordingNote);
                    csvAppender.appendField("melo_" + melomind_headset_name);
                    csvAppender.endLine();
                    //}
                    //eegPacket.getChannelsData();
                }
            }
        } catch (IOException ioe) {
            Log.d("EEExport", "A File Error occurs");
        }

        return null;
    }

    /**
     * @return Headset technical infos from sdk, trigger on/off, Notch_filter 50Hz/60Hz, P300 enable/disable, ...
     */
    private String getRecorderInfos() {
        return recorderInfos;
    }

    //retrieve eeg recorder system informations
    String getSytemInfos() {
        systemInfos = "{SERIAL: " + Build.SERIAL + ", " +
                "MODEL: " + Build.MODEL + " ," +
                "ID: " + Build.ID + ", " +
                "Manufacture: " + Build.MANUFACTURER + ", " +
                "Brand: " + Build.BRAND + ", " +
                "Type: " + Build.TYPE + ", " +
                "User: " + Build.USER + ", " +
                "BASE: " + Build.VERSION_CODES.BASE + ", " +
                "INCREMENTAL: " + Build.VERSION.INCREMENTAL + ", " +
                "SDK:  " + Build.VERSION.SDK + "," +
                "BOARD: " + Build.BOARD + ", " +
                "BRAND: " + Build.BRAND + "," +
                "HOST: " + Build.HOST + ", " +
                "FINGERPRINT: " + Build.FINGERPRINT + ", " +
                "Version Code: " + Build.VERSION.RELEASE + "}";
        return systemInfos;
    }

    void setNotes(String Note) {
        this.note = note;
    }

    String getNotes() {

        if (note.isEmpty()) {
            this.note = "N/A";
        }
        return note;
    }

    private void setRecordingNote(String recording_Note) {
        this.recordingNote = recording_Note;
    }

    private String getRecordingNote() {

        if (recordingNote.isEmpty()) {
            this.recordingNote = "N/A";
        }
        return recordingNote;
    }

    private String getCategory() {

        return category;
    }

    private void setCategory(String category) {

        this.category = category;

    }
}