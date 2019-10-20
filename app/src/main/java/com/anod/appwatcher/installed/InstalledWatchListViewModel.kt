package com.anod.appwatcher.installed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import com.anod.appwatcher.AppWatcherApplication
import com.anod.appwatcher.utils.combineLatest
import com.anod.appwatcher.watchlist.LoadResult
import com.anod.appwatcher.watchlist.SectionHeaderFactory
import com.anod.appwatcher.watchlist.WatchListViewModel
import info.anodsplace.framework.content.InstalledPackage

/**
 * @author Alex Gavrishev
 * @date 14/04/2018
 */

typealias InstalledPairRow = Pair<String, Int>

typealias InstalledResult = Pair<List<String>, List<InstalledPackage>>

class InstalledWatchListViewModel(application: android.app.Application) : WatchListViewModel(application) {
    var hasSectionRecent = false
    var hasSectionOnDevice = false

    private val onPackageUninstalled = MutableLiveData(false)

    private val packageRemovedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onPackageUninstalled.value = true
        }
    }

    init {
        getApplication<AppWatcherApplication>().registerReceiver(packageRemovedReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
        })
    }

    val installed: LiveData<InstalledResult> = onPackageUninstalled.switchMap {
        liveData {
            emit(InstalledTaskWorker(context, sortId, titleFilter).run())
        }
    }

    override fun onCleared() {
        getApplication<AppWatcherApplication>().unregisterReceiver(packageRemovedReceiver)
        super.onCleared()
    }

    override var result: LiveData<LoadResult> = appsList.combineLatest(installed).map { pair ->
        val installed = pair.second
        val appsList = pair.first.first
        val filter = pair.first.second
        val recentlyInstalled = installed.first
        val installedPackages = installed.second

        val sections = SectionHeaderFactory(showRecentlyUpdated, hasSectionRecent, hasSectionOnDevice)
                .create(appsList.size, filter.newCount, filter.recentlyUpdatedCount, filter.updatableNewCount, recentlyInstalled.isNotEmpty(), installedPackages.isNotEmpty())

        InstalledLoadResult(
                recentlyInstalled,
                installedPackages,
                appsList,
                sections
        )
    }
}