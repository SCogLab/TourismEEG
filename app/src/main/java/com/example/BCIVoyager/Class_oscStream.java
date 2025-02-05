package com.example.BCIVoyager;

import android.os.AsyncTask;
import android.util.Log;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import core.eeg.storage.MbtEEGPacket;

import static utils.MatrixUtils.invertFloatMatrix;

import android.os.AsyncTask;
import android.util.Log;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import core.eeg.storage.MbtEEGPacket;

import static utils.MatrixUtils.invertFloatMatrix;

public class Class_oscStream extends AsyncTask<MbtEEGPacket, Void, Void> {


    String myIP = "192.168.137.1";
    // 192.168.137.124  "192.168.1.33";//"172.28.49.116";  172.28.49.116
    int myPort = 5000; //5000;  8080


    public void sendOSC(String address, int compteur) {
        OSCPortOut osc_port = null;
        try {
            osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ;
        OSCMessage oscmessage = new OSCMessage("/" + address);
        oscmessage.addArgument(compteur);
        try {
            osc_port.send(oscmessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);
            osc_port.send(oscmessage);
            Log.d("osc message", "categorie type");
        } catch (SocketException e) {
            Log.d("OSC2", "Socket Exception");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d("OSC2", "Unknown Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("OSC2", "IO Exception");
            e.printStackTrace();
        }

    }
    public void sendOSC(String address, char compteur) {
        OSCPortOut osc_port = null;
        try {
            osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ;
        OSCMessage oscmessage = new OSCMessage("/" + address);
        oscmessage.addArgument(compteur);
        try {
            Log.d("send message", compteur+"");
            osc_port.send(oscmessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);
            osc_port.send(oscmessage);
            Log.d("osc message", "categorie type");
        } catch (SocketException e) {
            Log.d("OSC2", "Socket Exception");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d("OSC2", "Unknown Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("OSC2", "IO Exception");
            e.printStackTrace();
        }

    }


    public void sendOSC(String imgName) {
        OSCPortOut osc_port;
        OSCMessage oscmessage = new OSCMessage("/cat");
        oscmessage.addArgument(imgName.charAt(0) + "");
        try {
            osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);
            osc_port.send(oscmessage);
            Log.d("osc message", "categorie type");
        } catch (SocketException e) {
            Log.d("OSC2", "Socket Exception");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            Log.d("OSC2", "Unknown Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("OSC2", "IO Exception");
            e.printStackTrace();
        }

    }

        private String melomind_headset_name;

        private String batLvl;

        /**
         * Manage distinct messages from distincts melomind devices
         *
         * @param deviceName
         */

        Class_oscStream(String deviceName, String battLvl) {
            melomind_headset_name = "/"+ deviceName; // to manage distinct headset messages
            batLvl = battLvl;
        }

    //Class_oscStream() {}

        @Override
        protected Void doInBackground(MbtEEGPacket... mbtEEGPackets) {


            String base_address = "/Coglab";  // file will be created each day or export button pushed


            for (MbtEEGPacket packet: mbtEEGPackets) {

                OSCPortOut osc_port;


                try {
                    osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);

                    //com.illposed.osc.transport.udp.OSCPort{In, Out}
                    // adresse IP 172.28.49.29
                    packet.setChannelsData(invertFloatMatrix(packet.getChannelsData()));

                    //pour déterminer le casque utilisé et faire le traitement distant
                    OSCMessage messageHeadsetValue = new OSCMessage(base_address + melomind_headset_name);
                    messageHeadsetValue.addArgument(melomind_headset_name);

                    //pour déterminer le timestamp du record
                    OSCMessage messageTimestampValue = new OSCMessage(base_address+melomind_headset_name+"/Timestamp");
                    messageTimestampValue.addArgument(packet.getTimeStamp());

                    packet.setChannelsData(invertFloatMatrix(packet.getChannelsData()));

                    //Log.d("EEGExport","number of P3 measures"+eegPacket.getChannelsData().get(0).size());

                    OSCMessage messageP3;

                    for (int currentEegData = 0; currentEegData < packet.getChannelsData().get(0).size(); currentEegData++) {

                        // Pour P3 :
                        messageP3 = new OSCMessage(base_address + melomind_headset_name + "/P3");
                        messageP3.addArgument(packet.getChannelsData().get(0).get(currentEegData)*10000);
                        osc_port.send(messageP3);
                    }

                    // Pour P4 :
                    OSCMessage messageP4 = new OSCMessage(base_address+ melomind_headset_name+"/P4");
                    messageP4.addArgument(packet.getChannelsData().get(1).toString());

                    //For the battery
                    OSCMessage messageBattery = new OSCMessage(base_address+melomind_headset_name+"/BatLvl");
                    messageBattery.addArgument(batLvl);

                    try {

                    /*OSCMessage oscmessage = new OSCMessage("/Coglab");
                    oscmessage.addArgument(packet.getQualities());
                    osc_port.send(oscmessage);*/
                        osc_port.send(messageTimestampValue);

                        osc_port.send(messageP4);
                        osc_port.send(messageBattery);

                        Log.d("osc message", messageBattery.toString());

                    } catch (SocketException e) {
                        Log.d("OSC2", "Socket Exception");
                        e.printStackTrace();
                    } catch (UnknownHostException e) {
                        Log.d("OSC2", "Unknown Exception");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("OSC2", "IO Exception");
                        e.printStackTrace();
                    }
                    //com.illposed.osc.OSCMessage
                    //com.illposed.osc.OSCBundle
                    packet.getChannelsData();

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d("OSC2", "IO Exception");
                    e.printStackTrace();
                }

            }

            return null;
        }
}
