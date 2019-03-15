package com.anod.appwatcher

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import android.util.LruCache
import android.view.ViewConfiguration
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.anod.appwatcher.sync.SyncNotification
import com.crashlytics.android.Crashlytics
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.app.ApplicationInstance
import info.anodsplace.framework.app.CustomThemeActivity
import io.fabric.sdk.android.Fabric
import java.io.File
import java.io.IOException
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.os.Build
import java.lang.reflect.Field


class AppWatcherApplication : Application(), AppLog.Listener, ApplicationInstance {

    override val notificationManager: NotificationManager
        get() = appComponent.notificationManager
    override val memoryCache: LruCache<String, Any?>
        get() = appComponent.memoryCache
    override val nightMode: Int
        get() = appComponent.prefs.nightMode

    val appComponent: AppComponent by lazy {
        AppComponent(this)
    }

    override fun onCreate() {
        super.onCreate()

        tryEnableMenuOnDeviceWithHardwareMenuButton()

        AppLog.setDebug(BuildConfig.DEBUG, "AppWatcher")

        if (appComponent.prefs.isDriveSyncEnabled) {
            appComponent.uploadServiceContentObserver
        }

        if (appComponent.prefs.collectCrashReports) {
            Fabric.with(this, Crashlytics())
            AppLog.logger = FirebaseLogger()
            AppLog.instance.listener = this
        }

        AppCompatDelegate.setDefaultNightMode(appComponent.prefs.nightMode)
        SyncNotification(ApplicationContext(this)).createChannels()
        registerActivityLifecycleCallbacks(LifecycleCallbacks())

        deleteUserLog()
    }

    private fun deleteUserLog() {
        val userLog = File(filesDir, "user-log")
        if (userLog.exists()) {
            userLog.delete()
        }
    }

    override fun onLogException(tr: Throwable) {

        if (isNetworkError(tr) && !appComponent.networkConnection.isNetworkAvailable) {
            // Ignore
            return
        }

        if (appComponent.prefs.collectCrashReports) {
            Crashlytics.logException(tr)
        }
    }

    private fun isNetworkError(tr: Throwable): Boolean {
        return tr is NetworkError
                || (tr is IOException && tr.message?.contains("NetworkError") == true)
                || tr is VolleyError
                || tr is TimeoutError
                || tr is NoConnectionError
                || appComponent.networkConnection.isNetworkException(tr)
    }

    private inner class FirebaseLogger : AppLog.Logger {
        override fun println(priority: Int, tag: String, msg: String) {
            Crashlytics.log(priority, tag, msg)
        }
    }

    private fun tryEnableMenuOnDeviceWithHardwareMenuButton() {
        try {
            val config = ViewConfiguration.get(this)
            val menuKeyField: Field? = ViewConfiguration::class.java.getDeclaredField("sHasPermanentMenuKey")
            if (menuKeyField != null) {
                menuKeyField.isAccessible = true
                menuKeyField.setBoolean(config, false)
            }
        } catch (ex: Exception) {
            // Ignore
        }
    }

}

class LifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {}
    override fun onActivityResumed(activity: Activity?) {}
    override fun onActivityStarted(activity: Activity?) {}
    override fun onActivityDestroyed(activity: Activity?) {}
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
    override fun onActivityStopped(activity: Activity?) {}

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (activity == null) return

        if (activity is CustomThemeActivity) {
            val themeRes = activity.themeRes
            if (themeRes > 0) {
                if (activity.themeColors.available) {
                    if (activity.themeColors.statusBarLight) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            activity.window.decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            activity.window.decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        }
                    } else {
                        activity.window.decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    }

                    if (activity.themeColors.statusBarColor.available) {
                        activity.window.statusBarColor = activity.themeColors.statusBarColor.get(activity)
                    }

                    if (activity.themeColors.navigationBarColor.available) {
                        activity.window.navigationBarColor = activity.themeColors.navigationBarColor.get(activity)
                    }
                }
                activity.setTheme(themeRes)
            }
        }
    }


}
