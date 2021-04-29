package com.cuelogic.android.nfc.main;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.Constants;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.Utils;
import com.cuelogic.android.nfc.webview.SendInputScreenActivity;

public class ScanEmpActivity extends BaseActivity {

    private static final String TAG = ScanEmpActivity.class.getSimpleName();
    private TextView tvScan;
    private ImageView ivScan;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String output;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_employee);
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: onCreate");

        tvScan = (TextView) findViewById(R.id.tvScan);
        ivScan = findViewById(R.id.ivScan);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: No NFC Feature found");
            Toast.makeText(this, "No NFC Feature found", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //finish();
                }
            }, 2000);
            return;
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();

            LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: NFC enabled");
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: onNewIntent");
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: resolveIntent");
        String action = intent.getAction();
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: action=" + action);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;

            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                output = Utils.toHex(tag.getId());
                LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: output=" + output);

                byte[] payload = printTechInfo(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }

            output = showTagInfo(msgs);
            LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: builder=" + output);
            navigate();
        }
    }

    public void onSendInputs(View view) {
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: onSendInputs");
        navigate();
    }

    private void navigate() {
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: navigate: output=" + output);
        Intent intent = new Intent(ScanEmpActivity.this, ScanDeviceActivity.class);
        output = output.replaceAll("[\\n ]", "");
        intent.putExtra(Constants.EMP_ID, output);
        startActivity(intent);
    }

    public void onShareDebugLogs(View view) {
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: onShareDebugLogs");
        LogUtils.emailLogs(ScanEmpActivity.this);
    }

    public void onSendManual(View view) {
        LogUtils.printLogs(ScanEmpActivity.this, "ScanEmpActivity:: onSendManual");
        Intent intent = new Intent(ScanEmpActivity.this, SendInputScreenActivity.class); //SendInputScreenActivity
        intent.putExtra(Constants.EMP_ID, "12345");
        intent.putExtra(Constants.DEVICE_ID, "800000534");
        startActivity(intent);
    }
}
