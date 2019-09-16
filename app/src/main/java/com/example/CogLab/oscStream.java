package com.example.CogLab;

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

public class oscStream extends AsyncTask<MbtEEGPacket, Void, Void>{


        private String melomind_headset_name;

        private String batLvl;

        /**
         * Manage distinct messages from distincts melomind devices
         *
         * @param deviceName
         */

        oscStream(String deviceName, String battLvl) {
            melomind_headset_name = "/"+ deviceName; // to manage distinct headset messages
            batLvl = battLvl;
        }

        @Override
        protected Void doInBackground(MbtEEGPacket... mbtEEGPackets) {


            String base_address = "/Coglab";  // file will be created each day or export button pushed


            for (MbtEEGPacket packet: mbtEEGPackets) {

                OSCPortOut osc_port;
                String myIP = "172.28.49.116";//"192.168.1.33";//"172.28.49.116";
                int myPort = 8080;//5000;

                try {
                    osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);

                    //com.illposed.osc.transport.udp.OSCPort{In, Out}
                    // adresse IP 172.28.49.29
                    packet.setChannelsData(invertFloatMatrix(packet.getChannelsData()));

                    //pour déterminer le casque utilisé et faire le traitement distant
                    OSCMessage messageHeadsetValue = new OSCMessage(base_address+melomind_headset_name);
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
