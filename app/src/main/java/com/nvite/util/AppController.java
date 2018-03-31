package com.nvite.util;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;


/**
 * Created by cqlsys on 1/15/2016.
 */
public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}