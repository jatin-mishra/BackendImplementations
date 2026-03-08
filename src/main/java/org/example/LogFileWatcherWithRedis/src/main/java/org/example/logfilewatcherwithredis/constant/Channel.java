package org.example.logfilewatcherwithredis.constant;

public enum Channel {
    logChannel("log:stream");

    private String channelName;

    Channel(String channelName){
        this.channelName = channelName;
    }

    public String getChannelName(){
        return this.channelName;
    }
}
