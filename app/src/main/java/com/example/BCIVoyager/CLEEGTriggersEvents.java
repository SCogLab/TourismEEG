package com.example.BCIVoyager;

import android.os.AsyncTask;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import core.eeg.storage.MbtEEGPacket;

import static utils.MatrixUtils.invertFloatMatrix;

/**
 * CLEEGTriggersEvents defines external software triggers outside headset
 * this class define an array of triggers representing a float array : 0.0 (min), 1.0 (max)
 *
 * note : the samplerate should not be another value than 250 (corresponding to Melomind
 * the samplerate)
 *
 */

@Keep
public final class CLEEGTriggersEvents extends AsyncTask<MbtEEGPacket, Void, Void> {

    @NonNull
    private ArrayList<TriggerEvent> triggersEvents;

    @NonNull
    private ArrayList<Long> eegTimestampList = new ArrayList<>();

    @NonNull
    private MbtEEGPacket triggersEventsEEGPackets;

    @NonNull
    private ArrayList<Long> eegTriggerValue = new ArrayList<>();

    private CLEEGTriggersEvents(ArrayList<TriggerEvent> triggerEvents, MbtEEGPacket triggersEventsEEGPackets) {

        this.triggersEvents = triggerEvents;
        this.triggersEventsEEGPackets = triggersEventsEEGPackets;

    }

    @NonNull
    public ArrayList<TriggerEvent> getTriggersEvents() {
        return triggersEvents;
    }

    @NonNull
    public MbtEEGPacket getTriggersEEGPackets() {
        return triggersEventsEEGPackets;
    }

    public void setTriggersEvents(@NonNull ArrayList<TriggerEvent> triggersEvents) {
        this.triggersEvents = triggersEvents;
    }

    public void setTriggersEventsEEGPackets(@NonNull MbtEEGPacket eegPacket) {
        this.triggersEventsEEGPackets = eegPacket;
    }

    @Override
    public String toString(){
        return "CLEEGTriggerEvents{"+"TriggersEvents = " + triggersEvents +
                "EEGPackets = " + triggersEventsEEGPackets+"}";
    }


    public boolean isEmpty(){
        return this.triggersEvents.isEmpty() && this.triggersEventsEEGPackets.isEmpty();
    }


    public void setEEGEventTrigger(long triggerValue, long timeStamp, MbtEEGPacket... eegPacket) {

        for(MbtEEGPacket eegPackets : eegPacket) {

            eegPackets.setChannelsData(invertFloatMatrix(eegPackets.getChannelsData()));

            for (int currentEegData = 0; currentEegData < eegPackets.getChannelsData().get(0).size(); currentEegData++) {

                eegPackets.getChannelsData().get(0).get(currentEegData);
                eegTimestampList.add(Long.valueOf(timeStamp));
                eegTriggerValue.add(Long.valueOf(triggerValue));
            }

        }
    }

    @Override
    protected Void doInBackground(MbtEEGPacket... eegPackets) {
        return null;
    }
}