package com.cuelogic.android.nfc.webview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.api.JavaScriptReceiver;
import com.cuelogic.android.nfc.comman.Constants;
import com.cuelogic.android.nfc.comman.MainApplication;
import com.cuelogic.android.nfc.api.RequestInfo;
import com.cuelogic.android.nfc.api.ResponseInfo;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.main.MainActivity;
import com.cuelogic.android.nfc.main.ScanEmpActivity;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlackLineWebScreenActivity extends AppCompatActivity {

    private static final String TAG = BlackLineWebScreenActivity.class.getSimpleName();
    private WebView webView;
    private ProgressBar progressBar;
    private String WEB_URL = "https://live.blacklinesafety.com/sign-in?redirect_to=/ng/quick-assign";
    private String QUICK_ASSIGN_URL = "https://live.blacklinesafety.com/ng/quick-assign";

    private String EMAIL = "swapnil.sonar@cuelogic.com";
    private String PASSWORD = "Cuel0gic!";

    private String DEVICE_ID = "RT001100";
    private String EMP_ID = "Cue1100";

    private JavaScriptReceiver javaScriptReceiver;
    private String token;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webscreen);
        LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: onCreate");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            DEVICE_ID = extras.getString(Constants.DEVICE_ID);
            EMP_ID = extras.getString(Constants.EMP_ID);
            LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: Bundle: deviceName="
                    + DEVICE_ID + " empName=" + EMP_ID);
        } else {
            finish();
        }

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        javaScriptReceiver = new JavaScriptReceiver(this, new CueWebListener() {
            @Override
            public void onAssignDevice() {
                Log.e(TAG, "onAssignDevice");
                LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: onAssignDevice");
                LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                        "onAssignDevice: isRedirectHome=" + isRedirectHome);
                if (!isRedirectHome) navigateToHome();
                //assignDevice();
            }
        });
        webView.addJavascriptInterface(javaScriptReceiver, "JSReceiver");

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage message) {
                String response = message.message();
                Log.e(TAG, "onConsoleMessage:: response=" + response);
                LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                        "onConsoleMessage:: response=" + response);
                return true;
            }
        });

        webView.setWebViewClient(new MyWebClient());
        webView.loadUrl(QUICK_ASSIGN_URL);
        LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                "loadUrl=" + QUICK_ASSIGN_URL);
    }

    private boolean isActionPhpDone = false;
    private boolean isRedirected;

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onPageStarted:: url=" + url);
            LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                    "onPageStarted:: url=" + url);
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading:: url=" + url);
            LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                    "shouldOverrideUrlLoading:: url=" + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished:: url=" + url);
            LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                    "onPageFinished:: url=" + url);
            progressBar.setVisibility(View.GONE);
            if (url.equals(WEB_URL)) {
                String js = "javascript:var x =document.getElementById('email').value = '"
                        + EMAIL + "';var y=document.getElementById('password').value='"
                        + PASSWORD + "';document.getElementById('loginBtn').click();";
                LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                        "onPageFinished:: \njavascript=" + js);
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                                    "onPageFinished:: " + url + "\nonReceiveValue=" + s);
                        }
                    });
                } else {
                    view.loadUrl(js);
                }
            } else if (url.equals(QUICK_ASSIGN_URL)) {

                //TODO code before tech team inputs
//                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
//                        "x.value = '" + DEVICE_ID + "';" +
//                        "x.dispatchEvent(new KeyboardEvent('keyup', {'keyCode':13}));" +
//                        "var val = x.value; x.value = '';x.value = val;" +
//                        "x.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "x.blur();" +
//                        "var y=document.getElementById('employeeInput');" +
//                        "y.value='" + EMP_ID + "';" +
//                        "var val1 = y.value; y.value = '';y.value = val1;" +
//                        "y.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "y.blur();" +
//                        "setTimeout(function() { y.select();y.focus(); },5000);" +
//                        "y.dispatchEvent(new KeyboardEvent('keyup', {'keyCode':13}));" +
//                        "JSReceiver.assignDevice();" +
//                        "}, 10000)";

                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
                        "x.value = '" + DEVICE_ID + "';" +
                        //"x.dispatchEvent(new KeyboardEvent('keyup', {'keyCode':13}));" +
                        "x.dispatchEvent(new Event('input', {bubbles:true }));" +
                        "var y=document.getElementById('employeeInput');" +
                        "y.value='" + EMP_ID + "';" +
                        "y.dispatchEvent(new Event('input', {bubbles:true }));" +
                        "y.dispatchEvent(new KeyboardEvent('keydown', {code:'Enter',key:'Enter', view:window, bubbles:true, cancelable:true, keyCode:13}));" +
                        //"y.dispatchEvent(new KeyboardEvent('keypress', {code:'Enter',key:'Enter', view:window, bubbles:true, cancelable:true, keyCode:13}));" +
                        "y.dispatchEvent(new KeyboardEvent('keyup', {code:'Enter',key:'Enter', view:window, bubbles:true, cancelable:true, keyCode:13}));" +
                        "JSReceiver.assignDevice();" +
                        "}, 10000)";

                Log.e("Javascript", "javascript=" + javascript);
                LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                        "onPageFinished:: \njavascript=" + javascript);
                //https://stackoverflow.com/questions/3276794/jquery-or-pure-js-simulate-enter-key-pressed-for-testing

                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(javascript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                                    "onPageFinished:: " + url + "\nonReceiveValue=" + s);
                        }
                    });
                } else {
                    view.loadUrl(javascript);
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            //Log.e(TAG, "shouldInterceptRequest= WebURL=" + request.getUrl().toString());
            Map<String, String> result = request.getRequestHeaders();
            try {
                token = result.get("Authorization");
                //Log.e(TAG, "Token=" + token);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return super.shouldInterceptRequest(view, request);
        }

//        @Nullable
//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            Log.e(TAG, "shouldInterceptRequest= url");
//            Log.e(TAG, "shouldInterceptRequest= " + url);
//            return super.shouldInterceptRequest(view, url);
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void assignDevice() {
        RequestInfo info = new RequestInfo();

        RequestInfo.Device device = new RequestInfo.Device();
        RequestInfo.Spec deviceSpec = new RequestInfo.Spec();
        deviceSpec.desc = DEVICE_ID;
        device.spec = deviceSpec;
        info.device = device;

        RequestInfo.Employee employee = new RequestInfo.Employee();
        RequestInfo.Spec empSpec = new RequestInfo.Spec();
        empSpec.desc = EMP_ID;
        employee.spec = empSpec;
        info.employee = employee;

        //String token = "Bearer eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiemlwIjoiREVGIn0..jBEW0mE2SAYvacnWSjCEwA.J23OpPORtFLwLH21la8xmE5kX-smSebpIKzratwgal24ekOSoK0xV7zXVtaUOPJWCE326aRfC5Mz13N0UGKlYZzUjH6rAlgDg1_8KyZHytKBnZUyjUHnQkZJqKHAeqzS1tuGSDT4TOtqQRK9hC6o6n9puzUTXlHhw6Z5MxODwNBw-FLIjrXtMCQpjcBadlKT9dDv7GTRQqcD7XgPu_ixiKR5CX5-bgi3eW3mocsJAqN7uiqEIBpvhc2WKJVKWIeph6ViejSmTC3HZ8vfTYWQVNewU4VjMCkI7d49eWkOfFhqszT_TnHHzLhnzRJtnuTnqIwHiBjsUu_eYrlg_qwvrCuIbbnM5OXRYOv_rNRTdndOFSCeAUsChgyrj4eJEs9DblX35gLIur0PW3se2wpZzeyE5BWZcPsv2OXVKrpjjyYYGx-9G8KvmMjXIZ0tEyUbcgPFWfG7e0WUIruI-ypZ_NJyW2hdKMtT1JYZDzkiPs3BdO3V2Mc3fM1DL_XSLXX97IFYSOmDhfw4Om9cGksUHorpwSGbPxf5IfOblYwFD6r3hsMpXV5hgvTQd3DNq83lVtzXV9JSvRd2fDeN8IESTvW3yUP0r9lGpD9vsoh5P1XV_T-4X9Ns4zYSP0GsHpIHoP5e1UBK5Up5i3f5Z5uc91RxoaIIINsIK7cuUAc3RAl7u0riy-aMN4WLY5kheBF0.LBZjpIQgT012-k-CrvhHVg";
        MainApplication.apiManager.assignDevice(token, info, new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.e(TAG, "onResponse: " + response.body());
                if (null == response.body()) return;

                String body = response.body().toString();
                if (null == body) return;
                if (TextUtils.isEmpty(body)) return;

                Log.e(TAG, "onResponse=" + body);
                ResponseInfo responseInfo = new Gson().fromJson(body, ResponseInfo.class);
                String message = "Something went wrong";
                if (responseInfo.assigned) {
                    message = "Device assigned successfully";
                }
                Toast.makeText(BlackLineWebScreenActivity.this, message, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> runOnUiThread(() -> navigateToHome()), 200);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e(TAG, "onFailure=" + t);
                Toast.makeText(BlackLineWebScreenActivity.this,
                        "Error while device getting assigned. Please try again", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> runOnUiThread(() -> navigateToHome()), 200);
            }
        });
    }

    private boolean isRedirectHome;

    private void navigateToHome() {
        LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: navigateToHome");
        isRedirectHome = true;
        Toast.makeText(BlackLineWebScreenActivity.this, "Device assigned successfully",
                Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), ScanEmpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
