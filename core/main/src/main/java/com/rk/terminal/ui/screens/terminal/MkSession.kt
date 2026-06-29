package com.rk.terminal.ui.screens.terminal

import com.rk.libcommons.alpineHomeDir
import com.rk.libcommons.child
import com.rk.libcommons.localBinDir
import com.rk.libcommons.localLibDir
import com.rk.libcommons.pendingCommand
import com.rk.settings.Settings
import com.rk.terminal.App.Companion.getTempDir
import com.rk.terminal.BuildConfig
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.screens.settings.WorkingMode
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import java.io.File

object MkSession {
    fun createSession(
        activity: MainActivity, sessionClient: TerminalSessionClient, session_id: String,workingMode:Int
    ): TerminalSession {
        with(activity) {
            // Only forward non-null Android system env vars. Forwarding null
            // would put literal "KEY=null" entries into the child env, which
            // can confuse shell scripts that source these variables.
            val envVariables = mapOf(
                "ANDROID_ART_ROOT" to System.getenv("ANDROID_ART_ROOT"),
                "ANDROID_DATA" to System.getenv("ANDROID_DATA"),
                "ANDROID_I18N_ROOT" to System.getenv("ANDROID_I18N_ROOT"),
                "ANDROID_ROOT" to System.getenv("ANDROID_ROOT"),
                "ANDROID_RUNTIME_ROOT" to System.getenv("ANDROID_RUNTIME_ROOT"),
                "ANDROID_TZDATA_ROOT" to System.getenv("ANDROID_TZDATA_ROOT"),
                "BOOTCLASSPATH" to System.getenv("BOOTCLASSPATH"),
                "DEX2OATBOOTCLASSPATH" to System.getenv("DEX2OATBOOTCLASSPATH"),
                "EXTERNAL_STORAGE" to System.getenv("EXTERNAL_STORAGE")
            ).filterValues { it != null }

            val workingDir = pendingCommand?.workingDir ?: alpineHomeDir().path

            // init-host and init are written by UpdateManager.onUpdate() on every
            // app start; we don't need to rewrite them here per-session.

            val env = mutableListOf(
                "PATH=${System.getenv("PATH") ?: "/system/bin"}:/sbin:${localBinDir().absolutePath}",
                "HOME=/sdcard",
                "PUBLIC_HOME=${getExternalFilesDir(null)?.absolutePath}",
                "COLORTERM=truecolor",
                "TERM=xterm-256color",
                "LANG=C.UTF-8",
                "BIN=${localBinDir()}",
                "DEBUG=${BuildConfig.DEBUG}",
                "PREFIX=${filesDir.parentFile!!.path}",
                "LD_LIBRARY_PATH=${localLibDir().absolutePath}",
                "LINKER=${if(File("/system/bin/linker64").exists()){ "/system/bin/linker64" }else{ "/system/bin/linker" }}",
                "NATIVE_LIB_DIR=${applicationInfo.nativeLibraryDir}",
                "PKG=${packageName}",
                "RISH_APPLICATION_ID=${packageName}",
                "PKG_PATH=${applicationInfo.sourceDir}",
                "PROOT_TMP_DIR=${getTempDir().child(session_id).also { if (it.exists().not()){it.mkdirs()} }}",
                "TMPDIR=${getTempDir().absolutePath}",
                "ANDLINUX_DEFAULT_SHELL=${Settings.default_shell}",
                "SHELL=/bin/${Settings.default_shell}"
            )

            if (File(applicationInfo.nativeLibraryDir).child("libproot-loader32.so").exists()){
                env.add("PROOT_LOADER32=${applicationInfo.nativeLibraryDir}/libproot-loader32.so")
            }

            if (File(applicationInfo.nativeLibraryDir).child("libproot-loader.so").exists()){
                env.add("PROOT_LOADER=${applicationInfo.nativeLibraryDir}/libproot-loader.so")
            }

            if (Settings.seccomp) {
                env.add("SECCOMP=1")
            }

            env.addAll(envVariables.map { "${it.key}=${it.value}" })

            pendingCommand?.env?.let {
                env.addAll(it)
            }

            val args: Array<String>

            val shell = if (pendingCommand == null) {
                args = if (workingMode == WorkingMode.ALPINE){
                    arrayOf("-c", localBinDir().child("init-host").absolutePath)
                }else{
                    arrayOf()
                }
                "/system/bin/sh"
            } else{
                args = pendingCommand!!.args
                pendingCommand!!.shell
            }

            pendingCommand = null
            return TerminalSession(
                shell,
                workingDir,
                args,
                env.toTypedArray(),
                TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
                sessionClient,
            )
        }

    }
}