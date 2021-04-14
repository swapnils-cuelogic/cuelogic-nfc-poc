package com.cuelogic.android.nfc.webview;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.main.MainActivity;

public class SendInputScreenActivity extends AppCompatActivity {

    private static final String TAG = SendInputScreenActivity.class.getSimpleName();
    private EditText edtDeviceName, edtEmpName;
    private Button btnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_screen);
        LogUtils.printLogs(SendInputScreenActivity.this, "SendInputScreenActivity:: onCreate");

        edtDeviceName = findViewById(R.id.edtDeviceName);
        edtEmpName = findViewById(R.id.edtEmpName);
        btnSend = findViewById(R.id.btnSend);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String deviceName = extras.getString("deviceName");
            edtDeviceName.setText(deviceName);
            edtEmpName.setText(deviceName);
        } else {
            finish();
        }

        //TODO testing purpose
        edtDeviceName.setText("800000534");
        edtEmpName.setText("12345");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.printLogs(SendInputScreenActivity.this, "SendInputScreenActivity:: btnSend");
                if (edtDeviceName.getText().toString().length() == 0) {
                    String error = "Please enter device name";
                    Toast.makeText(SendInputScreenActivity.this, error, Toast.LENGTH_SHORT).show();
                    LogUtils.printLogs(SendInputScreenActivity.this, "SendInputScreenActivity:: btnSend: " + error);
                } else if (edtEmpName.getText().toString().length() == 0) {
                    String error = "Please enter employee name";
                    Toast.makeText(SendInputScreenActivity.this, error,
                            Toast.LENGTH_SHORT).show();
                    LogUtils.printLogs(SendInputScreenActivity.this, "SendInputScreenActivity:: btnSend: " + error);
                } else {
                    sendInputs();
                }
            }
        });
    }

    private void sendInputs() {
        LogUtils.printLogs(SendInputScreenActivity.this, "SendInputScreenActivity:: sendInputs");
        String deviceName = edtDeviceName.getText().toString();
        String empName = edtEmpName.getText().toString();
        Log.e(TAG, "sendInputs:: deviceName=" + deviceName + "empName: " + empName);
        LogUtils.printLogs(SendInputScreenActivity.this, "SendInputScreenActivity:: sendInputs: deviceName: "
                + deviceName + "empName: " + empName);
        //WebScreenActivity
        Intent i = new Intent(SendInputScreenActivity.this, BlackLineWebScreenActivity.class);
        i.putExtra("deviceName", deviceName);
        i.putExtra("empName", empName);
        startActivity(i);
    }
}
