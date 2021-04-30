package com.cuelogic.android.nfc.comman;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * This singleton class is used as PreferencesHelper to store and retrieve the data values.
 */
public class PreferencesHelper {

    private static final String TAG = PreferencesHelper.class.getSimpleName();

    // shared preferences
    private static final String APP_PREFERENCES = "CueNFCPrefs";
    private static PreferencesHelper rmSharedPreferences;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor prefsEditor;

    private final String ACCESS_TOKEN = "accessToken";

    private PreferencesHelper() {
        // EMPTY CONSTRUCTOR
    }

    public static PreferencesHelper getSharedPreferences(Context context) {
        if (rmSharedPreferences == null) {
            rmSharedPreferences = new PreferencesHelper();
            sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            prefsEditor = sharedPreferences.edit();
        }
        return rmSharedPreferences;
    }

    public void setAccessToken(String accessToken) {
        prefsEditor.putString(ACCESS_TOKEN, accessToken);
        prefsEditor.commit();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
