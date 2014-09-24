package com.anod.appwatcher.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.anod.appwatcher.R;
import com.anod.appwatcher.backup.gdrive.SyncTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.concurrent.CountDownLatch;

/**
 * Created by alex on 7/30/14.
 */
abstract public class GooglePlayServices implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityListener.ResultListener {

    public static final int REQUEST_CODE_RESOLUTION = 123;

    private static final int ACTION_CONNECT = 1;
    protected final Context mContext;
    protected GoogleApiClient mGoogleApiClient;
    private Activity mActivity;
    private int mOnConnectAction;
    private PendingIntent mResolutionIntent;

    public static class ResolutionException extends Exception {
        private final PendingIntent mResolutionIntent;

        ResolutionException(String message, PendingIntent resolutionIntent) {
            super(message);
            mResolutionIntent = resolutionIntent;
        }

        public PendingIntent getResolution() {
             return mResolutionIntent;
        }
    }

    public GooglePlayServices(Activity activity) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
    }

    public GooglePlayServices(Context context) {
        mContext = context.getApplicationContext();
    }

    public void connect() {
        if (!isConnected()) {
            connectWithAction(ACTION_CONNECT);
        } else {
            onConnectAction(ACTION_CONNECT);
        }
    }

    public void connectLocked() throws Exception {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = createGoogleApiClientBuilder().build();
        }
        mResolutionIntent = null;
        final CountDownLatch latch = new CountDownLatch(1);
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
            }

            @Override
            public void onConnected(Bundle arg0) {
                latch.countDown();
            }
        });

        mGoogleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                mResolutionIntent = result.getResolution();
                latch.countDown();
                AppLog.e(result.toString());
            }
        });
        mGoogleApiClient.connect();
        latch.await();
        if (!mGoogleApiClient.isConnected()) {
            String resolution = (mResolutionIntent == null) ? "none" : mResolutionIntent.toString();
            throw new ResolutionException("Cannot connect to Google Play Services. See log. Resolution: "+resolution,mResolutionIntent);
        }
    }

    public void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isSupported() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS;
    }

    @Override
    public void onConnected(Bundle bundle) {
        onConnectAction(mOnConnectAction);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        AppLog.e("GoogleApiClient connection failed: " + result.toString());

        if (!result.hasResolution()) {
            // show the localized error dialog.
            onConnectionError();
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mActivity, 0).show();
            return;
        }
        try {
            result.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            AppLog.ex(e);
            onConnectionError();
        }
    }


    public boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    /**
     * @return
     */
    public String getPlayServiceStatusText() {
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            return mContext.getString(R.string.gms_service_missing);
        }
        if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            return mContext.getString(R.string.gms_service_update_required);
        }
        if (errorCode == ConnectionResult.SERVICE_DISABLED) {
            return mContext.getString(R.string.gms_service_disabled);
        }
        if (errorCode == ConnectionResult.SERVICE_INVALID) {
            return mContext.getString(R.string.gms_service_invalid);
        }
        return GooglePlayServicesUtil.getErrorString(errorCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == Activity.RESULT_OK) {
            connectWithAction(mOnConnectAction);
        }
    }


    protected void connectWithAction(int action) {
        mOnConnectAction = action;
        if (mGoogleApiClient == null) {

            mGoogleApiClient = createGoogleApiClientBuilder()
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    protected abstract void onConnectAction(int action);
    protected abstract GoogleApiClient.Builder createGoogleApiClientBuilder();
    protected abstract void onConnectionError();


}
