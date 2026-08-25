package com.example.data.updater

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class GitHubUpdateManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("travel_plus_updater_prefs", Context.MODE_PRIVATE)

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GitHubUpdateManager"
        private const val PREF_REPO_OWNER = "pref_repo_owner"
        private const val PREF_REPO_NAME = "pref_repo_name"
        private const val PREF_AUTO_CHECK = "pref_auto_check"
        private const val PREF_DISMISSED_TAG = "pref_dismissed_tag"
        private const val PREF_LAST_CHECK_TIME = "pref_last_check_time"

        const val DEFAULT_OWNER = "sgmatzev23"
        const val DEFAULT_REPO = "travel-plus"
    }

    fun getRepoOwner(): String = prefs.getString(PREF_REPO_OWNER, DEFAULT_OWNER) ?: DEFAULT_OWNER
    fun setRepoOwner(owner: String) = prefs.edit().putString(PREF_REPO_OWNER, owner.trim()).apply()

    fun getRepoName(): String = prefs.getString(PREF_REPO_NAME, DEFAULT_REPO) ?: DEFAULT_REPO
    fun setRepoName(repo: String) = prefs.edit().putString(PREF_REPO_NAME, repo.trim()).apply()

    fun isAutoCheckEnabled(): Boolean = prefs.getBoolean(PREF_AUTO_CHECK, true)
    fun setAutoCheckEnabled(enabled: Boolean) = prefs.edit().putBoolean(PREF_AUTO_CHECK, enabled).apply()

    fun getDismissedTag(): String? = prefs.getString(PREF_DISMISSED_TAG, null)
    fun setDismissedTag(tag: String) = prefs.edit().putString(PREF_DISMISSED_TAG, tag).apply()
    fun clearDismissedTag() = prefs.edit().remove(PREF_DISMISSED_TAG).apply()

    fun getLastCheckTime(): Long = prefs.getLong(PREF_LAST_CHECK_TIME, 0L)
    fun setLastCheckTime(time: Long) = prefs.edit().putLong(PREF_LAST_CHECK_TIME, time).apply()

    /**
     * Retrieves current app version name from PackageInfo (defaults to 1.0.0).
     */
    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            Log.w(TAG, "Could not get package info version", e)
            "1.0.0"
        }
    }

    /**
     * Checks for the latest release on GitHub.
     */
    suspend fun checkForUpdates(
        owner: String = getRepoOwner(),
        repo: String = getRepoName(),
        forceCheck: Boolean = false
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val currentVersion = getCurrentVersionName()
        setLastCheckTime(System.currentTimeMillis())

        try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("User-Agent", "TravelPlus-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                val msg = when (response.code) {
                    404 -> "No GitHub releases found for '$owner/$repo'. Ensure releases are published."
                    403 -> "GitHub API rate limit reached. Please try again shortly."
                    else -> "GitHub API error (${response.code}): ${response.message}"
                }
                Log.w(TAG, "Update check failed: $msg. Body: $errorBody")
                return@withContext UpdateCheckResult.Error(msg, currentVersion)
            }

            val responseBody = response.body?.string()
                ?: return@withContext UpdateCheckResult.Error("Empty response from GitHub API", currentVersion)

            val json = JSONObject(responseBody)
            val release = parseReleaseJson(json)

            val isNewer = isVersionNewer(currentVersion, release.cleanVersion)

            if (isNewer) {
                UpdateCheckResult.UpdateAvailable(
                    release = release,
                    currentVersion = currentVersion,
                    isNewer = true
                )
            } else {
                UpdateCheckResult.UpToDate(
                    currentVersion = currentVersion,
                    latestRelease = release
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            UpdateCheckResult.Error(
                message = e.localizedMessage ?: "Failed to connect to GitHub releases",
                currentVersion = currentVersion
            )
        }
    }

    private fun parseReleaseJson(json: JSONObject): GitHubRelease {
        val tagName = json.optString("tag_name", "")
        val name = json.optString("name", tagName)
        val body = json.optString("body", "No release notes provided.")
        val htmlUrl = json.optString("html_url", "")
        val publishedAt = json.optString("published_at", "")
        val isPrerelease = json.optBoolean("prerelease", false)
        val isDraft = json.optBoolean("draft", false)

        var apkDownloadUrl: String? = null
        var apkName: String? = null
        var apkSize: Long = 0L

        val assets = json.optJSONArray("assets")
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                val assetName = asset.optString("name", "")
                val contentType = asset.optString("content_type", "")
                val downloadUrl = asset.optString("browser_download_url", "")
                val size = asset.optLong("size", 0L)

                if (assetName.endsWith(".apk", ignoreCase = true) ||
                    contentType.equals("application/vnd.android.package-archive", ignoreCase = true)
                ) {
                    apkDownloadUrl = downloadUrl
                    apkName = assetName
                    apkSize = size
                    break
                }
            }
        }

        return GitHubRelease(
            tagName = tagName,
            name = if (name.isNotBlank()) name else tagName,
            body = body,
            htmlUrl = htmlUrl,
            publishedAt = publishedAt,
            apkDownloadUrl = apkDownloadUrl,
            apkName = apkName,
            apkSize = apkSize,
            isPrerelease = isPrerelease,
            isDraft = isDraft
        )
    }

    /**
     * Compares semantic versioning strings.
     * Returns true if remoteVersion > currentVersion.
     */
    fun isVersionNewer(currentVersion: String, remoteVersion: String): Boolean {
        try {
            val currParts = parseVersionParts(currentVersion)
            val remoteParts = parseVersionParts(remoteVersion)

            val maxLen = maxOf(currParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val curr = currParts.getOrElse(i) { 0 }
                val remote = remoteParts.getOrElse(i) { 0 }
                if (remote > curr) return true
                if (remote < curr) return false
            }
            return false
        } catch (_: Exception) {
            // Fallback string compare if irregular tag
            return remoteVersion.trimStart('v', 'V') != currentVersion.trimStart('v', 'V')
        }
    }

    private fun parseVersionParts(version: String): List<Int> {
        val clean = version.trimStart('v', 'V', 'r', 'R').trim()
        val segments = clean.split('.', '-', '_')
        return segments.mapNotNull { segment ->
            segment.takeWhile { it.isDigit() }.toIntOrNull()
        }
    }

    /**
     * Downloads the APK file to cache directory and notifies progress.
     */
    suspend fun downloadApk(
        release: GitHubRelease,
        onProgress: (DownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        val downloadUrl = release.apkDownloadUrl
        if (downloadUrl.isNullOrBlank()) {
            onProgress(DownloadState.Error("No APK asset attached to this GitHub release. You can open the release page in your browser."))
            return@withContext
        }

        try {
            onProgress(DownloadState.Downloading(0L, release.apkSize, 0))

            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }

            val fileName = release.apkName ?: "travel-plus-${release.cleanVersion}.apk"
            val targetFile = File(updateDir, fileName)

            val request = Request.Builder()
                .url(downloadUrl)
                .addHeader("User-Agent", "TravelPlus-App")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                onProgress(DownloadState.Error("Failed to download APK (HTTP ${response.code})"))
                return@withContext
            }

            val body = response.body
            if (body == null) {
                onProgress(DownloadState.Error("Response body is empty"))
                return@withContext
            }

            val totalBytes = if (body.contentLength() > 0) body.contentLength() else release.apkSize
            var bytesDownloaded = 0L

            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(targetFile)

            val buffer = ByteArray(8 * 1024)
            var read: Int
            var lastReportPercent = -1

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                bytesDownloaded += read
                val percentage = if (totalBytes > 0) {
                    ((bytesDownloaded * 100) / totalBytes).toInt()
                } else 0

                if (percentage != lastReportPercent) {
                    lastReportPercent = percentage
                    onProgress(DownloadState.Downloading(bytesDownloaded, totalBytes, percentage))
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            if (targetFile.exists() && targetFile.length() > 0) {
                onProgress(DownloadState.ReadyToInstall(targetFile, release))
            } else {
                onProgress(DownloadState.Error("Downloaded file is empty or corrupted"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK", e)
            onProgress(DownloadState.Error(e.localizedMessage ?: "Download failed"))
        }
    }

    /**
     * Prompts the Android OS package installer to install the downloaded APK.
     */
    fun installApk(file: File): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error launching package installer", e)
            false
        }
    }

    /**
     * Demo simulation release object for testing the UI, changelog view, and dialog.
     */
    fun createDemoRelease(): GitHubRelease {
        return GitHubRelease(
            tagName = "v1.2.0",
            name = "Travel Plus v1.2.0 - Multi-City AI Itineraries & Live Flight Radar",
            body = """
### 🚀 What's New in v1.2.0

* **✨ Multi-City AI Planner**: Seamlessly chain multi-destination itineraries with auto-computed transit gaps.
* **🛡️ Live Consular Advisories**: Real-time safety alert badges and emergency consulate contacts for 180+ countries.
* **💳 Foreign Currency Optimizer**: Real-time currency exchange rates with zero-fee credit card recommendation engine.
* **📦 Smart Packing Weather-Sync**: Dynamic packing checklist auto-adapts to 14-day destination forecast.
* **⚡ GitHub In-App Auto-Updates**: Instant update detection with interactive markdown changelogs and one-tap APK installation.

### 🐛 Bug Fixes & Improvements

* Fixed dark-mode contrast issues in booking timeline.
* Optimized offline document vault encryption performance.
* Enhanced OpenRouter auto-failover speed on rate-limited endpoints.
            """.trimIndent(),
            htmlUrl = "https://github.com/${getRepoOwner()}/${getRepoName()}/releases",
            publishedAt = "2026-08-25T00:00:00Z",
            apkDownloadUrl = "https://github.com/${getRepoOwner()}/${getRepoName()}/releases/download/v1.2.0/travel-plus-debug-apk.apk",
            apkName = "travel-plus-v1.2.0.apk",
            apkSize = 28 * 1024 * 1024L,
            isPrerelease = false
        )
    }
}
