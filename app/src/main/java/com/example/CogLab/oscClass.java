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

public class oscClass extends AsyncTask<MbtEEGPacket, Void, Void> {
    private String ip;
    private int port;
    public oscClass(){
        this.ip = "192.168.1.70";
        this.port = 5000;
    }
    public void sendOSC(String imgName){
        OSCPortOut osc_port;
        OSCMessage oscmessage = new OSCMessage("/cat");
        oscmessage.addArgument(imgName.charAt(0)+"");
        try {
            osc_port = new OSCPortOut(InetAddress.getByName(this.ip), this.port);
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

    }
    @Override
    protected Void doInBackground(MbtEEGPacket... mbtEEGPackets) {

        for (MbtEEGPacket packet : mbtEEGPackets) {


            OSCPortOut osc_port;

            try {
                osc_port = new OSCPortOut(InetAddress.getByName(this.ip), this.port);

                //com.illposed.osc.transport.udp.OSCPort{In, Out}
                // adresse IP 172.28.49.29
                try {

                    OSCMessage oscmessage = new OSCMessage("/eeg");
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
                packet.getChannelsData();

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }


        }
        return null;
    }


}
