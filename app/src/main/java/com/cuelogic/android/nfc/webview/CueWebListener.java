package com.cuelogic.android.nfc.webview;

public interface CueWebListener {
    void onAssignDevice();

    void onTokenReceived(String token);
}
