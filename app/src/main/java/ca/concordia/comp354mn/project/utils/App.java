package ca.concordia.comp354mn.project.utils;

import android.app.Application;
import android.content.Context;

/**
 *  Provides a global reference to the application's context that can be
 *  accessed by any Activity or member class without having to pass around
 *  references to constructors, etc. Pseudo-implementation of Singleton.
 */
public class App extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }
}
