package com.smartmobile.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.smartmobile.admob.log

object ActivityLifecycleListener : Application.ActivityLifecycleCallbacks,
    FragmentManager.FragmentLifecycleCallbacks() {

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        log(p0.javaClass.simpleName)
        if (p0 is AppCompatActivity) {
            p0.supportFragmentManager.registerFragmentLifecycleCallbacks(this, true)
        }
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        log(f.javaClass.simpleName)
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

}
