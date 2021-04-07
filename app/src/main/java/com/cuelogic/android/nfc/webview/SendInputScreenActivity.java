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

public class SendInputScreenActivity extends AppCompatActivity {

    private static final String TAG = SendInputScreenActivity.class.getSimpleName();
    private EditText edtDeviceName, edtEmpName;
    private Button btnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_screen);

        edtDeviceName = findViewById(R.id.edtDeviceName);
        edtEmpName = findViewById(R.id.edtEmpName);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtDeviceName.getText().toString().length() == 0) {
                    Toast.makeText(SendInputScreenActivity.this, "Please enter device name",
                            Toast.LENGTH_SHORT).show();
                } else if (edtEmpName.getText().toString().length() == 0) {
                    Toast.makeText(SendInputScreenActivity.this, "Please enter employee name",
                            Toast.LENGTH_SHORT).show();
                } else {
                    sendInputs();
                }
            }
        });
    }

    private void sendInputs() {
        String deviceName = edtDeviceName.getText().toString();
        String empName = edtEmpName.getText().toString();
        Log.e(TAG, "sendInputs:: deviceName=" + deviceName + "empName: " + empName);
        //WebScreenActivity
        Intent i = new Intent(SendInputScreenActivity.this, BlackLineWebScreenActivity.class);
        i.putExtra("deviceName", deviceName);
        i.putExtra("empName", empName);
        startActivity(i);
    }
}
