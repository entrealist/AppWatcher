package com.anod.appwatcher.details

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.anod.appwatcher.R
import com.anod.appwatcher.content.AppChangeCursor
import com.anod.appwatcher.model.AppChange
import com.anod.appwatcher.model.AppInfo
import info.anodsplace.framework.text.Html
import info.anodsplace.framework.widget.recyclerview.ArrayAdapter
import info.anodsplace.framework.widget.recyclerview.RecyclerViewCursorListAdapter
import info.anodsplace.framework.widget.recyclerview.RecyclerViewStateAdapter

class ChangeView(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val changelog: TextView by lazy {
        val view = itemView.findViewById<TextView>(R.id.changelog)
        view.autoLinkMask = Linkify.ALL
        view
    }
    val version: TextView by lazy { itemView.findViewById<TextView>(R.id.version) }
    val uploadDate: TextView  by lazy { itemView.findViewById<TextView>(R.id.upload_date) }

    fun bindView(change: AppChange) {
        version.text = "${change.versionName} (${change.versionCode})"
        uploadDate.text = change.uploadDate
        if (change.details.isEmpty()) {
            changelog.setText(R.string.no_recent_changes)
        } else {
            changelog.text = Html.parse(change.details)
        }
    }
}

class ChangesAdapter(private val context: Context): RecyclerView.Adapter<ChangeView>()  {

    private var localChanges = emptyList<AppChange>()

    override fun getItemCount(): Int {
        return localChanges.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangeView {
        val v = LayoutInflater.from(context).inflate(R.layout.list_item_change, parent, false)
        return ChangeView(v)
    }

    override fun onBindViewHolder(holder: ChangeView, position: Int) {
        holder.bindView(localChanges[position])
    }

    val isEmpty: Boolean
        get() = itemCount == 0

    fun setData(localChanges: List<AppChange>, recentChange: AppChange) {
        when {
            localChanges.isEmpty() -> {
                if (!recentChange.isEmpty) {
                    this.localChanges = listOf(recentChange)
                }
            }
            localChanges.first() == recentChange -> this.localChanges = localChanges
            else -> {
                if (recentChange.isEmpty) {
                    this.localChanges = localChanges
                } else {
                    this.localChanges = listOf(recentChange, *localChanges.toTypedArray())
                }
            }
        }
        notifyDataSetChanged()
    }
}