package com.github.johnkal.appcrashhandlerlibrary;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author John Kalimeris
 * @version 1.0.0
 */

/**
 * Class that catch exception and build the object to send to UncaughtExceptionActivity.
 * @see Thread.UncaughtExceptionHandler
 */
public class AppCrashHandler implements Parcelable, Thread.UncaughtExceptionHandler {

    //Activity _activity;
    Throwable _ex;
    Builder builder;

    private Activity activity;
    private String title;
    private int titleRes;
    private String description;
    private int descriptionRes;
    private int backgroundColor;
    private int titleColor;
    private int descriptionColor;
    private int iconRes;
    private int positiveText;
    private int negativeText;
    private boolean takeScreenshot;
    private String emailTo;
    private String emailSubject;

    /**
     * Constructor of class according to builder pattern.
     * @param builder
     */
    private AppCrashHandler(final Builder builder) {

        activity = builder.activity;
        title = builder.title;
        titleRes = builder.titleRes;
        description = builder.description;
        descriptionRes = builder.descriptionRes;
        backgroundColor = builder.backgroundColor;
        titleColor = builder.titleColor;
        descriptionColor = builder.descriptionColor;
        iconRes = builder.iconRes;
        positiveText = builder.positiveText;
        negativeText = builder.negativeText;
        takeScreenshot = builder.takeScreenshot;
        emailTo = builder.emailTo;
        emailSubject = builder.emailSubject;

        this.builder = builder;
    }

    public AppCrashHandler(Parcel in)
    {
        this.title = in.readString();
        this.titleRes = in.readInt();
        this.description = in.readString();
        this.descriptionRes = in.readInt();
        this.backgroundColor = in.readInt();
        this.titleColor = in.readInt();
        this.descriptionColor = in.readInt();
        this.iconRes = in.readInt();
        this.positiveText = in.readInt();
        this.negativeText = in.readInt();
        this.takeScreenshot = in.readInt() == 1;
        this.emailTo = in.readString();
        this.emailSubject = in.readString();
    }

    public Activity getActivity() {
        return activity;
    }

    public String getTitle() {
        return title;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public String getDescription() {
        return description;
    }

    public int getDescriptionRes() {
        return descriptionRes;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getDescriptionColor() {
        return descriptionColor;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getPositiveText() {
        return positiveText;
    }

    public int getNegativeText() {
        return negativeText;
    }

    public boolean getTakeScreenshot() {
        return takeScreenshot;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public Builder getBuilder() {
        return builder;
    }

    /**
     * This is the method who catch the exception.
     * @param t
     * @param ex
     */
    @SuppressWarnings("WrongConstant")
    @Override
    public void uncaughtException(Thread t, Throwable ex) {

        _ex = ex;
        String screenshotPath;
        if (takeScreenshot && hasPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            screenshotPath = takeScreenshot();
        }
        else {
            screenshotPath = "";
        }

        Intent registerActivity = new Intent(activity, UncaughtExceptionActivity.class);
        String stackTrace = Log.getStackTraceString(_ex);
        registerActivity.putExtra("log", stackTrace);
        registerActivity.putExtra("builder", getBuilder().getAppCrashHandler());
        if (takeScreenshot) {
            if (screenshotPath != "") {
                registerActivity.putExtra("screenshotPath", screenshotPath);
            }
        }
        registerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        registerActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        activity.startActivity(registerActivity);

        //This will finish your activity manually
        activity.finish();

        //This will stop your application and take out from it.
        System.exit(2);
    }

    /**
     * Check if we the app has permission Write_external_storage for take screenshot.
     * @param context
     * @param permission
     * @return boolean
     * @since 23
     */
    private boolean hasPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Method tha take scrrenshot of the crashed app.
     * @return pathImage
     */
    private String takeScreenshot() {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(now);

        try {
            String folder_main = "AppCrashHandler";

            File f = new File(Environment.getExternalStorageDirectory(), folder_main);
            if (!f.exists()) {
                f.mkdirs();
            }
            // image naming and path  to include sd card  appending name you choose for file
            final String mPath = f.getAbsolutePath() + "/" + date + ".jpg";

            // create bitmap screen capture
            final View v1 = activity.getWindow().getDecorView().getRootView();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // your UI code here
                    Bitmap bitmap = Bitmap.createBitmap(getBitmapFromView(v1));

                    File imageFile = new File(mPath);

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(imageFile);
                        int quality = 100;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            return mPath;
            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Method that create Bitmap from app view.
     * @param view
     * @return Bitmap
     */
    public static Bitmap getBitmapFromView(View view) {

        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static final Parcelable.Creator<AppCrashHandler> CREATOR = new Parcelable.Creator<AppCrashHandler>() {
        public AppCrashHandler createFromParcel(Parcel in) {
            return new AppCrashHandler(in);  // ERROR! Leave(Parcel) is undefined
        }                          // Builder has a private constructor leaving
        //     a single instance;

        public AppCrashHandler[] newArray(int size) {
            return new AppCrashHandler[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(titleRes);
        dest.writeString(description);
        dest.writeInt(descriptionRes);
        dest.writeInt(backgroundColor);
        dest.writeInt(titleColor);
        dest.writeInt(descriptionColor);
        dest.writeInt(iconRes);
        dest.writeInt(positiveText);
        dest.writeInt(negativeText);
        dest.writeInt(takeScreenshot ? 1 : 0);
        dest.writeString(emailTo);
        dest.writeString(emailSubject);
    }

    /**
     * Builder class that construct the object to send to AppCrashHandler.
     * @see "https://www.jetbrains.com/help/idea/replace-constructor-with-builder.html"
     */
    public static class Builder {
        private AppCrashHandler appCrashHandler;
        private Activity activity;
        private String title;
        private int titleRes;
        private String description;
        private int descriptionRes;
        private int backgroundColor;
        private int titleColor;
        private int descriptionColor;
        private int iconRes;
        private int positiveText;
        private int negativeText;
        private boolean takeScreenshot;
        private String emailTo;
        private String emailSubject;

        public void setAppCrashHandler(AppCrashHandler appCrashHandler) {
            this.appCrashHandler = appCrashHandler;
        }

        public Builder setActivity(@NonNull final Activity activity) {
            this.activity = activity;
            return this;
        }

        public Builder setTitle(@NonNull final String title) {
            this.title = title;
            return this;
        }

        public Builder setTitleRes(@NonNull @StringRes final int titleRes) {
            this.titleRes = titleRes;
            return this;
        }

        public Builder setDescription(@NonNull final String description) {
            this.description = description;
            return this;
        }

        public Builder setDescriptionRes(@NonNull @StringRes final int descriptionRes) {
            this.descriptionRes = descriptionRes;
            return this;
        }

        public Builder setBackgroundColor(@NonNull @ColorRes final int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setTitleColor(@NonNull @ColorRes final int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder setDescriptionColor(@NonNull @ColorRes final int descriptionColor) {
            this.descriptionColor = descriptionColor;
            return this;
        }

        public Builder setIcon(@NonNull @DrawableRes final int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        public Builder setPositiveText(@NonNull @StringRes final int positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public Builder setNegativeText(@NonNull @StringRes final int negativeText) {
            this.negativeText = negativeText;
            return this;
        }

        public Builder setTakeScreenshot(@NonNull final boolean takeScreenshot) {
            this.takeScreenshot = takeScreenshot;
            return this;
        }

        public Builder setEmailTo(@NonNull final String emailTo) {
            this.emailTo = emailTo;
            return this;
        }

        public Builder setEmailSubject(@NonNull final String emailSubject) {
            this.emailSubject = emailSubject;
            return this;
        }

        public AppCrashHandler getAppCrashHandler() {
            return appCrashHandler;
        }

        /**
         * Create the AppCrashHandler object with Builder object.
         * @return
         */
        public AppCrashHandler create() {
            if (title != null && titleRes != 0) {
                throw new IllegalStateException("You can't set concurrently title and titleRes");
            }
            if ((description != null && descriptionRes != 0)) {
                throw new IllegalStateException("You can't set concurrently description and +" +
                        "descriptionRes");
            }
            setAppCrashHandler(new AppCrashHandler(this));
            return getAppCrashHandler();
        }
    }
}
