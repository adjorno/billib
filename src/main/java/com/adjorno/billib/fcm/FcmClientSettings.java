package com.adjorno.billib.fcm;

import com.m14n.ex.Ex;

import de.bytefish.fcmjava.http.options.IFcmClientSettings;

public class FcmClientSettings implements IFcmClientSettings {

    private final String mUrl;
    private final String mApiKey;

    public FcmClientSettings(String url, String apiKey) {
        mUrl = url;
        mApiKey = apiKey;
    }

    @Override
    public String getFcmUrl() {
        return mUrl;
    }

    @Override
    public String getApiKey() {
        return mApiKey;
    }

    public static IFcmClientSettings createFromSysEnv() {
        final String theFCMUrl = System.getenv("FCM_URL");
        final String theFCMApiKey = System.getenv("FCM_API_KEY");
        if (Ex.isNotEmpty(theFCMApiKey) && Ex.isNotEmpty(theFCMUrl)) {
            return new FcmClientSettings(theFCMUrl, theFCMApiKey);
        }
        return null;
    }
}
