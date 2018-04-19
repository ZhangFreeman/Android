package com.android.my.zhang.dribbview;

import android.app.Application;
import android.content.Context;
import com.facebook.drawee.backends.pipeline.Fresco;

public class DribbViewApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        DribbViewApplication.context = getApplicationContext();

    }
    public static Context getAppContext() {
        return DribbViewApplication.context;
    }
}

