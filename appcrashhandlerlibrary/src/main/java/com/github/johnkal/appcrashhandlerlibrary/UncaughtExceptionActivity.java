package com.github.johnkal.appcrashhandlerlibrary;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
  *@author John Kalimeris
  * @version 1.0.0
 */

/**
 * Class that build the dialog with error catch.
 */
public class UncaughtExceptionActivity extends AppCompatActivity {

    private Activity _activity = UncaughtExceptionActivity.this;
    private AppCrashHandler appCrashHandler;
    private Uri uri = null;
    private MaterialDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //final String log = (String) getIntent().getExtras().get("log");

        final String log = (String) getIntent().getStringExtra("log");
        appCrashHandler = getIntent().getParcelableExtra("builder");
        if (appCrashHandler.getTakeScreenshot()) {
            String screenshotPath = (String) getIntent().getStringExtra("screenshotPath");
            File imageFile = new File(screenshotPath);
            //final Uri uri = Uri.fromFile(imageFile);
            uri = FileProvider.getUriForFile(UncaughtExceptionActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider", imageFile);
        }

        if (appCrashHandler.getTitle() != null) {
            dialog = new MaterialDialog.Builder(UncaughtExceptionActivity.this)
                    .title(appCrashHandler.getTitle() != null ? appCrashHandler.getTitle() : "Error")
                    .content(appCrashHandler.getDescription() != null ? appCrashHandler.getDescription()
                            : "An error occured. Do you want to send report to developer?")
                    .backgroundColorRes(appCrashHandler.getBackgroundColor() != 0 ?
                            appCrashHandler.getBackgroundColor() : R.color.white)
                    .titleColorRes(appCrashHandler.getTitleColor() != 0 ? appCrashHandler.getTitleColor()
                            : R.color.black)
                    .contentColorRes(appCrashHandler.getDescriptionColor() != 0 ?
                            appCrashHandler.getDescriptionColor() : R.color.black)
                    .positiveText(appCrashHandler.getPositiveText() != 0 ?
                            appCrashHandler.getPositiveText() : R.string.send)
                    .negativeText(appCrashHandler.getNegativeText() != 0 ?
                            appCrashHandler.getNegativeText() : R.string.cancel)
                    .iconRes(appCrashHandler.getIconRes() != 0 ? appCrashHandler.getIconRes() :
                            R.drawable.ic_bug_report_black_24dp)
                    .onPositive((dialog, which) -> {
                        sendEmail(log, uri);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    })
                    .onNegative((dialog, which) -> {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    })
                    .canceledOnTouchOutside(true)
                    .show();
        }else {
            dialog = new MaterialDialog.Builder(UncaughtExceptionActivity.this)
                    .title(appCrashHandler.getTitleRes() != 0 ? appCrashHandler.getTitleRes() : R.string.error)
                    .content(appCrashHandler.getDescriptionRes() != 0 ? appCrashHandler.getDescriptionRes()
                            : R.string.content_message)
                    .backgroundColorRes(appCrashHandler.getBackgroundColor() != 0 ?
                            appCrashHandler.getBackgroundColor() : R.color.white)
                    .titleColorRes(appCrashHandler.getTitleColor() != 0 ? appCrashHandler.getTitleColor()
                            : R.color.black)
                    .contentColorRes(appCrashHandler.getDescriptionColor() != 0 ?
                            appCrashHandler.getDescriptionColor() : R.color.black)
                    .positiveText(appCrashHandler.getPositiveText() != 0 ?
                            appCrashHandler.getPositiveText() : R.string.send)
                    .negativeText(appCrashHandler.getNegativeText() != 0 ?
                            appCrashHandler.getNegativeText() : R.string.cancel)
                    .iconRes(appCrashHandler.getIconRes() != 0 ? appCrashHandler.getIconRes() :
                            R.drawable.ic_bug_report_black_24dp)
                    .onPositive((dialog, which) -> {
                        sendEmail(log, uri);
                        android.os.Process.killProcess(android.os.Process.myPid());
                    })
                    .onNegative((dialog, which) -> {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    })
                    .canceledOnTouchOutside(false)
                    .show();
        }


    }

    /**
     * Create send email proccess. Screenshot, log and device info append to mail.
     * @param log
     * @param uri
     */
    private void sendEmail(String log, Uri uri)
    {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("application/octet-stream");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{appCrashHandler.getEmailTo() != null ?
                appCrashHandler.getEmailTo() : ""});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,appCrashHandler.getEmailSubject()
                != null ? appCrashHandler.getEmailSubject() : "App Crash");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, log + "\n" + getDeviceInfo());
        if (appCrashHandler.getTakeScreenshot()) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(Intent.createChooser(emailIntent,
                    getResources().getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get the device info.
     * @return
     */
    private String getDeviceInfo()
    {
        String s = "";
        try {
            PackageInfo pInfo = _activity.getPackageManager().getPackageInfo(
                    _activity.getPackageName(), PackageManager.GET_META_DATA);
            s += "\n APP Package Name: " + _activity.getPackageName();
            s += "\n APP Version Name: " + pInfo.versionName;
            s += "\n APP Version Code: " + pInfo.versionCode;
            s += "\n";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        s += "\n OS Version: " + System.getProperty("os.version") + " ("
                + android.os.Build.VERSION.INCREMENTAL + ")";
        s += "\n OS API Level: " + android.os.Build.VERSION.SDK;
        s += "\n Device: " + android.os.Build.DEVICE;
        s += "\n Model (and Product): " + android.os.Build.MODEL + " ("
                + android.os.Build.PRODUCT + ")";

        // more from
        // http://developer.android.com/reference/android/os/Build.html :
        s += "\n Manufacturer: " + android.os.Build.MANUFACTURER;
        s += "\n Other TAGS: " + android.os.Build.TAGS;

        s += "\n screenWidth: "
                + _activity.getWindow().getWindowManager().getDefaultDisplay().getWidth();
        s += "\n screenHeigth: "
                + _activity.getWindow().getWindowManager().getDefaultDisplay().getHeight();
        s += "\n Keyboard available: "
                + (_activity.getResources().getConfiguration().keyboard !=
                Configuration.KEYBOARD_NOKEYS);

        s += "\n Trackball available: "
                + (_activity.getResources().getConfiguration().navigation ==
                Configuration.NAVIGATION_TRACKBALL);
        s += "\n SD Card state: " + Environment.getExternalStorageState();
        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        String key = "";
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            s += "\n > " + key + " = " + (String) p.get(key);
        }
        return s;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }
}
