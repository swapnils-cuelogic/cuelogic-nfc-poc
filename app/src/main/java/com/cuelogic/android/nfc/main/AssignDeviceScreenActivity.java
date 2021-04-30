package com.cuelogic.android.nfc.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.api.ConsoleResponse;
import com.cuelogic.android.nfc.api.JavaScriptReceiver;
import com.cuelogic.android.nfc.api.RequestInfo;
import com.cuelogic.android.nfc.api.ResponseInfo;
import com.cuelogic.android.nfc.comman.Constants;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.MainApplication;
import com.cuelogic.android.nfc.webview.CueWebListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//https://jsfiddle.net/swapnilsonar/w0c7s342/3/
public class AssignDeviceScreenActivity extends AppCompatActivity {

    private static final String TAG = AssignDeviceScreenActivity.class.getSimpleName();
    private WebView webView;
    private ProgressBar progressBar;
    private TextView tvScan;
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
        setContentView(R.layout.activity_device_aasigned);
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: onCreate");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            DEVICE_ID = extras.getString(Constants.DEVICE_ID);
            EMP_ID = extras.getString(Constants.EMP_ID);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                    "Bundle: deviceName=" + DEVICE_ID + " empName=" + EMP_ID);
        } else {
            finish();
        }

        webView = findViewById(R.id.webview);
        webView.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.progressBar);

        View layout = findViewById(R.id.screenContent);
        layout.setVisibility(View.VISIBLE);

        tvScan = findViewById(R.id.tvScan);
        String text = "Processing..";
        tvScan.setText(text);
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                "tvScan: " + text);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        javaScriptReceiver = new JavaScriptReceiver(this, new CueWebListener() {
            @Override
            public void onAssignDevice() {
                Log.e(TAG, "onAssignDevice");
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: onAssignDevice");
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                        "onAssignDevice: isRedirectHome=" + isRedirectHome);
                //if (!isRedirectHome) navigateToHome();
                //assignDevice();
            }

            @Override
            public void onTokenReceived(String token) {

            }
        });
        webView.addJavascriptInterface(javaScriptReceiver, "JSReceiver");

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage message) {
                String response = message.message();
                Log.v(TAG, "onConsoleMessage:: response=" + response);
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                        "onConsoleMessage:: response=" + response);
                try {
                    String value = response.replace("Resp::  ", "");
                    ConsoleResponse responseInfo = new Gson().fromJson(value, ConsoleResponse.class);
                    Log.e(TAG, "ConsoleResponse=" + responseInfo.toString());
                    if (200 == responseInfo.status) {
                        Log.v(TAG, "ConsoleResponse= info=" + responseInfo.data.toString());
                        if (!isRedirectHome) navigateToHome();
                    } else {

                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        webView.setWebViewClient(new MyWebClient());
        webView.loadUrl(QUICK_ASSIGN_URL);
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                "loadUrl=" + QUICK_ASSIGN_URL);
    }

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onPageStarted:: url=" + url);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                    "onPageStarted:: url=" + url);
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading:: url=" + url);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                    "shouldOverrideUrlLoading:: url=" + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished:: url=" + url);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                    "onPageFinished:: url=" + url);
            if (url.equals(WEB_URL)) {
                String js = "javascript:var x =document.getElementById('email').value = '"
                        + EMAIL + "';var y=document.getElementById('password').value='"
                        + PASSWORD + "';document.getElementById('loginBtn').click();";
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                        "onPageFinished:: \njavascript=" + js);
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                                    "onPageFinished:: " + url + "\nonReceiveValue=" + s);
                        }
                    });
                } else {
                    view.loadUrl(js);
                }
            } else if (url.equals(QUICK_ASSIGN_URL)) {
                String javascript = "javascript:" +
                        "function addXMLRequestCallback(callback) {\n" +
                        "  var oldSend, i;\n" +
                        "  if (XMLHttpRequest.callbacks) {\n" +
                        "    // we've already overridden send() so just add the callback\n" +
                        "    XMLHttpRequest.callbacks.push(callback);\n" +
                        "  } else {\n" +
                        "    // create a callback queue\n" +
                        "    XMLHttpRequest.callbacks = [callback];\n" +
                        "    // store the native send()\n" +
                        "    oldSend = XMLHttpRequest.prototype.send;\n" +
                        "    // override the native send()\n" +
                        "    XMLHttpRequest.prototype.send = function () {\n" +
                        "      // call the native send()\n" +
                        "      oldSend.apply(this, arguments);\n" +
                        "\n" +
                        "      this.onreadystatechange = function (progress) {\n" +
                        "        for (i = 0; i < XMLHttpRequest.callbacks.length; i++) {\n" +
                        "          XMLHttpRequest.callbacks[i](progress);\n" +
                        "        }\n" +
                        "      };\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n" +
                        "\n" +
                        "addXMLRequestCallback(function (progress) {\n" +
                        "  if (typeof progress.srcElement.responseText != 'undefined'\n" +
                        "    && progress.srcElement.responseText != ''\n" +
                        "    && progress.srcElement.readyState == 4) {\n" +
                        "    var myObj = { status : progress.srcElement.status, data : JSON.parse(progress.srcElement.responseText) };\n" +
                        "    console.log('Resp:: ', JSON.stringify(myObj));\n" +
//                        "    console.log('Status:: ', progress.srcElement.status);\n" +
//                        "    console.log('Response:: ', progress.srcElement.responseText);\n" +
                        "  }\n" +
                        "});" +

                        "setTimeout(function () {var x =document.getElementById('deviceInput');" +
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
                        "}, 7000)";


                Log.e("Javascript", "javascript=" + javascript);
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                        "onPageFinished:: \njavascript=" + javascript);
                //https://stackoverflow.com/questions/3276794/jquery-or-pure-js-simulate-enter-key-pressed-for-testing

                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(javascript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
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
            Map<String, String> result = request.getRequestHeaders();
            try {
                token = result.get("Authorization");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return super.shouldInterceptRequest(view, request);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean isRedirectHome;

    private void navigateToHome() {
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: navigateToHome");
        progressBar.setVisibility(View.GONE);
        isRedirectHome = true;

        String text = "Device Assigned!";
        tvScan.setText(text);
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " +
                "tvScan: " + text);

        String result = "Device assigned successfully";
        Toast.makeText(AssignDeviceScreenActivity.this, result, Toast.LENGTH_LONG).show();
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: " + result);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.printLogs(AssignDeviceScreenActivity.this, "AssignDeviceScreenActivity:: navigating to ScanEmpActivity");
                        Intent intent = new Intent(getApplicationContext(), ScanEmpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        }, 1500);
    }

}
