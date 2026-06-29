package com.rk.terminal

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.github.anrwatchdog.ANRWatchDog
import com.rk.crashhandler.CrashHandler
import com.rk.libcommons.application
import com.rk.resources.Res
import com.rk.update.UpdateManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors


class App : Application() {

    @OptIn(DelicateCoroutinesApi::class)
    companion object {
        private const val TAG = "AndLinuxApp"

        fun getTempDir(): File {
            val tmp = File(application!!.filesDir.parentFile, "tmp")
            if (!tmp.exists()) {
                tmp.mkdir()
            }
            return tmp
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        application = this
        Res.application = this

        // Install the global crash handler so unexpected exceptions get logged to
        // filesDir/crash.log instead of silently killing the process.
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler)

        GlobalScope.launch(Dispatchers.IO) {
            getTempDir().apply {
                if (exists() && listFiles().isNullOrEmpty().not()) { deleteRecursively() }
            }
        }

        ANRWatchDog().start()

        UpdateManager().onUpdate()

        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder().apply {
                    detectAll()
                    penaltyLog()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        // Use a single-thread executor to log violations. We deliberately
                        // do NOT rethrow — doing so killed the listener thread and stopped
                        // further violation reporting.
                        penaltyListener(Executors.newSingleThreadExecutor()) { violation ->
                            Log.w(TAG, "StrictMode VM violation: ${violation.message}", violation)
                        }
                    }
                }.build()
            )
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

}
