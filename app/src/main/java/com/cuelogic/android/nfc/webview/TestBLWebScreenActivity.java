package com.cuelogic.android.nfc.webview;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.api.JavaScriptReceiver;
import com.cuelogic.android.nfc.api.RequestInfo;
import com.cuelogic.android.nfc.api.ResponseInfo;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.MainApplication;
import com.cuelogic.android.nfc.main.MainActivity;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestBLWebScreenActivity extends AppCompatActivity {

    private static final String TAG = TestBLWebScreenActivity.class.getSimpleName();
    private WebView webView;
    private ProgressBar progressBar;
    private String LOGIN_URL = "https://live-staging-20.blackline-dev.com/sign-in";
    private String REDIRECT_URL = "https://live-staging-20.blackline-dev.com/ng/devices";

    private String EMAIL = "devendra.jadhav@cuelogic.com";
    private String PASSWORD = "Cuelogic_staging20";

    private String DEVICE_ID = "RT001100";
    private String EMP_ID = "Cue1100";

    private JavaScriptReceiver javaScriptReceiver;
    private String token;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webscreen);
        LogUtils.printLogs(TestBLWebScreenActivity.this, "TestBLWebScreenActivity:: onCreate");

//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {
//            DEVICE_ID = extras.getString("deviceName");
//            EMP_ID = extras.getString("empName");
//            LogUtils.printLogs(TestBLWebScreenActivity.this, "TestBLWebScreenActivity:: Bundle: deviceName="
//                    + DEVICE_ID + " empName=" + EMP_ID);
//        } else {
//            finish();
//        }

        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        javaScriptReceiver = new JavaScriptReceiver(this, new CueWebListener() {
            @Override
            public void onAssignDevice() {
                Log.e(TAG, "onAssignDevice");
                assignDevice();
            }
        });
        webView.addJavascriptInterface(javaScriptReceiver, "JSReceiver");

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage message) {
                String response = message.message();
                Log.e(TAG, "onConsoleMessage:: response=" + response);
                LogUtils.printLogs(TestBLWebScreenActivity.this, "TestBLWebScreenActivity:: " +
                        "onConsoleMessage:: response=" + response);
                return true;
            }
        });

        webView.setWebViewClient(new MyWebClient());
        webView.loadUrl(REDIRECT_URL);
        LogUtils.printLogs(TestBLWebScreenActivity.this, "TestBLWebScreenActivity:: " +
                "loadUrl=" + REDIRECT_URL);
    }

    private boolean isActionPhpDone = false;

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onPageStarted:: url=" + url);
            LogUtils.printLogs(TestBLWebScreenActivity.this, "TestBLWebScreenActivity:: " +
                    "onPageStarted:: url=" + url);
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading:: url=" + url);
            LogUtils.printLogs(TestBLWebScreenActivity.this, "TestBLWebScreenActivity:: " +
                    "shouldOverrideUrlLoading:: url=" + url);
            view.loadUrl(url);
            return true;
//            if (isActionPhpDone) {
//                new FetchWebResponse().execute(url);
//                return true;
//            }
//            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished:: url=" + url);
            LogUtils.printLogs(TestBLWebScreenActivity.this, "TestBLWebScreenActivity:: " +
                    "onPageFinished:: url=" + url);
            progressBar.setVisibility(View.GONE);
            if (url.equals(LOGIN_URL)) {
                String js = "javascript:var x =document.getElementById('email').value = '"
                        + EMAIL + "';var y=document.getElementById('password').value='"
                        + PASSWORD + "';document.getElementById('loginBtn').click();";
                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                        }
                    });
                } else {
                    view.loadUrl(js);
                }
            } else if (url.equals(REDIRECT_URL)) {
                Log.e(TAG, "Token=" + token);
                if (null != token) {
                    finish();
                }
            }
        }

        String finalURL = "";

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String webURL = request.getUrl().toString();
            Log.e(TAG, "shouldInterceptRequest= WebURL=" + webURL);
            if (webURL.contains("me")) {
                Map<String, String> result = request.getRequestHeaders();
                try {
//                String[] arr = result.get("Authorization").split(" ");
//                String token = arr[1];
                    //Bearer eyJhbGciOiJka
                    token = result.get("Authorization");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return super.shouldInterceptRequest(view, request);

//            if (webURL.contains("rest/auth-token")) {// condition to intercept webview's request
//                return handleIntercept(request);
//            } else
//                return super.shouldInterceptRequest(view, request);

//            WebResourceResponse webResourceResponse = super.shouldInterceptRequest(view, request);
//            if (webURL.contains("rest/auth-token")) {
//                finalURL = request.getUrl().toString();
//                Log.e(TAG, "shouldInterceptRequest= finalURL=" + finalURL);
//                if (null != webResourceResponse) {
//                    InputStream stream = webResourceResponse.getData();
//                    if (null != stream) {
//                        String value = strResponse(stream);
//                        Log.e(TAG, "shouldInterceptRequest= authCode=" + value);
//                    }
//                }
//            }
//            return webResourceResponse;
        }

//        @Nullable
//        @Override
//        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            Log.e(TAG, "shouldInterceptRequest= url");
//            Log.e(TAG, "shouldInterceptRequest= " + url);
//            return super.shouldInterceptRequest(view, url);
//        }
    }


    public String strResponse(InputStream inputStream) {
        StringBuffer string = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                string.append(line + "\n");
            }
        } catch (IOException e) {
        }
        return string.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private WebResourceResponse handleIntercept(WebResourceRequest request) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final okhttp3.Call call = okHttpClient.newCall(new Request.Builder()
                .url(request.getUrl().toString())
                .method(request.getMethod(), null)
                .headers(Headers.of(request.getRequestHeaders()))
                .build()
        );
        try {
            final okhttp3.Response response = call.execute();// get response header here
            return new WebResourceResponse(
                    response.header("Connection", "keep-alive"), // You can set something other as default content-type
                    response.header("Content-Type", "application/json"),  //you can set another encoding as default
                    response.body().byteStream()
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
                Toast.makeText(TestBLWebScreenActivity.this, message, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> runOnUiThread(() -> navigateToHome()), 200);
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e(TAG, "onFailure=" + t);
                Toast.makeText(TestBLWebScreenActivity.this,
                        "Error while device getting assigned. Please try again", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(() -> runOnUiThread(() -> navigateToHome()), 200);
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
