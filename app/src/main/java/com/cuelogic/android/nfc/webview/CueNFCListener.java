package com.cuelogic.android.nfc.webview;

public interface CueNFCListener {

    void onNfcError(String error);

    void onNfcFound(String value);

    void onNfcInfo(String info);
}
