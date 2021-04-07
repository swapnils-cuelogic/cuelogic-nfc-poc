package com.cuelogic.android.nfc.poc3;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.cuelogic.android.nfc.R;
import com.cuelogic.android.nfc.comman.LogUtils;
import com.cuelogic.android.nfc.poc2.Poc2MainActivity;
import com.cuelogic.android.nfc.poc2.nfc.Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class MainTestActivity extends AppCompatActivity {

    //initialize attributes
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    final static String TAG = MainTestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: onCreate");
        //initialise NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        //If no NfcAdapter, display that the device has no NFC
        if (nfcAdapter == null) {
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: No NFC Capabilities");
            Toast.makeText(this, "NO NFC Capabilities",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        //Create a PendingIntent object so the Android system can
        //populate it with the details of the tag when it is scanned.
        //PendingIntent.getActivity(Context,requestcode(identifier for
        //                           intent),intent,int)

        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: sending the pending intent");
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        if (null != getIntent() && null != getIntent().getExtras()) {
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: parsing intent");
            Bundle extras = getIntent().getExtras();
            String nfcKey = extras.getString("NFCKey");
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: nfcKey=" + nfcKey);
            if (null != nfcKey) {
                showAlertPopup(nfcKey);
            }
        }

    }

    private void showAlertPopup(String nfcKey) {
        String content = "We have found NFC TAG " + nfcKey + "\nWe trying to fetch more details about it!";
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainTestActivity.this);
        builder1.setMessage(content);
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertPopup = builder1.create();
        alertPopup.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;
        //nfcAdapter.enableForegroundDispatch(context,pendingIntent,
        //                                    intentFilterArray,
        //                                    techListsArray)
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: onResume");
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    protected void onPause() {
        super.onPause();
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: onPause");
        //on-pause stop listening
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: resolveIntent");
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            assert tag != null;
            byte[] payload = detectTagData(tag).getBytes();
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: payload=" + payload);
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: payload length=" + payload.length);
            if (payload.length > 0) {
                try {
                    String text = new String(payload, "UTF-8");
                    LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: trying to convert payload to text="
                            + text);
                    sendNotification(tag);
                } catch (UnsupportedEncodingException e) {
                    LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: UnsupportedEncodingException="
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendNotification(Tag tag) {
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: sendNotification");
        Intent intent = new Intent(MainTestActivity.this, MainTestActivity.class);

        byte[] id = tag.getId();
        String hexValue = toHex(id);
        intent.putExtra("NFCKey", hexValue);

        PendingIntent contentIntent = PendingIntent.getActivity(MainTestActivity.this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = "NFC TAG Detected";
        String content = "We have found NFC TAG " + hexValue + "\nPlease tap on it see more information";

        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: sendNotification, " +
                "title=" + title);
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: sendNotification, " +
                "content=" + content);

        NotificationCompat.Builder b = new NotificationCompat.Builder(MainTestActivity.this);
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo(content);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }

    //For detection
    private String detectTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

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
        Log.v(TAG, sb.toString());
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: detectTagData");
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: detectTagData, " +
                "output=" + sb.toString());
        return sb.toString();
    }

    //For reading and writing
//    private String detectTagData(Tag tag) {
//        StringBuilder sb = new StringBuilder();
//        byte[] id = tag.getId();
//        sb.append("NFC ID (dec): ").append(toDec(id)).append('\n');
//        for (String tech : tag.getTechList()) {
//            if (tech.equals(MifareUltralight.class.getName())) {
//                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
//                String payload;
//                payload = readTag(mifareUlTag);
//                sb.append("payload: ");
//                sb.append(payload);
//                writeTag(mifareUlTag);
//            }
//        }
//    Log.v("test",sb.toString());
//    return sb.toString();
//}
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        String output = sb.toString();
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: toHex=" + output);
        return output;
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        String output = sb.toString();
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: toReversedHex=" + output);
        return output;
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: toDec=" + result);
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: toReversedDec=" + result);
        return result;
    }

    public void writeTag(MifareUltralight mifareUlTag) {
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: writeTag");
        try {
            mifareUlTag.connect();
            mifareUlTag.writePage(4, "get ".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(5, "fast".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(6, " NFC".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(7, " now".getBytes(Charset.forName("US-ASCII")));
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight...", e);
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                    "IOException while writing MifareUltralight...");
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                    e.getLocalizedMessage());
        } finally {
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: writeTag finally");
            try {
                mifareUlTag.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
                LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                        "IOException while closing MifareUltralight...");
                LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                        e.getLocalizedMessage());
            }
        }
    }

    public String readTag(MifareUltralight mifareUlTag) {
        LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: readTag");
        try {
            mifareUlTag.connect();
            byte[] payload = mifareUlTag.readPages(4);
            return new String(payload, Charset.forName("US-ASCII"));
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading MifareUltralight message...", e);
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                    "IOException while reading MifareUltralight message...");
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                    e.getLocalizedMessage());
        } finally {
            LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: readTag finally");
            if (mifareUlTag != null) {
                try {
                    mifareUlTag.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                    LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                            "IOException while reading MifareUltralight message...");
                    LogUtils.printLogs(MainTestActivity.this, "MainTestActivity:: " +
                            e.getLocalizedMessage());
                }
            }
        }
        return null;
    }
}