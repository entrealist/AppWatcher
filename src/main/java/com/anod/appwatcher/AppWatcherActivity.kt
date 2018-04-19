package com.anod.appwatcher

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.widget.SearchView
import android.widget.TextView

import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.sync.SyncScheduler
import com.anod.appwatcher.watchlist.*

import info.anodsplace.framework.AppLog

class AppWatcherActivity : WatchListActivity(), TextView.OnEditorActionListener, SearchView.OnQueryTextListener {

    override val isHomeAsMenu: Boolean
        get() = true

    override val defaultFilterId: Int
        get() = prefs.defaultMainFilterId

    override val layoutResource: Int
        @LayoutRes get() = R.layout.activity_main

    override val menuResource: Int
        get() = R.menu.watchlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)

        if (prefs.useAutoSync) {
            SyncScheduler.schedule(this, prefs.isRequiresCharging, prefs.isWifiOnly, prefs.updatesFrequency)
        }
    }

    override fun onResume() {
        super.onResume()

        AppLog.d("mark updates as viewed.")
        prefs.isLastUpdatesViewed = true
    }

    override fun createViewPagerAdapter(): WatchListActivity.Adapter {
        val adapter = WatchListActivity.Adapter(supportFragmentManager)
        val title = resources.getStringArray(R.array.filter_titles)

        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_ALL,
                prefs.sortIndex,
                sectionForAll(prefs),
                null), title[Filters.TAB_ALL])
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_INSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), title[Filters.TAB_INSTALLED])
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_UNINSTALLED,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), title[Filters.TAB_UNINSTALLED])
        adapter.addFragment(WatchListFragment.newInstance(
                Filters.TAB_UPDATABLE,
                prefs.sortIndex,
                WatchListFragment.DefaultSection(), null), title[Filters.TAB_UPDATABLE])
        return adapter
    }

    private fun sectionForAll(prefs: Preferences): WatchListFragment.Section {
        if (prefs.showRecent && prefs.showOnDevice) {
            return RecentAndOnDeviceSection()
        }
        if (prefs.showRecent) {
            return RecentSection()
        }
        if (prefs.showOnDevice) {
            return OnDeviceSection()
        }
        return WatchListFragment.DefaultSection()
    }
}
