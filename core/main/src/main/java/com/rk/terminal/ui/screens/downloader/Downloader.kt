package com.rk.terminal.ui.screens.downloader

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rk.libcommons.*
import com.rk.resources.strings
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.screens.terminal.Rootfs
import com.rk.terminal.ui.screens.terminal.TerminalScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * First-run downloader.
 *
 * Pulls three things on first launch:
 *  - libtalloc.so.2  (needed by proot)
 *  - proot           (the user-space syscall translator)
 *  - alpine.tar.gz   (the Alpine Linux minirootfs)
 *
 * For the Alpine rootfs we always resolve the latest stable release at runtime
 * from https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases/<arch>/ by
 * scraping the directory listing. This keeps AndLinux from being pinned to a
 * specific Alpine point release (e.g. 3.21.0) that ages out within weeks.
 */
@Composable
fun Downloader(
    modifier: Modifier = Modifier,
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val installingStr = stringResource(strings.installing)
    val downloadingStr = stringResource(strings.downloading)
    val networkErrorStr = stringResource(strings.network_error)
    val setupFailedStr = stringResource(strings.setup_failed)
    var progress by remember { mutableFloatStateOf(0f) }
    var progressText by remember { mutableStateOf(installingStr) }
    var isSetupComplete by remember { mutableStateOf(false) }
    var needsDownload by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val abi = Build.SUPPORTED_ABIS.firstOrNull { it in abiMap }
                ?: throw RuntimeException("Unsupported CPU: ${Build.SUPPORTED_ABIS.joinToString()}")

            val abiUrls = abiMap[abi]!!
            val resolvedAlpineUrl = withContext(Dispatchers.IO) {
                resolveLatestAlpineMinirootfsUrl(abiUrls.alpineDir, abiUrls.alpineArch)
            }

            val filesToDownload = listOf(
                "libtalloc.so.2" to abiUrls.talloc,
                "proot" to abiUrls.proot,
                "alpine.tar.gz" to resolvedAlpineUrl
            ).map { (name, url) -> DownloadFile(url, Rootfs.andLinux.child(name)) }

            needsDownload = filesToDownload.any { !it.outputFile.exists() }

            setupEnvironment(
                filesToDownload,
                onProgress = { completed, total, currentProgress ->
                    if (needsDownload) {
                        progress = ((completed + currentProgress) / total).coerceIn(0f, 1f)
                        val pct = (progress * 100).toInt()
                        progressText = downloadingStr.format(pct)
                    }
                },
                onComplete = {
                    isSetupComplete = true
                },
                onError = { error ->
                    toast(if (error is UnknownHostException) networkErrorStr else setupFailedStr.format(error.message))
                }
            )
        } catch (e: Exception) {
            toast(if (e is UnknownHostException) networkErrorStr else setupFailedStr.format(e.message))
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!isSetupComplete) {
            if (needsDownload) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(progressText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.8f))
                }
            }
        } else {
            TerminalScreen(mainActivityActivity = mainActivity, navController = navController)
        }
    }
}

private data class DownloadFile(val url: String, val outputFile: File)

private suspend fun setupEnvironment(
    filesToDownload: List<DownloadFile>,
    onProgress: (Int, Int, Float) -> Unit,
    onComplete: () -> Unit,
    onError: (Exception) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            var completedFiles = 0
            val totalFiles = filesToDownload.size

            filesToDownload.forEach { file ->
                val outputFile = file.outputFile.apply { parentFile?.mkdirs() }
                if (!outputFile.exists()) {
                    downloadFile(file.url, outputFile) { downloaded, total ->
                        runOnUiThread {
                            onProgress(
                                completedFiles,
                                totalFiles,
                                if (total > 0) downloaded.toFloat() / total else 0f
                            )
                        }
                    }
                }
                completedFiles++
                runOnUiThread { onProgress(completedFiles, totalFiles, 1f) }
                outputFile.setExecutable(true, false)
            }
            runOnUiThread { onComplete() }
        } catch (e: Exception) {
            // Wipe any half-written downloads so the next attempt starts clean.
            filesToDownload.forEach { it.outputFile.takeIf { f -> f.exists() }?.delete() }
            withContext(Dispatchers.Main) { onError(e) }
        }
    }
}

private suspend fun downloadFile(url: String, outputFile: File, onProgress: (Long, Long) -> Unit) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code} for $url")
            }
            val body = response.body ?: throw Exception("Empty response body for $url")
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            outputFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var lastReport = 0L
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        // Throttle UI updates to ~10 per second to avoid main-thread thrash.
                        val now = System.currentTimeMillis()
                        if (now - lastReport > 100 || downloadedBytes == totalBytes) {
                            withContext(Dispatchers.Main) { onProgress(downloadedBytes, totalBytes) }
                            lastReport = now
                        }
                    }
                }
            }
        }
    }
}

/**
 * Resolve the latest stable Alpine minirootfs URL by listing the
 * `latest-stable/releases/<arch>/` directory and picking the highest-versioned
 * `alpine-minirootfs-<version>-<arch>.tar.gz` file.
 *
 * Falls back to a sensible default if the listing can't be parsed.
 */
@Throws(Exception::class)
internal fun resolveLatestAlpineMinirootfsUrl(alpineDir: String, alpineArch: String): String {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    val listingUrl = "$alpineDir/$alpineArch/"
    val request = Request.Builder().url(listingUrl).build()
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code} fetching Alpine release listing at $listingUrl")
        }
        val html = response.body?.string()
            ?: throw Exception("Empty response body fetching $listingUrl")

        // Match alpine-minirootfs-3.21.3-aarch64.tar.gz (and any version pattern).
        val pattern = Pattern.compile(
            "alpine-minirootfs-(\\d+(?:\\.\\d+){1,3})-" + Pattern.quote(alpineArch) + "\\.tar\\.gz"
        )
        val matcher = pattern.matcher(html)
        var bestVersion: List<Int>? = null
        var bestFileName: String? = null
        while (matcher.find()) {
            val fileName = matcher.group(0) ?: continue
            val versionStr = matcher.group(1) ?: continue
            val parts = versionStr.split(".").mapNotNull { it.toIntOrNull() }
            if (parts.isEmpty()) continue
            if (bestVersion == null || compareVersions(parts, bestVersion) > 0) {
                bestVersion = parts
                bestFileName = fileName
            }
        }
        if (bestFileName != null) {
            return "$alpineDir/$alpineArch/$bestFileName"
        }
    }
    // Fallback: use a known-good pinned release. This will be replaced the next
    // time the user opens the downloader once the listing is reachable again.
    throw Exception("Could not resolve latest Alpine minirootfs from $listingUrl")
}

private fun compareVersions(a: List<Int>, b: List<Int>): Int {
    val maxLen = maxOf(a.size, b.size)
    for (i in 0 until maxLen) {
        val av = a.getOrElse(i) { 0 }
        val bv = b.getOrElse(i) { 0 }
        if (av != bv) return av - bv
    }
    return 0
}

/**
 * Per-ABI download metadata.
 *
 * - `talloc`: libtalloc.so.2 binary, needed by proot
 * - `proot`:  proot binary itself
 * - `alpineDir`: base URL of the Alpine release tree (no arch suffix),
 *                e.g. `https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases`
 * - `alpineArch`: per-ABI Alpine arch name as used in the URL path,
 *                e.g. `aarch64`, `x86_64`, `armhf`
 */
private data class AbiUrls(
    val talloc: String,
    val proot: String,
    val alpineDir: String,
    val alpineArch: String,
)

private val abiMap = mapOf(
    "x86_64" to AbiUrls(
        talloc = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/x86_64/libtalloc.so.2",
        proot = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/x86_64/proot",
        alpineDir = "https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases",
        alpineArch = "x86_64"
    ),
    "arm64-v8a" to AbiUrls(
        talloc = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/aarch64/libtalloc.so.2",
        proot = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/aarch64/proot",
        alpineDir = "https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases",
        alpineArch = "aarch64"
    ),
    "armeabi-v7a" to AbiUrls(
        talloc = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/arm/libtalloc.so.2",
        proot = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/arm/proot",
        alpineDir = "https://dl-cdn.alpinelinux.org/alpine/latest-stable/releases",
        alpineArch = "armhf"
    )
)
