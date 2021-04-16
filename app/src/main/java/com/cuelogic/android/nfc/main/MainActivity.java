package com.cuelogic.android.nfc.main;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.comman.Utils;
import com.cuelogic.android.nfc.parse.NdefMessageParser;
import com.cuelogic.android.nfc.parse.ParsedNdefRecord;
import com.cuelogic.android.nfc.webview.BlackLineWebScreenActivity;
import com.cuelogic.android.nfc.webview.SendInputScreenActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView text, tvTagInfo;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String output;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.printLogs(MainActivity.this, "MainActivity:: onCreate");

        text = (TextView) findViewById(R.id.text);
        tvTagInfo = findViewById(R.id.tvTagInfo);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            LogUtils.printLogs(MainActivity.this, "MainActivity:: No NFC Feature found");
            //Toast.makeText(this, "No NFC Feature found", Toast.LENGTH_SHORT).show();
            text.setText("No NFC Feature found");
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

            LogUtils.printLogs(MainActivity.this, "MainActivity:: NFC enabled");
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void showWirelessSettings() {
        LogUtils.printLogs(MainActivity.this, "MainActivity:: You need to enable NFC");
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        }
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        LogUtils.printLogs(MainActivity.this, "MainActivity:: onNewIntent");
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        LogUtils.printLogs(MainActivity.this, "MainActivity:: resolveIntent");
        String action = intent.getAction();
        LogUtils.printLogs(MainActivity.this, "MainActivity:: action=" + action);

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
                LogUtils.printLogs(MainActivity.this, "MainActivity:: output=" + output);
                tvTagInfo.setText(output);

                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }

            displayMsgs(msgs);
        }
    }

    private void displayMsgs(NdefMessage[] msgs) {
        LogUtils.printLogs(MainActivity.this, "MainActivity:: displayMsgs");
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();

        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            String str = record.str();
            builder.append(str).append("\n");
        }

        String output = builder.toString();
        LogUtils.printLogs(MainActivity.this, "MainActivity:: builder=" + output);
        text.setText(output);
        navigate();
    }

    public void onShareDebugLogs(View view) {
        LogUtils.printLogs(MainActivity.this, "MainActivity:: onShareDebugLogs");
        LogUtils.emailLogs(MainActivity.this);
    }

    public void onSendInputs(View view) {
        LogUtils.printLogs(MainActivity.this, "MainActivity:: onSendInputs");
        navigate();
    }

    private void navigate() {
        LogUtils.printLogs(MainActivity.this, "MainActivity:: navigate: output=" + output);
        Intent intent = new Intent(MainActivity.this, SendInputScreenActivity.class);
        intent.putExtra("deviceName", output);
        startActivity(intent);
    }

    private String dumpTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(Utils.toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(Utils.toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(Utils.toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(Utils.toReversedDec(id)).append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }
}
