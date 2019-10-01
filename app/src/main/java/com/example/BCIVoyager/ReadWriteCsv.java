package com.example.BCIVoyager;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

public class ReadWriteCsv {
    private File file;
    private String eegSubDir = "EEG";
    private String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private File fileExported;

    public ReadWriteCsv(String fileName) {
        if (!fileName.contains(".csv"))
            fileName = fileName + ".csv";
        try {
            File folder = new File(baseDir + File.separator + eegSubDir);
            //check if file exist (change every 60 seconds otherwise button_pressed or manual)
            fileExported = new File(folder + File.separator + fileName);

            if (fileExported.exists()) {
                fileExported.delete();
                Log.d("EEGExport", "File delet : " + fileExported);
            }
            if (!fileExported.exists()) {
                fileExported.createNewFile();
                Log.d("EEGExport", "File created : " + fileExported);
            }

        } catch (IOException ioe) {
            Log.d("EEGExport", "Creating File failed");
        }
        this.file = fileExported;
        try {
            FileWriter fileWriter = new FileWriter(this.file, true); //Set true for append mode
            PrintWriter writer = new PrintWriter(fileWriter);
            /*StringBuilder sb = new StringBuilder();
            sb.append("timestamp");
            sb.append(";");
            sb.append("P3");
            sb.append(";");
            sb.append("P4");
            sb.append("\n");
            writer.write(sb.toString());*/
            writer.close();

            fileWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void writeCsv(String text) {
        try {
            FileWriter fileWriter = new FileWriter(this.file, true); //Set true for append mode
            PrintWriter writer = new PrintWriter(fileWriter);

            writer.println(text);
            writer.close();
            fileWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setFile(String fileName){
        if (!fileName.contains(".csv"))
            fileName = fileName + ".csv";
        try {
            File folder = new File(baseDir + File.separator + eegSubDir);
            //check if file exist (change every 60 seconds otherwise button_pressed or manual)
            fileExported = new File(folder + File.separator + fileName);

            if (fileExported.exists()) {
                fileExported.delete();
                Log.d("EEGExport", "File delet : " + fileExported);
            }
            if (!fileExported.exists()) {
                fileExported.createNewFile();
                Log.d("EEGExport", "File created : " + fileExported);
            }

        } catch (IOException ioe) {
            Log.d("EEGExport", "Creating File failed");
        }
        this.file = fileExported;
        try {
            FileWriter fileWriter = new FileWriter(this.file, true); //Set true for append mode
            PrintWriter writer = new PrintWriter(fileWriter);
            /*StringBuilder sb = new StringBuilder();
            sb.append("timestamp");
            sb.append(";");
            sb.append("P3");
            sb.append(";");
            sb.append("P4");
            sb.append("\n");
            writer.write(sb.toString());*/
            writer.close();

            fileWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public Float getGraph(String fileName, long time) throws IOException {
        File folder = new File(baseDir + File.separator + eegSubDir);
        //check if file exist (change every 60 seconds otherwise button_pressed or manual)
        fileExported = new File(folder + File.separator + fileName);
        int dureGraph = 500;
        //ArrayList<Float> p3 = new ArrayList<>();
        //ArrayList<Float> p4 = new ArrayList<>();
        Float p3 = Float.parseFloat("0");
        Float p4 = Float.parseFloat("0");

        CsvReader csvReader = new CsvReader();
        csvReader.setFieldSeparator(';');

        CsvContainer csv = csvReader.read(fileExported, StandardCharsets.UTF_8);
        boolean capture = false;
        int compteur = 0;
        for (CsvRow row : csv.getRows()) {
            if(capture){
                p3 += (Float.parseFloat(row.getField(1)));
                p4 += (Float.parseFloat(row.getField(2)));
                compteur ++;
            }else if(Long.parseLong(row.getField(0)) >= time)
            {
                capture = true;
                p3 += (Float.parseFloat(row.getField(1)));
                p4 += (Float.parseFloat(row.getField(2)));
                compteur ++;
            }
            if(compteur == dureGraph){
                break;
            }
        }
        return (p3 / dureGraph) + (p4 / dureGraph) / 2;

    }


}
