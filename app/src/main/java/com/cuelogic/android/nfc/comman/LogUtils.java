package com.cuelogic.android.nfc.comman;

import android.content.Context;

public class LogUtils {

    public static void printLogs(Context context, String msg) {
        Logger.WriteLog(context, msg);
    }

    public static void printLogs(String msg) {
        Logger.WriteLog(msg);
    }

    public static void emailLogs(Context context) {
        Logger.sendLogs(context);
    }
}
