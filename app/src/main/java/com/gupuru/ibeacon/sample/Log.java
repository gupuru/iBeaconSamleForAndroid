package com.gupuru.ibeacon.sample;

import io.realm.RealmObject;

public class Log extends RealmObject {

    private String log;
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

}
