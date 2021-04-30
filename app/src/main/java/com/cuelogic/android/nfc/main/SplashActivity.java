package com.cuelogic.android.nfc.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.PreferencesHelper;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        LogUtils.printLogs(SplashActivity.this, "SplashActivity:: onCreate");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String accessToken = PreferencesHelper.getSharedPreferences(SplashActivity.this).getAccessToken();
                        LogUtils.printLogs(SplashActivity.this, "SplashActivity:: accessToken=" + accessToken);
                        Class activity = ScanEmpActivity.class;
                        if (null == accessToken) activity = LoginScreenActivity.class;
                        startActivity(new Intent(SplashActivity.this, activity));
                        finish();
                    }
                });
            }
        }, 2000);
    }
}
