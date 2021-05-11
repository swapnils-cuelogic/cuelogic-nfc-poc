package com.cuelogic.android.nfc.main;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.PreferencesHelper;

public class SplashActivity extends BaseActivity {

    private TextView tvAppLine;
    private String accessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        LogUtils.printLogs(SplashActivity.this, "SplashActivity:: onCreate");

        tvAppLine = findViewById(R.id.tvAppLine);
        tvAppLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //beep(150);
            }
        });

        accessToken = PreferencesHelper.getSharedPreferences(SplashActivity.this).getAccessToken();
        LogUtils.printLogs(SplashActivity.this, "SplashActivity:: accessToken=" + accessToken);

        //TODO for additional web condition
        if (null == accessToken) {
            clearWebPref();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Class activity = ScanEmpActivity.class;
                        if (null == accessToken) activity = LoginScreenActivity.class;
                        startActivity(new Intent(SplashActivity.this, activity));
                        finish();
                    }
                });
            }
        }, 2000);
    }

    private void clearWebPref() {
        LogUtils.printLogs(SplashActivity.this, "SplashActivity:: clearWebPref");
        // Clear all the Application Cache, Web SQL Database and the HTML5 Web Storage
        WebStorage.getInstance().deleteAllData();

        // Clear all the cookies
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }

        WebView webView = new WebView(SplashActivity.this);
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearSslPreferences();
    }
}
