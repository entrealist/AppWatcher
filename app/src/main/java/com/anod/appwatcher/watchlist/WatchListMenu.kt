package com.anod.appwatcher.watchlist

import android.content.Intent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.anod.appwatcher.MarketSearchActivity
import com.anod.appwatcher.R
import com.anod.appwatcher.SettingsActivity
import com.anod.appwatcher.installed.InstalledFragment
import com.anod.appwatcher.model.Filters
import com.anod.appwatcher.preferences.Preferences
import com.anod.appwatcher.provide
import com.anod.appwatcher.tags.TagsListFragment
import com.anod.appwatcher.utils.SingleLiveEvent
import com.anod.appwatcher.utils.forMyApps
import info.anodsplace.framework.app.CustomThemeActivity
import info.anodsplace.framework.app.DialogSingleChoice
import info.anodsplace.framework.content.startActivitySafely
import info.anodsplace.framework.view.MenuItemAnimation

sealed class MenuAction
class SortMenuAction(val sortId: Int) : MenuAction()
class FilterMenuAction(val filterId: Int) : MenuAction()

/**
 * @author Alex Gavrishev
 * @date 03/12/2017
 */
class WatchListMenu(
        private val searchListener: SearchView.OnQueryTextListener,
        private val action: SingleLiveEvent<MenuAction>,
        private val activity: AppCompatActivity
) : SearchView.OnQueryTextListener {
    var expandSearch = false
    var searchQuery = ""
        set(value) {
            this.expandSearch = value.isNotBlank()
            field = value
        }
    var filterId: Int = Filters.TAB_ALL
        set(value) {
            updateFilterItem(value)
            field = value
        }

    private var searchMenuItem: MenuItem? = null
    private val refreshMenuAnimation = MenuItemAnimation(activity, R.anim.rotate)

    private var filterItem: MenuItem? = null

    fun init(menu: Menu) {
        searchMenuItem = menu.findItem(R.id.menu_act_search)
        searchMenuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                onQueryTextChange("")
                return true
            }
        })

        val searchView = searchMenuItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = activity.getString(R.string.search)

        if (expandSearch) {
            searchMenuItem?.expandActionView()
            searchView.setQuery(searchQuery, false)
        }

        refreshMenuAnimation.menuItem = menu.findItem(R.id.menu_act_refresh)

        filterItem = menu.findItem(R.id.menu_act_filter)
        updateFilterItem(filterId)
    }

    private fun updateFilterItem(filterId: Int) {
        if (filterId == Filters.TAB_ALL) {
            filterItem?.icon = activity.resources.getDrawable(R.drawable.ic_flash_off_24dp, activity.theme)
        } else {
            filterItem?.icon = activity.resources.getDrawable(R.drawable.ic_flash_on_24dp, activity.theme)
        }
        filterItem?.subMenu?.getItem(filterId)?.isChecked = true
    }

    fun startRefresh() {
        refreshMenuAnimation.start()
    }

    fun stopRefresh() {
        refreshMenuAnimation.stop()
    }

    private fun collapseSearch() {
        searchMenuItem?.collapseActionView()
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        searchQuery = ""
        if (TextUtils.isEmpty(query)) {
            collapseSearch()
        } else {
            onQueryTextChange("")
            collapseSearch()
        }
        return searchListener.onQueryTextSubmit(query)
    }

    override fun onQueryTextChange(newText: String): Boolean {
        searchQuery = newText
        return searchListener.onQueryTextChange(newText)
    }

    fun onItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {
                val addActivity = Intent(activity, MarketSearchActivity::class.java)
                activity.startActivity(addActivity)
                return true
            }
            R.id.menu_act_refresh -> {
                ViewModelProvider(activity).get(WatchListStateViewModel::class.java).requestRefresh()
                return true
            }
            R.id.menu_settings -> {
                val settingsActivity = Intent(activity, SettingsActivity::class.java)
                activity.startActivity(settingsActivity)
                return true
            }
            R.id.menu_act_installed -> {
                activity.startActivity(InstalledFragment.intent(
                        Preferences.SORT_NAME_ASC,
                        true,
                        activity,
                        (activity as CustomThemeActivity).themeRes,
                        (activity as CustomThemeActivity).themeColors))
                return true
            }
            R.id.menu_act_tags -> {
                activity.startActivity(TagsListFragment.intent(
                        activity,
                        (activity as CustomThemeActivity).themeRes,
                        (activity as CustomThemeActivity).themeColors,
                        null))
                return true
            }
            R.id.menu_act_sort -> {
                val prefs = activity.provide.prefs
                val selected = prefs.sortIndex
                DialogSingleChoice(activity, R.style.AlertDialog, R.array.sort_titles, selected) { dialog, index ->
                    prefs.sortIndex = index
                    action.value = SortMenuAction(index)
                    dialog.dismiss()
                }.show()
                return true
            }
            R.id.menu_filter_all,
            R.id.menu_filter_installed,
            R.id.menu_filter_not_installed,
            R.id.menu_filter_updatable -> {
                action.value = FilterMenuAction(item.order)
                return true
            }
            R.id.menu_my_apps -> {
                activity.startActivitySafely(Intent().forMyApps(true, activity))
                return true
            }
        }
        return false
    }

}