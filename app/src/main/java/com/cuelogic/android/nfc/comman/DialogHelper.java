package com.cuelogic.android.nfc.comman;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;

import com.cuelogic.android.nfc.R;

public class DialogHelper {

    private static Dialog dialog;
    private static ProgressDialog mProgressDialog;
    private static AlertDialog alertDialog;

    public static void showProgressDialog(Activity activity, String message) {
        createAndShowProgressDialog(activity, message);
    }

    public static void showProgressDialog(Activity activity, int resourceId) {
        String message = activity.getString(resourceId);
        createAndShowProgressDialog(activity, message);
    }

    private static void createAndShowProgressDialog(final Activity activity, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((null != mProgressDialog) && mProgressDialog.isShowing()) {
                    mProgressDialog.setMessage(message);
                } else {
                    mProgressDialog = ProgressDialog.show(activity, "", message);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setIndeterminate(true);
                }
            }
        });
    }

    public static void dismissProgressDialog(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((null != mProgressDialog) && mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                    mProgressDialog = null;
                }
            }
        });
    }

    public static void dismissProgressDialog() {
        try {
            if ((null != mProgressDialog) && mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
                mProgressDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAlertDialog(Activity activity, String title, String message) {
        createAndShowAlertDialog(activity, title, message, activity.getResources().getString(R.string.string_ok), null);
    }

    public static void showAlertDialog(Activity activity, int resIdTitle, int resIdMessage) {
        String title = (resIdTitle == 0) ? "" : activity.getString(resIdTitle);
        String message = (resIdMessage == 0) ? "" : activity.getString(resIdMessage);
        createAndShowAlertDialog(activity, title, message, activity.getResources().getString(R.string.string_ok), null);
    }

    public static void showAlertDialog(Activity activity, String title, String
            message, AlertClickListener alertClickListener) {
        createAndShowAlertDialog(activity, title, message, activity.getResources().getString(R.string.string_ok), alertClickListener);
    }

    public static void showAlertDialog(Activity activity, int resIdTitle, int resIdMessage, AlertClickListener alertClickListener) {
        String title = (resIdTitle == 0) ? "" : activity.getString(resIdTitle);
        String message = (resIdMessage == 0) ? "" : activity.getString(resIdMessage);
        createAndShowAlertDialog(activity, title, message, activity.getResources().getString(R.string.string_ok), alertClickListener);
    }

    public static void showAlertDialog(Activity activity, int resIdTitle, int resIdMessage, int resIdButton, AlertClickListener alertClickListener) {
        String title = (resIdTitle == 0) ? "" : activity.getString(resIdTitle);
        String message = (resIdMessage == 0) ? "" : activity.getString(resIdMessage);
        String button = (resIdButton == 0) ? "" : activity.getString(resIdButton);
        createAndShowAlertDialog(activity, title, message, button, alertClickListener);
    }

    public static void createAndShowAlertDialog(final Activity activity, String title, String message, String buttonText,
                                                final AlertClickListener alertClickListener) {
        dismissAlertDialog();
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            if (!title.equals("")) {
                builder.setTitle(title);
            }
            builder.setMessage(message).setPositiveButton(buttonText,
                    new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.cancel();
                            dialogInterface.dismiss();
                            dismissAlertDialog();

                            if (alertClickListener != null)
                                alertClickListener.onClicked();
                        }
                    });
            builder.setCancelable(false);
            alertDialog = builder.create();
            alertDialog.show();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAlertDialogWithTwoButton(Activity activity, String title, String message,
                                                    String positiveText, String negativeText, AlertDialogListener listener) {
        createAndShowAlertDialog(activity, title, message, positiveText, negativeText, listener);
    }

    public static void showAlertDialogWithTwoButton(Activity activity, int resIdTitle, int resIdMessage,
                                                    int resIdPositiveText, int resIdNegativeText, AlertDialogListener listener) {
        String title = (resIdTitle == 0) ? "" : activity.getString(resIdTitle);
        String message = (resIdMessage == 0) ? "" : activity.getString(resIdMessage);
        String positiveText = (resIdPositiveText == 0) ? "" : activity.getString(resIdPositiveText);
        String negativeText = (resIdNegativeText == 0) ? "" : activity.getString(resIdNegativeText);
        createAndShowAlertDialog(activity, title, message, positiveText, negativeText, listener);
    }

    public interface AlertClickListener {
        void onClicked();
    }

    public interface AlertDialogListener {
        void onPositiveButtonSelected();

        void onNegativeButtonSelected();
    }

    private static void createAndShowAlertDialog(final Activity activity, String title, String message,
                                                 String positiveText, String negativeText, final AlertDialogListener listener) {

        dismissAlertDialog();

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            if (!title.equals("")) {
                builder.setTitle(title);
            }

            builder.setMessage(message).setPositiveButton(positiveText, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    listener.onPositiveButtonSelected();
                }
            }).setNegativeButton(negativeText, new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.cancel();
                    dialogInterface.dismiss();
                    dismissAlertDialog();
                    listener.onNegativeButtonSelected();
                }
            });

            builder.setCancelable(false);
            alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void dismissAlertDialog() {
        if ((null != alertDialog) && alertDialog.isShowing()) {
            alertDialog.cancel();
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

}
