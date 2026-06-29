package com.rk.libcommons

import android.content.Context
import java.io.File
import com.rk.terminal.BuildConfig

private fun getFilesDir(): File{
    return if (application == null){
        // Fallback only used very early during process init. Keep it in sync with the
        // actual applicationId declared in app/build.gradle.kts (and its .debug suffix).
        if (BuildConfig.DEBUG){
            File("/data/data/com.term.andlinux.debug/files")
        }else{
            File("/data/data/com.term.andlinux/files")
        }
    }else{
        application!!.filesDir
    }
}

fun localDir(): File {
    return File(getFilesDir().parentFile, "local").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

fun alpineDir(): File{
    return localDir().child("alpine").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

fun alpineHomeDir(): File{
    return alpineDir().child("root").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

fun localBinDir(): File {
    return localDir().child("bin").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

fun localLibDir(): File {
    return localDir().child("lib").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }
}

fun File.child(fileName:String):File {
    return File(this,fileName)
}

fun File.createFileIfNot():File{
    if (exists().not()){
        createNewFile()
    }
    return this
}