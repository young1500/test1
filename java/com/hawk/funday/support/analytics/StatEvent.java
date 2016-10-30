package com.hawk.funday.support.analytics;

public interface StatEvent {

    interface UserProperty {
        String LANGUAGE = "language";
        String CHANNEL = "channel";
        String IMEI = "imei";
        String SCREEN = "screen";
    }

    interface CommonParams {
        String NETWORK = "network";
    }

}
