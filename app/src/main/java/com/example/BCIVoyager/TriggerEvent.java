package com.example.BCIVoyager;

import androidx.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * TriggerEvent to manage triggers in EEGpacket :
 *
 * - long timestamp in milliseconds or nanoseconds (trigger timestamp)
 * - trigger name (e.g : Event1, etc)
 * - trigger type (e.g : trigger Append P300)
 * - trigger value (0.0 min et max 1.0)
 * - triggerDate from timestamp
 *
 */
public class TriggerEvent {

    /**
     * in millis or nanoseconds (e.g :
     * time in nanoseconds = 255073580723571
     * time in milliseconds = 1349311227921)
     */
    private long timeStamp;

    private String triggerEventName;

    private String triggerType; //e.g : Append P300 event type

    private float triggerEventValue; // 0.0 no trigger 1.0, one trigger

    private String triggerDate;

    public TriggerEvent(@Nullable String name, @Nullable String type, float value, long timestamp ) {
        this.triggerEventName = name;
        this.triggerType = type;
        this.triggerEventValue = value;
        this.timeStamp = timestamp;
    }


    private String getTriggerEventName(){
        return this.triggerEventName;
    }

    private void setTriggerEventName(String name) {
        this.triggerEventName = name;
    }

    private String getTriggerType() {

        return this.triggerType;

    }

    private void setTriggerType(String type) {
        this.triggerType = type;
    }

    private long getTimeStamp() {
        return this.timeStamp;
    }

    private void setTimeStamp(long timestamp) {
        this.timeStamp = timestamp;
    }

    private void setDate(String date){
        this.triggerDate = date;
    }

    private String getDate(long timeStamp) {

        triggerDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeStamp));

        return triggerDate;

    }
}
