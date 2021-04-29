package com.cuelogic.android.nfc.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.api.ConsoleResponse;
import com.cuelogic.android.nfc.api.JavaScriptReceiver;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.PreferencesHelper;
import com.cuelogic.android.nfc.comman.Toaster;
import com.cuelogic.android.nfc.webview.CueWebListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

public class LoginScreenActivity extends BaseActivity {

    private static final String TAG = LoginScreenActivity.class.getSimpleName();

    private WebView webView;
    private ProgressBar progressBar;
    private EditText edtEmail, edtPassword;
    private Button btnLogin;

    private JavaScriptReceiver javaScriptReceiver;
    private String token;
//    private String WEB_URL = "https://live.blacklinesafety.com/sign-in?redirect_to=/ng/devices";
//    private String REDIRECT_URL = "https://live.blacklinesafety.com/ng/devices";


    //https://live.blacklinesafety.com/sign-in
    //https://live.blacklinesafety.com/dashboard
    private String WEB_URL = "https://live.blacklinesafety.com/sign-in";
    private String REDIRECT_URL = "https://live.blacklinesafety.com/dashboard";

    private String EMAIL = "swapnil.sonar@cuelogic.com";
    private String PASSWORD = "Cuel0gic!";

    private String accessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUI();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUI() {
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        edtEmail = findViewById(R.id.edtEmail);
        edtEmail.setText(EMAIL);
        edtPassword = findViewById(R.id.edtPassword);
        edtPassword.setText(PASSWORD);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtEmail.getText().toString().length() == 0) {
                    Toaster.showShort(LoginScreenActivity.this, "Please check the email address");
                } else if (edtPassword.getText().toString().length() == 0) {
                    Toaster.showShort(LoginScreenActivity.this, "Please check the password");
                } else {
                    doLogin();
                }
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        javaScriptReceiver = new JavaScriptReceiver(this, new CueWebListener() {
            @Override
            public void onAssignDevice() {
                Log.e(TAG, "onAssignDevice");
//                LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: onAssignDevice");
//                LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
//                        "onAssignDevice: isRedirectHome=" + isRedirectHome);
                //if (!isRedirectHome) navigateToHome();
                //assignDevice();
            }

            @Override
            public void onTokenReceived(String token) {
                Log.e(TAG, "onTokenReceived= " + token);
                progressBar.setVisibility(View.GONE);
                if (null != token && !token.equals(accessToken)) {
                    accessToken = token;
                    Toaster.showShort(LoginScreenActivity.this, "Login Successfully");
                    PreferencesHelper.getSharedPreferences(LoginScreenActivity.this)
                            .setAccessToken(accessToken);
                    startActivity(new Intent(LoginScreenActivity.this, ScanEmpActivity.class));
                }
            }
        });
        webView.addJavascriptInterface(javaScriptReceiver, "JSReceiver");

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage message) {
                String response = message.message();
                Log.e(TAG, "onConsoleMessage:: response=" + response);
                return true;
            }
        });
    }

    private void doLogin() {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        Log.e(TAG, "Email= " + email + " Password= " + password);

        progressBar.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new LoginWebClient(email, password));
        webView.loadUrl(WEB_URL);
        LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
                "loadUrl=" + WEB_URL);
    }

    private void loginSuccess(String token) {
        progressBar.setVisibility(View.GONE);
        Log.e(TAG, "Login successfully\n" + token);
    }

    private class LoginWebClient extends WebViewClient {

        private String email, password;

        public LoginWebClient(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onPageStarted:: url=" + url);
            LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
                    "onPageStarted:: url=" + url);
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading:: url=" + url);
            LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
                    "shouldOverrideUrlLoading:: url=" + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished:: url=" + url);
            LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
                    "onPageFinished:: url=" + url);
            if (url.equals(WEB_URL)) {
                String js = "javascript:var x =document.getElementById('email').value = '"
                        + email + "';var y=document.getElementById('password').value='"
                        + password + "';document.getElementById('loginBtn').click();";
                LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
                        "onPageFinished:: \njavascript=" + js);
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
                                    "onPageFinished:: " + url + "\nonReceiveValue=" + s);
                        }
                    });
                } else {
                    view.loadUrl(js);
                }
            } else if (url.equals(REDIRECT_URL)) {
                String js = "javascript:var x = User.token;" +
                        "JSReceiver.sendToken(x);";
                Log.e(TAG, js);
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(LoginScreenActivity.this, "LoginScreenActivity:: " +
                                    "onPageFinished:: " + url + "\nonReceiveValue=" + s);
                        }
                    });
                } else {
                    view.loadUrl(js);
                }
//                Log.e(TAG, "Token=" + token);
//                if (null != token) loginSuccess(token);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String webURL = request.getUrl().toString();
            Log.e(TAG, "shouldInterceptRequest= WebURL=" + webURL);
            if (webURL.contains("me")) {
                Map<String, String> result = request.getRequestHeaders();
                try {
                    token = result.get("Authorization");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }
}
