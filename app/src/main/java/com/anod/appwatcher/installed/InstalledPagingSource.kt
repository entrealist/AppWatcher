// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.installed

import androidx.paging.PagingSource
import com.anod.appwatcher.Application
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.App
import com.anod.appwatcher.database.entities.AppListItem
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.watchlist.OnDeviceItem
import com.anod.appwatcher.watchlist.SectionItem
import com.anod.appwatcher.watchlist.WatchListPagingSource
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.util.dayStartAgoMillis

class InstalledPagingSource(
        private val sortId: Int,
        private val titleFilter: String,
        private val config: WatchListPagingSource.Config,
        private val changelogAdapter: ChangelogAdapter,
        private val appContext: ApplicationContext
) : PagingSource<Int, SectionItem>() {
    private val database: AppsDatabase = Application.provide(appContext).database

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SectionItem> {
        val installed = InstalledTaskWorker(appContext, sortId, titleFilter).run()
        val allInstalledPackageNames = installed.map { it.pkg.name }
        val watchingPackages = database.apps().loadRowIds(allInstalledPackageNames).associateBy({ it.packageName }, { it.rowId })

        if (sortId == Preferences.SORT_DATE_ASC || sortId == Preferences.SORT_DATE_DESC) {
            val recentTime = dayStartAgoMillis(Preferences.recentDays)
            val recentNotWatched = installed.filter {
                !watchingPackages.containsKey(it.pkg.name) && it.pkg.updateTime > recentTime
            }.map { it.pkg }
            if (recentNotWatched.isNotEmpty() || watchingPackages.isNotEmpty()) {
                changelogAdapter.load(watchingPackages.keys.toList(), recentNotWatched)
            }
        }

        val items: List<SectionItem> = installed
                .asSequence()
                .mapNotNull {
                    val rowId = watchingPackages[it.pkg.name] ?: -1
                    if (config.selectionMode && rowId >= 0)
                        null
                    else
                        App.fromInstalledPackage(rowId, it)
                }
                .map { app ->
                    val appChange = changelogAdapter.changelogs[app.appId]
                    OnDeviceItem(AppListItem(
                            app, appChange?.details ?: "",
                            noNewDetails = false, recentFlag = false
                    ), config.selectionMode)
                }.toList()


        return LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = null
        )
    }

}