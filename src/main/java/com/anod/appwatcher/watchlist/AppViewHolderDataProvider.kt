package com.anod.appwatcher.watchlist

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat

import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppChangeContentProviderClient
import info.anodsplace.appwatcher.framework.InstalledApps

/**
 * @author alex
 * *
 * @date 2015-08-30
 */
open class AppViewHolderDataProvider(
        private val context: Context,
        override val installedApps: InstalledApps)
    : AppViewHolderBase.DataProvider {

    override val installedText = context.resources.getString(R.string.installed)!!
    override val noRecentChangesText = context.resources.getString(R.string.no_recent_changes)!!
    override val appChangeContentProvider = AppChangeContentProviderClient(context)
    override var totalAppsCount = 0
    final override var newAppsCount = 0
        private set
    final override var updatableAppsCount = 0
        private set

    internal fun setNewAppsCount(newAppsCount: Int, updatableAppsCount: Int) {
        this.newAppsCount = newAppsCount
        this.updatableAppsCount = updatableAppsCount
    }

    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    @ColorInt
    override fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    override fun formatVersionText(versionName: String, versionNumber: Int): String {
        return context.getString(R.string.version_text, versionName, versionNumber)
    }

}