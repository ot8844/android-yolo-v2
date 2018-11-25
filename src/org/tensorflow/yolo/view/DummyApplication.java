package org.tensorflow.yolo.view;

import android.app.Application;
import android.content.Context;

public class DummyApplication extends Application {
    public static Context applicationCtx;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationCtx = this;
    }
}
