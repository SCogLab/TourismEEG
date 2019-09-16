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

public class StreamCasque extends AsyncTask<MbtEEGPacket, Void, Void> {


    @Override
    protected Void doInBackground(MbtEEGPacket... mbtEEGPackets) {
        Log.e("create","create");
        for (MbtEEGPacket packet: mbtEEGPackets) {


            OSCPortOut osc_port;
            String myIP = "192.168.1.70";
            int myPort = 5000;

            try {
                osc_port = new OSCPortOut(InetAddress.getByName(myIP), myPort);

                //com.illposed.osc.transport.udp.OSCPort{In, Out}
                // adresse IP 172.28.49.29
                try {

                    OSCMessage oscmessage = new OSCMessage("/imgID");
                    oscmessage.addArgument(packet.getQualities());
                    osc_port.send(oscmessage);
                    Log.d("osc message", oscmessage.toString());
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
                //packet.getChannelsData();

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }


        }

        return null;
    }
}
