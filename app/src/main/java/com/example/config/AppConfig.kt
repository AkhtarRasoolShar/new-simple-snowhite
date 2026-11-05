package com.example.config

import android.content.Context

enum class AppEnvironment(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val domain: String
) {
    PRODUCTION(
        id = "PRODUCTION",
        displayName = "Production (Live Server)",
        baseUrl = "https://snowhiteltd.com/api/routes.php",
        domain = "https://snowhiteltd.com"
    ),
    STAGING(
        id = "STAGING",
        displayName = "Staging (Test Server)",
        baseUrl = "https://snow.akfasft.com/api/routes.php",
        domain = "https://snow.akfasft.com"
    );

    companion object {
        fun fromId(id: String?): AppEnvironment {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PRODUCTION
        }
    }
}

object AppConfig {
    private const val PREFS_NAME = "snowhite_app_config_prefs"
    private const val KEY_ENVIRONMENT = "selected_environment"

    var currentEnvironment: AppEnvironment = AppEnvironment.PRODUCTION
        private set

    val DEFAULT_DOMAIN: String
        get() = currentEnvironment.domain

    val activeDomain: String
        get() = currentEnvironment.domain

    val apiEndpoint: String
        get() = currentEnvironment.baseUrl

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_ENVIRONMENT, AppEnvironment.PRODUCTION.id)
        currentEnvironment = AppEnvironment.fromId(savedId)
    }

    fun setEnvironment(context: Context, environment: AppEnvironment) {
        currentEnvironment = environment
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ENVIRONMENT, environment.id).apply()
    }

    fun resolveImageUrl(path: String?): String {
        if (path.isNullOrBlank() || path == "null") return ""
        val trimmed = path.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        return "${activeDomain.trimEnd('/')}/${trimmed.trimStart('/')}"
    }
}
