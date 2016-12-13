package com.zpdsherlock.zapplib.app;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by zpd on 2016/12/8.
 */

public class Zapp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
