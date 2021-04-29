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
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: onCreate");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            DEVICE_ID = extras.getString(Constants.DEVICE_ID);
            EMP_ID = extras.getString(Constants.EMP_ID);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: Bundle: deviceName="
                    + DEVICE_ID + " empName=" + EMP_ID);
        } else {
            finish();
        }

        webView = findViewById(R.id.webview);
        webView.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.progressBar);

        RelativeLayout layout = findViewById(R.id.screenContent);
        layout.setVisibility(View.VISIBLE);

        tvScan = findViewById(R.id.tvScan);
        tvScan.setText("Device assigning.. Please wait");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        javaScriptReceiver = new JavaScriptReceiver(this, new CueWebListener() {
            @Override
            public void onAssignDevice() {
                Log.e(TAG, "onAssignDevice");
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: onAssignDevice");
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
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
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
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
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
                "loadUrl=" + QUICK_ASSIGN_URL);
    }

    private boolean isActionPhpDone = false;
    private boolean isRedirected;

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onPageStarted:: url=" + url);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
                    "onPageStarted:: url=" + url);
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading:: url=" + url);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
                    "shouldOverrideUrlLoading:: url=" + url);
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished:: url=" + url);
            LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
                    "onPageFinished:: url=" + url);
            progressBar.setVisibility(View.GONE);
            if (url.equals(WEB_URL)) {
                String js = "javascript:var x =document.getElementById('email').value = '"
                        + EMAIL + "';var y=document.getElementById('password').value='"
                        + PASSWORD + "';document.getElementById('loginBtn').click();";
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
                        "onPageFinished:: \njavascript=" + js);
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
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

//                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
//                        "x.value = '" + DEVICE_ID + "';" +
//                        //"x.dispatchEvent(new KeyboardEvent('keyup', {'keyCode':13}));" +
//                        "x.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "var y=document.getElementById('employeeInput');" +
//                        "y.value='" + EMP_ID + "';" +
//                        "y.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "y.dispatchEvent(new KeyboardEvent('keydown', {code:'Enter',key:'Enter', view:window, bubbles:true, cancelable:true, keyCode:13}));" +
//                        //"y.dispatchEvent(new KeyboardEvent('keypress', {code:'Enter',key:'Enter', view:window, bubbles:true, cancelable:true, keyCode:13}));" +
//                        "y.dispatchEvent(new KeyboardEvent('keyup', {code:'Enter',key:'Enter', view:window, bubbles:true, cancelable:true, keyCode:13}));" +
//                        "JSReceiver.assignDevice();" +
//                        "}, 10000)";


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
                        "}, 10000)";


                Log.e("Javascript", "javascript=" + javascript);
                LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
                        "onPageFinished:: \njavascript=" + javascript);
                //https://stackoverflow.com/questions/3276794/jquery-or-pure-js-simulate-enter-key-pressed-for-testing

                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(javascript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                            LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: " +
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
                Toast.makeText(AssignDeviceScreenActivity.this, message, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> runOnUiThread(() -> navigateToHome()), 200);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e(TAG, "onFailure=" + t);
                Toast.makeText(AssignDeviceScreenActivity.this,
                        "Error while device getting assigned. Please try again", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> runOnUiThread(() -> navigateToHome()), 200);
            }
        });
    }

    private boolean isRedirectHome;

    private void navigateToHome() {
        LogUtils.printLogs(AssignDeviceScreenActivity.this, "BlackLineWebScreenActivity:: navigateToHome");
        isRedirectHome = true;
        tvScan.setText("Device Assigned!");
        Toast.makeText(AssignDeviceScreenActivity.this, "Device assigned successfully",
                Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), ScanEmpActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        }, 1500);
    }

}
