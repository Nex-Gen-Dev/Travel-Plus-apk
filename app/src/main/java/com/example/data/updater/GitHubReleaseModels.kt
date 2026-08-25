package com.example.data.updater

import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class GitHubRelease(
    val tagName: String,
    val name: String,
    val body: String,
    val htmlUrl: String,
    val publishedAt: String,
    val apkDownloadUrl: String?,
    val apkName: String?,
    val apkSize: Long = 0L,
    val isPrerelease: Boolean = false,
    val isDraft: Boolean = false
) {
    val cleanVersion: String
        get() = tagName.trimStart('v', 'V', 'r', 'R').trim()

    val formattedDate: String
        get() = try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = isoFormat.parse(publishedAt)
            val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            if (date != null) displayFormat.format(date) else publishedAt.take(10)
        } catch (_: Exception) {
            publishedAt.take(10)
        }

    val formattedSize: String
        get() = when {
            apkSize <= 0 -> ""
            apkSize < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", apkSize / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", apkSize / (1024.0 * 1024.0))
        }
}

sealed class UpdateCheckResult {
    data object Idle : UpdateCheckResult()
    data object Checking : UpdateCheckResult()
    data class UpdateAvailable(
        val release: GitHubRelease,
        val currentVersion: String,
        val isNewer: Boolean
    ) : UpdateCheckResult()
    data class UpToDate(
        val currentVersion: String,
        val latestRelease: GitHubRelease?
    ) : UpdateCheckResult()
    data class Error(
        val message: String,
        val currentVersion: String
    ) : UpdateCheckResult()
}

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percentage: Int
    ) : DownloadState()
    data class ReadyToInstall(
        val file: File,
        val release: GitHubRelease
    ) : DownloadState()
    data class Error(
        val message: String
    ) : DownloadState()
}

data class GitHubRepoConfig(
    val owner: String = "sgmatzev23",
    val repo: String = "travel-plus",
    val autoCheckOnStartup: Boolean = true
)
