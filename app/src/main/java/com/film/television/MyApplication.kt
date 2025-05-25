package com.film.television

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.common.wheel.admanager.AdvertisementManager
import com.common.wheel.admanager.InitCallback
import com.film.television.activity.SplashActivity
import com.film.television.utils.AdUtil
import com.film.television.utils.ApplicationData
import com.film.television.utils.DataStoreUtil
import com.film.television.utils.RouteUtil
import com.film.television.utils.UMUtil
import com.umeng.commonsdk.UMConfigure
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MyApplication : Application() {
    private var activityCount = 0
    private var isAppInForeground = true
    private val activityRefList = mutableListOf<WeakReference<Activity>>()

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        UMConfigure.preInit(this, UMUtil.APP_KEY, UMUtil.CHANNEL)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?
            ) {
                activityRefList.add(WeakReference(activity))
            }

            override fun onActivityStarted(activity: Activity) {
                if (activityCount == 0 && !isAppInForeground) {
                    onAppForeground()
                }
                activityCount += 1
                isAppInForeground = true
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {
                activityCount -= 1
                if (activityCount == 0) {
                    isAppInForeground = false
                }
            }

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {

            }

            override fun onActivityDestroyed(activity: Activity) {
                val newActivityRefList = mutableListOf<WeakReference<Activity>>()
                for (activityRef in activityRefList) {
                    val act = activityRef.get()
                    if (act != null && act != activity) {
                        newActivityRefList.add(WeakReference(act))
                    }
                }
                activityRefList.clear()
                activityRefList.addAll(newActivityRefList)
            }
        })
        mainScope.launch {
            if (DataStoreUtil.getGranted()) {
                UMConfigure.init(
                    this@MyApplication,
                    UMUtil.APP_KEY,
                    UMUtil.CHANNEL,
                    UMConfigure.DEVICE_TYPE_PHONE,
                    ""
                )
            }
        }
        AdvertisementManager.getInstance()
            .init(this, AdUtil.APP_ID, getString(R.string.app_name), object : InitCallback {
                override fun success() {
                    Log.d("lytest", "sdk init success")
                    ApplicationData.setInitStatusAsync(true)
                }

                override fun error() {
                    Log.d("lytest", "sdk init error")
                    ApplicationData.setInitStatusAsync(false)
                }
            })
        AdvertisementManager.getInstance().initConfig()
    }

    private fun onAppForeground() {
        val lastActivity = activityRefList.lastOrNull()?.get()
        if (lastActivity != null && lastActivity !is SplashActivity) {
            RouteUtil.restartApp(lastActivity)
        }
    }

    fun printActivityList() {
        Log.d(
            "lytest",
            "Activity list: ${
                activityRefList.filter { it.get() != null }.map { it.get()!!.javaClass.simpleName }
            }"
        )
    }

    companion object {
        lateinit var INSTANCE: MyApplication
    }

}