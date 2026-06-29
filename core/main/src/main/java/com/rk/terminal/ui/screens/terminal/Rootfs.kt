package com.rk.terminal.ui.screens.terminal

import androidx.compose.runtime.mutableStateOf
import com.rk.libcommons.application
import com.rk.libcommons.child

/**
 * Holds the working directory used by the Alpine downloader and proot init scripts.
 *
 * Historically this was called `reTerminal`; it has been renamed to `andLinux` so the
 * source code matches the project identity. The on-disk location itself did not change.
 */
object Rootfs {
    val andLinux = application!!.filesDir

    init {
        if (!andLinux.exists()) {
            andLinux.mkdirs()
        }
    }

    var isDownloaded = mutableStateOf(isFilesDownloaded())

    fun isFilesDownloaded(): Boolean {
        return andLinux.exists() &&
            andLinux.child("proot").exists() &&
            andLinux.child("libtalloc.so.2").exists() &&
            andLinux.child("alpine.tar.gz").exists()
    }
}
