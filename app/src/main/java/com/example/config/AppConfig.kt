package com.example.config

object AppConfig {
    // CHANGE ONLY THIS ONE LINE WHEN BUILDING FOR A NEW CLIENT DOMAIN:
    const val DEFAULT_DOMAIN = "https://snow.akfasft.com"

    var activeDomain: String = DEFAULT_DOMAIN

    val apiEndpoint: String
        get() = "${activeDomain.trimEnd('/')}/api/routes.php"

    fun resolveImageUrl(path: String?): String {
        if (path.isNullOrBlank() || path == "null") return ""
        val trimmed = path.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        return "${activeDomain.trimEnd('/')}/${trimmed.trimStart('/')}"
    }
}
