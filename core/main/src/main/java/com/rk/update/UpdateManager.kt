package com.rk.update

import com.rk.libcommons.application
import com.rk.libcommons.child
import com.rk.libcommons.localBinDir
import java.io.File

/**
 * Refreshes the init scripts in $PREFIX/local/bin on each app start.
 *
 * This makes sure the user always runs the init scripts that match the
 * currently-installed APK, even after an in-place upgrade.
 */
class UpdateManager {
    fun onUpdate() {
        // Always overwrite init scripts — they are app-owned and the APK is
        // the source of truth. Skipping this when the file exists would leave
        // stale scripts in place after an APK upgrade.
        listOf("init-host" to "init-host.sh", "init" to "init.sh").forEach { (binName, assetName) ->
            val target: File = localBinDir().child(binName)
            target.parentFile?.mkdirs()
            application!!.assets.open(assetName).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.setExecutable(true, false)
        }
    }
}
