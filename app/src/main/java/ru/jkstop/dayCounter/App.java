package ru.jkstop.dayCounter;

import android.app.Application;
import android.content.Context;

/**
 * Created by ivsmirnov on 12.07.2016.
 */
public class App extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getContext(){
        return App.context;
    }
}
