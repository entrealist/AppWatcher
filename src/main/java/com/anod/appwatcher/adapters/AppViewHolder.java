package com.anod.appwatcher.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anod.appwatcher.R;
import com.anod.appwatcher.model.AppInfo;
import com.anod.appwatcher.utils.PackageManagerUtils;

public class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected final DataProvider mDataProvider;

    public AppInfo app;
    public int position;
    public View section;
    public TextView sectionText;
    public TextView sectionCount;
    public TextView title;
    public TextView details;
    public TextView version;
    public TextView price;
    public ImageView icon;
    public View newIndicator;
    public TextView updateDate;
    private OnClickListener mListener;

    protected Bitmap mDefaultIcon;

    public interface OnClickListener {
        void onIconClick(AppInfo app);
        void onItemClick(AppInfo app);
    }

    public interface DataProvider {
        String getVersionText();
        String getUpdateText();
        String getInstalledText();
        int getUpdateTextColor();
        int getTotalAppsCount();
        int getNewAppsCount();
        PackageManagerUtils getPackageManagerUtils();
    }

    public AppViewHolder(View itemView, DataProvider dataProvider, OnClickListener listener) {
        super(itemView);

        mListener = listener;
        mDataProvider = dataProvider;

        this.app = null;
        this.position = 0;
        this.section = itemView.findViewById(R.id.sec_header);
        this.sectionText = (TextView) itemView.findViewById(R.id.sec_header_title);
        this.sectionCount = (TextView) itemView.findViewById(R.id.sec_header_count);
        this.title = (TextView) itemView.findViewById(android.R.id.title);
        this.details = (TextView) itemView.findViewById(R.id.details);
        this.icon = (ImageView) itemView.findViewById(android.R.id.icon);
        this.version = (TextView) itemView.findViewById(R.id.updated);
        this.price = (TextView) itemView.findViewById(R.id.price);
        this.newIndicator = itemView.findViewById(R.id.new_indicator);
        this.updateDate = (TextView) itemView.findViewById(R.id.update_date);

        itemView.setOnClickListener(this);
        this.icon.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == android.R.id.icon) {
            mListener.onIconClick(this.app);
        }
        mListener.onItemClick(this.app);
    }

    public void bindView(int position, AppInfo app) {
        this.position = position;
        this.app = app;
        title.setText(app.getTitle());
        details.setText(app.getCreator());
        icon.setTag(this);
        Bitmap icon = app.getIcon();
        if (icon == null) {
            if (mDefaultIcon == null) {
                mDefaultIcon = BitmapFactory.decodeResource(itemView.getContext().getResources(), R.drawable.ic_android_black_24dp);
            }
            icon = mDefaultIcon;
        }

        this.icon.setImageBitmap(icon);
        if (app.getStatus() == AppInfo.STATUS_UPDATED) {
            version.setVisibility(View.VISIBLE);
            version.setText(String.format(mDataProvider.getUpdateText(), app.getVersionName()));
            version.setTextColor(mDataProvider.getUpdateTextColor());
            newIndicator.setVisibility(View.VISIBLE);
        } else {
            if (TextUtils.isEmpty(app.getVersionName())) {
                version.setVisibility(View.INVISIBLE);
            } else {
                version.setVisibility(View.VISIBLE);
                version.setText(String.format(mDataProvider.getVersionText(), app.getVersionName()));
            }
            newIndicator.setVisibility(View.INVISIBLE);
        }

        bindPriceView(app);

        bindSectionView();

        String uploadDate = app.getUploadDate();

        if (!"".equals(uploadDate)) {
            updateDate.setText(uploadDate);
            updateDate.setVisibility(View.VISIBLE);
        } else {
            updateDate.setVisibility(View.GONE);
        }
    }

    protected void bindPriceView(AppInfo app) {
        boolean isInstalled = mDataProvider.getPackageManagerUtils().isAppInstalled(app.getPackageName());
        if (isInstalled) {
            PackageManagerUtils.InstalledInfo installed = mDataProvider.getPackageManagerUtils().getInstalledInfo(app.getPackageName());
            if (TextUtils.isEmpty(installed.versionName)) {
                price.setText(mDataProvider.getInstalledText());
            } else {
                price.setText(mDataProvider.getInstalledText() + " " + installed.versionName);
            }
        } else {
            if (app.getPriceMicros() == 0) {
                price.setText(R.string.free);
            } else {
                price.setText(app.getPriceText());
            }
        }
    }

    protected void bindSectionView() {
        if (position == mDataProvider.getNewAppsCount()) {
            sectionText.setText(R.string.watching);
            sectionCount.setText(String.valueOf(mDataProvider.getTotalAppsCount() - mDataProvider.getNewAppsCount()));
            section.setVisibility(View.VISIBLE);
        } else if (position == 0 && mDataProvider.getNewAppsCount() > 0) {
            sectionText.setText(R.string.recently_updated);
            sectionCount.setText(String.valueOf(mDataProvider.getNewAppsCount()));
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }
    }
}