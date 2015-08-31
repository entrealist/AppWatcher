package com.anod.appwatcher;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import com.crashlytics.android.Crashlytics;
import com.mopub.common.MoPub;
import com.squareup.leakcanary.LeakCanary;

import info.anodsplace.android.log.AppLog;
import io.fabric.sdk.android.Fabric;
import java.lang.reflect.Field;

public class AppWatcherApplication extends Application implements AppLog.Listener {
    private ObjectGraph mObjectGraph;

    @Override
	 public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics(), new MoPub());
		LeakCanary.install(this);

//		 NewRelic.withApplicationToken(
//			"AA47c4b684f2af988fdf3a13518738d7eaa8a4976f"
//		 ).start(this);

		 try {
			 ViewConfiguration config = ViewConfiguration.get(this);
			 Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			 if(menuKeyField != null) {
				 menuKeyField.setAccessible(true);
				 menuKeyField.setBoolean(config, false);
			 }
		 } catch (Exception ex) {
			 // Ignore
		 }

        AppLog.setDebug(BuildConfig.DEBUG, "AppWatcher");
        AppLog.instance().setListener(this);

        mObjectGraph = new ObjectGraph(this);
    }

    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    public static AppWatcherApplication get(Context context) {
        return (AppWatcherApplication)context.getApplicationContext();
    }

    public static ObjectGraph provide(Context context) {
        return ((AppWatcherApplication) context.getApplicationContext()).getObjectGraph();
    }

    @Override
    public void onLogException(Throwable tr) {
        Crashlytics.logException(tr);
    }
}
