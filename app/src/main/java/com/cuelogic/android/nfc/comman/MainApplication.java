package com.cuelogic.android.nfc.comman;

import android.app.Application;

import com.cuelogic.android.nfc.api.ApiManager;

public class MainApplication extends Application {

    public static ApiManager apiManager;

    @Override
    public void onCreate() {
        super.onCreate();
        apiManager = ApiManager.getInstance();
    }
}