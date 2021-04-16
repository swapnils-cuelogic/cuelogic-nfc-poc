package com.cuelogic.android.nfc.api;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.cuelogic.android.nfc.webview.CueWebListener;

public class JavaScriptReceiver {
    private static final String TAG = JavaScriptReceiver.class.getSimpleName();
    private Context mContext;
    private CueWebListener listener;

    /**
     * Instantiate the receiver and set the context
     */
    public JavaScriptReceiver(Context c, CueWebListener listener) {
        this.mContext = c;
        this.listener = listener;
    }

    @JavascriptInterface
    public void assignDevice() {
        Log.e(TAG, "assignDevice");
        listener.onAssignDevice();
    }

    @JavascriptInterface
    public void showOrders(int orderid) {

    }
} 