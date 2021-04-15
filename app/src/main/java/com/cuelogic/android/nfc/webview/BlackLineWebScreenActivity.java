package com.cuelogic.android.nfc.webview;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webscreen);
        LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: onCreate");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            DEVICE_ID = extras.getString("deviceName");
            EMP_ID = extras.getString("empName");
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
            LogUtils.printLogs(BlackLineWebScreenActivity.this, "BlackLineWebScreenActivity:: " +
                    "onPageFinished:: url=" + url);
            progressBar.setVisibility(View.GONE);

//            <input type="text" name="email" id="email" value="" placeholder="Email Address" class="" prepend="<i class=&quot;fa fa-user&quot;></i>">
//
//<input type="password" name="password" id="password" value="" placeholder="Password" class="" prepend="<i class=&quot;fa fa-lock&quot;></i>" autocomplete="off">
//
//<button name="loginBtn" id="loginBtn" type="submit" placement="prepend" class="submit-button btn btn-success">
//                    SIGN IN</button>

            if (url.equals(WEB_URL)) {
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
            } else if (url.equals(QUICK_ASSIGN_URL)) {


//                <input _ngcontent-uwc-c197="" autofocus="" id="deviceInput" matinput="" required="" class="mat-input-element mat-form-field-autofill-control ng-tns-c55-2 ng-pristine cdk-text-field-autofill-monitored ng-invalid ng-touched" data-placeholder="Device" aria-invalid="true" aria-required="true" aria-describedby="mat-hint-0">
//
//<input _ngcontent-uwc-c197="" id="employeeInput" matinput="" required="" class="mat-input-element mat-form-field-autofill-control ng-tns-c55-6 ng-pristine ng-invalid cdk-text-field-autofill-monitored ng-touched" data-placeholder="Employee ID" aria-invalid="true" aria-required="true" aria-describedby="mat-hint-1">


//                String js = "javascript:var x =document.getElementById('deviceInput').value = '"
//                        + DEVICE_ID + "';var y=document.getElementById('employeeInput').value='"
//                        + EMP_ID + "';";

//                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput').value = '"
//                        + DEVICE_ID + "';" +
//                        "var y=document.getElementById('employeeInput').value='" + EMP_ID + "';" +
//                        "}, 10000)";

//                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
//                        "var element = angular.element(x);" +
//                        "element.val('" + DEVICE_ID + "';" +
//                        "element.triggerHandler('input');" +
//                        "var y=document.getElementById('employeeInput').value='" + EMP_ID + "';" +
//                        "}, 10000)";


//                String javascript = "javascript:setTimeout(function () { $scope.$apply(function() {" +
//                        "$('#deviceInput').val('" + DEVICE_ID + "');" +
//                        "});" +
////                        "var x =document.getElementById('deviceInput');" +
////                        "var element = angular.element(x);" +
////                        "element.val('" + DEVICE_ID + "';" +
////                        "element.triggerHandler('input');" +
////                        "var y=document.getElementById('employeeInput').value='" + EMP_ID + "';" +
//                        "}, 10000)";

//                $scope.$apply(function() {
//                    // every changes goes here
//                    $('#selectedDueDate').val(dateText);
//                });


//                element = document.getElementById('deviceInput')
//                element.value = "800000262"
//                element.dispatchEvent(new Event('input', {bubbles:true }))
//                true
//                element = document.getElementById('employeeInput')
//                element.value = "12345"
//                element.dispatchEvent(new Event('input', {bubbles:true }))


//                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
//                        "x.value = '" + DEVICE_ID + "';" +
//                        "x.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "var y=document.getElementById('employeeInput');" +
//                        "y.value='" + EMP_ID + "';" +
//                        "y.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "}, 10000)";


//                var x = document.getElementById('deviceInput');
//
//                x.value = '800000534';
//
//                var val = x.value;
//                x.value = '';
//                x.value = val;
//
//                x.dispatchEvent(new Event('input', {
//                        bubbles:true
//  }));
//
//                var y = document.getElementById('employeeInput');
//                y.focus();
//                y.value = '12345';
//
//                var val1 = y.value;
//                y.value = '';
//                y.value = val1;
//
//                y.dispatchEvent(new Event('input', {
//                        bubbles:true
//  }));
//                document.getElementById('employeeInput').select();
//
//                y.dispatchEvent(new Event('keydown', {
//                        bubbles:true, cancelable:true, keyCode:13
//  }));


//                y.dispatchEvent(new KeyboardEvent('keyup', {'keyCode': 13}));

//                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
//                        "x.focus();" +
//                        "x.value = '" + DEVICE_ID + "';" +
//                        "var val = x.value; x.value = '';x.value = val;" +
//                        "x.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "x.blur();" +
//                        "var y=document.getElementById('employeeInput');" +
//                        "y.focus();" +
//                        "y.value='" + EMP_ID + "';" +
//                        "var val1 = y.value; y.value = '';y.value = val1;" +
//                        "y.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "document.getElementById('employeeInput').select();" +
//                        "y.dispatchEvent(new Event('keydown', {bubbles:true, cancelable:true, keyCode:13}));" +
//                        "y.dispatchEvent(new KeyboardEvent('keyup', {'keyCode': 13}));" +
//                        "y.dispatchEvent(new Event('touchstart'));" +
//                        "y.dispatchEvent(new Event('touchend'));" +
//                        "var e = y.createEvent('TouchEvent'); e.initTouchEvent();" +
//                        //"y.blur();" +
//                        "}, 10000)";

//                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
//                        "x.focus();" +
//                        "x.value = '" + DEVICE_ID + "';" +
//                        "var val = x.value; x.value = '';x.value = val;" +
//                        "x.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "x.blur();" +
//                        "var y=document.getElementById('employeeInput');" +
//                        "y.focus();" +
//                        "y.value='" + EMP_ID + "';" +
//                        "var val1 = y.value; y.value = '';y.value = val1;" +
//                        "y.dispatchEvent(new Event('input', {bubbles:true }));" +
//                        "y.select();" +
//                        "y.focus();" +
//                        "}, 10000)";


                String newJavascript = "<script type=\"text/javascript\">\n" +
                        "                        var app = angular.module('myApp', []);\n" +
                        "                app.controller('myCtrl', function($scope) {\n" +
                        "                    $scope.firstName = \"John\";\n" +
                        "                    $scope.lastName = \"Doe\";\n" +
                        "                    $scope.resetName = function() {\n" +
                        "                        $scope.firstName = \"John1\";\n" +
                        "                        $scope.lastName = \"Doe1\";\n" +
                        "                    }\n" +
                        "                });";

//                var x = document.getElementById('deviceInput');
//                x.value = '800000534';
//
//                x.dispatchEvent(new KeyboardEvent('keyup', {'keyCode':13}));
//
//                x.dispatchEvent(new Event('input', {
//                        bubbles:true
//  }));
//
//                x.blur();
//
//                var y = document.getElementById('employeeInput');
//
//                y.value = '12345';
//
//                y.dispatchEvent(new Event('input', {
//                        bubbles:true
//  }));
//
//                y.blur();
//
//                setTimeout(function() {
//                    y.select();
//                    y.focus();
//                },5000);


                String javascript = "javascript:setTimeout(function () {var x =document.getElementById('deviceInput');" +
                        "x.value = '" + DEVICE_ID + "';" +
                        "x.dispatchEvent(new KeyboardEvent('keyup', {'keyCode':13}));" +
                        "var val = x.value; x.value = '';x.value = val;" +
                        "x.dispatchEvent(new Event('input', {bubbles:true }));" +
                        "x.blur();" +
                        "var y=document.getElementById('employeeInput');" +
                        "y.value='" + EMP_ID + "';" +
                        "var val1 = y.value; y.value = '';y.value = val1;" +
                        "y.dispatchEvent(new Event('input', {bubbles:true }));" +
                        "y.blur();" +
                        "setTimeout(function() { y.select();y.focus(); },5000);" +
                        "y.dispatchEvent(new KeyboardEvent('keyup', {'keyCode':13}));" +
                        "}, 10000)";

                Log.e("onPageFinished", "javascript=" + javascript);
//                var selectedDueDateField = document.getElementById("selectedDueDate");
//                var element = angular.element(selectedDueDateField);
//                element.val('new value here');
//                element.triggerHandler('input');

                //https://stackoverflow.com/questions/3276794/jquery-or-pure-js-simulate-enter-key-pressed-for-testing

                if (Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript(javascript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            Log.e(TAG, "onReceiveValue=" + s);
                        }
                    });
                } else {
                    view.loadUrl(javascript);
                }
            }
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


    public class FetchWebResponse extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            URL url;
            HttpURLConnection urlConnection = null;
            JSONObject response = null;

            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                //HTTP header
                //urlConnection.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = urlConnection.getResponseCode();
                String responseMessage = urlConnection.getResponseMessage();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseString = readStream(urlConnection.getInputStream());
                    Log.v("Response", responseString);
                    response = parseBookData(responseString);
                } else {
                    Log.v("FetchWebResponse", "Response code:" + responseCode);
                    Log.v("FetchWebResponse", "Response message:" + responseMessage);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return response;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuffer response = new StringBuffer();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return response.toString();
        }


        private JSONObject parseBookData(String jString) {
            JSONObject response = new JSONObject();
            try {
                response = new JSONObject(jString);
            } catch (JSONException e) {
                Log.e("FetchWebResponse", "unexpected JSON exception", e);
            }
            return response;
        }

        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);
            Log.e("FetchWebResponse", "onPostExecute=" + response);
            Toast.makeText(BlackLineWebScreenActivity.this, "Configuration Successful", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
    }

}
