package com.rk.karbon_exec

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.rk.libcommons.TerminalCommand
import com.rk.libcommons.application
import com.rk.libcommons.pendingCommand
import com.rk.libcommons.toast
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Termux RUN_COMMAND integration.
 *
 * Status: currently UNUSED inside AndLinux. The functions here are kept as a
 * standalone helper module so that future code (e.g. a "Run in Termux" menu
 * action) can call into a real Termux install via the RUN_COMMAND intent.
 *
 * If you do wire this up, make sure the user-facing UI checks
 * `isTermuxInstalled()` and `isExecPermissionGranted()` first and shows a
 * helpful error if either is false — otherwise `runCommandTermux` will
 * silently toast and do nothing.
 */
@Suppress("unused")
const val TERMUX_PKG = "com.termux"

@SuppressLint("SdCardPath")
@Suppress("unused")
const val TERMUX_PREFIX = "/data/data/$TERMUX_PKG/files/usr"

fun isTermuxInstalled(): Boolean {
    val packageManager: PackageManager = application!!.packageManager
    val intent = packageManager.getLaunchIntentForPackage(TERMUX_PKG) ?: return false
    val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
    return list.size > 0
}

private fun checkTermuxInstall() {
    if (isTermuxInstalled().not()) {
        throw RuntimeException("Termux not installed")
    }
    if (isTermuxCompatible().not()) {
        throw RuntimeException("Termux not compatible")
    }
    if (isExecPermissionGranted().not()) {
        throw RuntimeException("Termux exec permission denied")
    }
}

fun isTermuxCompatible(): Boolean {
    val intent = Intent("$TERMUX_PKG.RUN_COMMAND").apply {
        setPackage(TERMUX_PKG)
    }
    val activities = application!!.packageManager.queryIntentServices(intent, 0)
    return activities.isNotEmpty()
}

@Suppress("unused")
fun testExecPermission(): Pair<Boolean, Exception?> {
    return try {
        checkTermuxInstall()
        runCommandTermux(application!!, "$TERMUX_PREFIX/bin/echo", arrayOf(), background = true, isTesting = true)
        Pair(true, null)
    } catch (e: Exception) {
        Pair(false, e)
    }
}


fun isExecPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
        application!!, "com.termux.permission.RUN_COMMAND"
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Send a RUN_COMMAND intent to Termux.
 *
 * The correct extra key for the working directory is
 * `com.termux.RUN_COMMAND_WORKDIR` — NOT `com.termux.RUN_COMMAND_SERVICE.EXTRA_WORKDIR`.
 * The previous key was silently ignored by Termux's RunCommandService, so the
 * cwd was never actually applied. Fixed in 1.5.
 */
@OptIn(DelicateCoroutinesApi::class)
@Suppress("unused")
fun runCommandTermux(
    context: Context,
    exe: String,
    args: Array<String>,
    background: Boolean = true,
    cwd: String? = null,
    isTesting: Boolean = false
) {
    runCatching { checkTermuxInstall() }.onFailure { toast(it.message) }.onSuccess {
        GlobalScope.launch(Dispatchers.Main) {
            if (isTesting.not()) {
                runCatching { launchTermux() }
                delay(200)
            }
            val intent = Intent("$TERMUX_PKG.RUN_COMMAND").apply {
                setClassName(TERMUX_PKG, "$TERMUX_PKG.app.RunCommandService")
                putExtra("$TERMUX_PKG.RUN_COMMAND_PATH", exe)
                putExtra("$TERMUX_PKG.RUN_COMMAND_ARGUMENTS", args)
                putExtra("$TERMUX_PKG.RUN_COMMAND_BACKGROUND", background)
                cwd?.let { putExtra("$TERMUX_PKG.RUN_COMMAND_WORKDIR", it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}


@Suppress("unused")
fun runBashScript(
    context: Context, script: String, workingDir: String? = null, background: Boolean = false
) {
    runCommandTermux(
        context = context,
        exe = "$TERMUX_PREFIX/bin/bash",
        arrayOf("-c", script),
        background = background,
        cwd = workingDir
    )
}

/**
 * Launch AndLinux's own terminal activity with a pending command.
 *
 * Replaces the legacy Xed-Editor cross-process terminal launch path which
 * referenced a class that does not exist in this project.
 */
@Suppress("unused")
fun launchInternalTerminal(context: Context, terminalCommand: TerminalCommand) {
    pendingCommand = terminalCommand
    context.startActivity(
        Intent(
            context, Class.forName("com.rk.terminal.ui.activities.terminal.MainActivity")
        )
    )
}

fun launchTermux(): Boolean {
    if (isTermuxInstalled().not()) {
        return false
    }
    application!!.startActivity(application!!.packageManager.getLaunchIntentForPackage(TERMUX_PKG))
    return true
}

