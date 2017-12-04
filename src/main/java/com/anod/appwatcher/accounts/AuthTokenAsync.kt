package com.anod.appwatcher.accounts

import android.accounts.Account
import android.app.Activity
import android.content.Context
import info.anodsplace.appwatcher.framework.ApplicationContext
import info.anodsplace.appwatcher.framework.BackgroundTask

/**
 * @author algavris
 * @date 26/10/2017
 */
class AuthTokenAsync(private val authTokenBlocking: AuthTokenBlocking) {

    constructor(context: ApplicationContext): this(AuthTokenBlocking(context))
    constructor(context: Context): this(ApplicationContext(context))

    fun request(activity: Activity?, account: Account, callback: Callback) {
        BackgroundTask(object : BackgroundTask.Worker<Void?, String>(null) {
            override fun run(param: Void?): String {
                return authTokenBlocking.request(activity, account)
            }

            override fun finished(result: String) {
                if (result.isEmpty()) {
                    callback.onError("Cannot retrieve authorization token")
                } else {
                    callback.onToken(result)
                }
            }
        }).execute()
    }

    interface Callback {
        fun onToken(token: String)
        fun onError(errorMessage: String)
    }
}