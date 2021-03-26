package com.cuelogic.android.nfc.comman;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import android.util.Log;

import com.cuelogic.android.nfc.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * This class provides a system log when user want to email a log from app
 */
public class Logger {
    private static File logFile, logServerFile;
    private static File lonerDirectory, lonerSendFileDirectory;
    public static final boolean isDebug = true;
    static long numberOfDay = 4;
    static String folderName = "/android-nfc";
    static String sendFileName = "/nfc-send-file";

    /**
     * This method creates a log text file and open email composer with attaching log text file
     */
    public static void sendLogs(Context context) {
        createLogFile(context);
        sendLogcatMail(context);
    }

    /**
     * This method creates a file and write log into a file once it start and stop monitoring
     */
    public static void WriteLog(Context context, String logs) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = dateFormat.format(date);
        String filename = "log" + "_" + dateFormat.format(date) + ".txt";
        lonerDirectory = new File(context.getExternalFilesDir(null) + folderName);
        if (!lonerDirectory.exists()) {
            lonerDirectory.mkdir();
        }
        logFile = new File(lonerDirectory, filename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                deleteLogsFile(currentTime, dateFormat);
                appendLogs("================================================START===========================================");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        appendLogs(logs);

    }

    public static void WriteLog(String logs) {
        appendLogs(logs);
    }

    public static void appendLogs(String logs) {
        try {
            //String currentDateTimeString = DateFormat.getDateTimeInstance(DateFormat.LONG,SimpleDateFormat.MEDIUM,Locale.ENGLISH).format(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH);
            String currentDateTimeString = sdf.format(Calendar.getInstance().getTime());
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.write("\n" + currentDateTimeString + " : " + logs);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method calculates a difference between two dates
     *
     * @return no of day
     */
    public static long printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        return different / daysInMilli;
    }

    /**
     * This method merges two text file into a single text file
     */
    public static void mergingLogsFile(String currentTime, SimpleDateFormat dateFormat) {
        try {
            File[] files = lonerDirectory.listFiles();
            File[] sortedFile = sortFilesByLastModDate(files);
            int size = sortedFile.length;
            for (int i = 0; i < size; i++) {
                if (sortedFile[i].isFile()) {
                    String Filename = sortedFile[i].getName();
                    String[] fileCreationName = Filename.split("_");
                    String filenametext = fileCreationName[1];
                    String datetimeFilename = filenametext.substring(0, filenametext.lastIndexOf("."));
                    Date creationalDate = dateFormat.parse(datetimeFilename);
                    Date currentDate = dateFormat.parse(currentTime);
                    long differnt = printDifference(creationalDate, currentDate);
                    if (differnt <= numberOfDay) {
                        String filePath = sortedFile[i].getAbsolutePath();
                        File fileIpunt = new File(filePath);
                        mergeFiles(fileIpunt);
                    } else {
                        sortedFile[i].delete();
                    }

                }
            }

        } catch (Exception e) {
        }

    }

    public static File[] sortFilesByLastModDate(File[] fList) {
        Arrays.sort(fList, new Comparator() {
            public int compare(final Object o1, final Object o2) {
                return new Long(((File) o1).lastModified()).compareTo
                        (new Long(((File) o2).lastModified()));
            }
        });
        return fList;
    }

    /**
     * This method delete a file which creation date had more than 5 day
     */
    public static void deleteLogsFile(String currentTime, SimpleDateFormat dateFormat) {
        try {
            for (File file : lonerDirectory.listFiles()) {
                if (file.isFile()) {
                    String Filename = file.getName();
                    String[] fileCreationName = Filename.split("_");
                    String filenametext = fileCreationName[1];
                    String datetimeFilename = filenametext.substring(0, filenametext.lastIndexOf("."));
                    Date creationalDate = dateFormat.parse(datetimeFilename);
                    Date currentDate = dateFormat.parse(currentTime);
                    long differnt = printDifference(creationalDate, currentDate);
                    if (differnt > numberOfDay) {
                        file.delete();
                    }

                }
            }
        } catch (Exception e) {
        }

    }

    /**
     * This method clear old system log from system buffer and delete old log text file
     */
    public static void clearAllLogs() {
        Logger.log("Android NFC", "Logger ::  clearAllLogs");
        if (lonerDirectory != null) {
            for (File file : lonerDirectory.listFiles()) file.delete();
        }

    }

    // This function delete sender file to server.
    public static void clearServerLogs() {
        Logger.log("Android NFC", "Logger ::  clearAllLogs");
        if (lonerSendFileDirectory != null) {
            for (File file : lonerSendFileDirectory.listFiles()) file.delete();
        }
    }


    /**
     * This method creates a log file and copies the system log file on it.
     */
    private static void createLogFile(Context context) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String versionRelease = Build.VERSION.RELEASE;
//        String deviceId = DataStore.getStringData(context, DataStore.DEVICE_ID, null);
//
//        String batterylevel = String.valueOf(Math.round(AndroidUtility.getBatteryLevel(LonerApplication.getAppContext())));
        String appLanguage;

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentTime = dateFormat.format(date);
        String filename = "log" + "_" + dateFormat.format(date) + ".txt";
        lonerDirectory = new File(context.getExternalFilesDir(null) + filename);
        if (!lonerDirectory.exists()) {
            lonerDirectory.mkdir();
        }
        logFile = new File(lonerDirectory, filename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        appendLogs("Android NFC, " + "createLogFile ::  *** Device Info **** " +
                        "\n App version = " + BuildConfig.VERSION_NAME
                        //+ " \n Device id = " + deviceId

                        //+ " \n App Language = " + DataStore.getStringData(context, DataStore.LANGUAGE_NAME, "English")
                        + " \n Device manufacturer = " + manufacturer
                        + " \n Device model = " + model
                        + " \n API version  = " + version
                        + " \n Os version  = " + versionRelease
                //+ " \n Phone battery = " + batterylevel
        );

        appendLogs("================================================END===========================================");
        // Prepare server log file
        lonerSendFileDirectory = new File(context.getExternalFilesDir(null) + sendFileName);
        if (!lonerSendFileDirectory.exists()) {
            lonerSendFileDirectory.mkdir();
        }
        logServerFile = new File(lonerSendFileDirectory, filename);
        clearServerLogs();
        if (!logServerFile.exists()) {
            try {
                logServerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mergingLogsFile(currentTime, dateFormat);

    }

    /**
     * This method open email composer with attached log file
     */
    public static void sendLogcatMail(Context context) {
        //Write logcat to file
        Log.d("Android NFC", "sending mail");
        // default email id Lm-490
        String[] TO = {"swapnil.sonar@cuelogic.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        Uri logFileUri = FileProvider.getUriForFile(context, "com.cuelogic.android.nfc.file.provider", logServerFile);

        emailIntent.putExtra(Intent.EXTRA_STREAM, logFileUri);
        emailIntent.setType("text/email");
//		emailIntent .setType("text/plain");
        //emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // set default email Lm-490
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        // set default msg Lm-490
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Kindly send the information");
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NFC Android-" + "Logs : " + logServerFile.getName());
        Logger.log("Android NFC", "Logger :: sending logcat mail");
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public static void log(String tag, String message) {
        if (isDebug) {
            Log.d(tag, message);
        }
    }

    // This function merge two text file into one file
    public static void mergeFiles(File files) {
        FileWriter fstream = null;
        BufferedWriter out = null;
        try {
            fstream = new FileWriter(logServerFile, true);
            out = new BufferedWriter(fstream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        FileInputStream fis;
        try {
            fis = new FileInputStream(files);
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));

            String aLine;
            while ((aLine = in.readLine()) != null) {
                out.write(aLine);
                out.newLine();
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

