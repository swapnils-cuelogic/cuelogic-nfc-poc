package com.cuelogic.android.nfc.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cuelogic.android.nfc.comman.PreferencesHelper;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accessToken = PreferencesHelper.getSharedPreferences(this).getAccessToken();

        Class activity = ScanEmpActivity.class;
        if (null == accessToken) activity = LoginScreenActivity.class;
        startActivity(new Intent(SplashActivity.this, activity));
        finish();
    }
}
