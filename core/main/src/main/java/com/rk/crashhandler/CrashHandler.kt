package com.rk.crashhandler

import android.os.Looper
import com.rk.libcommons.application
import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import kotlin.system.exitProcess

/**
 * Global crash handler.
 *
 * On the main (UI) thread we log the exception and restart the Looper so the
 * app stays usable after a non-fatal crash. On background threads we terminate
 * the process so Android can restart it cleanly.
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        logErrorOrExit(ex)

        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Restart the Looper so the UI thread keeps processing messages.
            // This effectively swallows crashes that originate from inside Looper.loop().
            while (true) {
                try {
                    Looper.loop()
                    return
                } catch (t: Throwable) {
                    logErrorOrExit(t)
                }
            }
        }

        // Background thread: let the process die so Android can restart it cleanly.
        exitProcess(1)
    }
}

fun logErrorOrExit(throwable: Throwable) {
    runCatching {
        val logFile = application!!.filesDir.child("crash.log").createFileIfNot()
        logFile.appendText("\n==== ${System.currentTimeMillis()} ====\n")
        logFile.appendText(throwable.stackTraceToString())
    }.onFailure {
        it.printStackTrace()
        exitProcess(-1)
    }
}
