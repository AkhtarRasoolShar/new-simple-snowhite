package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.R
import com.example.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object SnowhiteImageCache {
    private val prefetchScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = (maxMemoryKb / 4).coerceAtLeast(8 * 1024)

    val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return (bitmap.byteCount / 1024).coerceAtLeast(1)
        }
    }

    @Volatile
    private var coilImageLoader: ImageLoader? = null

    fun getOrCreateCoilImageLoader(context: Context): ImageLoader {
        coilImageLoader?.let { return it }
        synchronized(this) {
            coilImageLoader?.let { return it }
            val appCtx = context.applicationContext
            val loader = ImageLoader.Builder(appCtx)
                .memoryCache {
                    MemoryCache.Builder(appCtx)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(appCtx.filesDir.resolve("image_cache"))
                        .maxSizeBytes(150L * 1024 * 1024)
                        .build()
                }
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .respectCacheHeaders(false)
                .crossfade(false)
                .build()
            Coil.setImageLoader(loader)
            coilImageLoader = loader
            return loader
        }
    }

    fun hashUrl(url: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(url.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            url.hashCode().toUInt().toString(16)
        }
    }

    private fun getCacheDirs(context: Context): List<File> {
        val appCtx = context.applicationContext
        val primaryPermanentDir = appCtx.filesDir.resolve("snowhite_image_cache").apply { if (!exists()) mkdirs() }
        val legacyPermanentDir = appCtx.filesDir.resolve("snowhite_images").apply { if (!exists()) mkdirs() }
        val cacheDir = appCtx.cacheDir.resolve("snowhite_images").apply { if (!exists()) mkdirs() }
        return listOf(primaryPermanentDir, legacyPermanentDir, cacheDir)
    }

    fun getMemoryBitmap(url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        return synchronized(memoryCache) {
            memoryCache.get(url)
        }
    }

    fun putMemoryBitmap(url: String, bitmap: Bitmap) {
        synchronized(memoryCache) {
            if (memoryCache.get(url) == null) {
                memoryCache.put(url, bitmap)
            }
        }
    }

    fun loadFromDisk(context: Context, url: String): Bitmap? {
        if (url.isBlank()) return null
        getMemoryBitmap(url)?.let { return it }

        val hashedName = hashUrl(url)
        val dirs = getCacheDirs(context)
        for (dir in dirs) {
            val file = File(dir, hashedName)
            if (file.exists() && file.length() > 0L) {
                try {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    if (bmp != null) {
                        putMemoryBitmap(url, bmp)
                        // Ensure both cacheDir and permanent filesDir have the copy
                        val otherDir = dirs.firstOrNull { it != dir }
                        if (otherDir != null) {
                            val otherFile = File(otherDir, hashedName)
                            if (!otherFile.exists() || otherFile.length() == 0L) {
                                runCatching { file.copyTo(otherFile, overwrite = true) }
                            }
                        }
                        return bmp
                    }
                } catch (_: Exception) {
                }
            }
        }
        return null
    }

    fun downloadAndCache(context: Context, url: String): Bitmap? {
        if (url.isBlank()) return null
        getMemoryBitmap(url)?.let { return it }
        loadFromDisk(context, url)?.let { return it }

        return try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
                connectTimeout = 10000
                readTimeout = 10000
                doInput = true
            }
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val bytes = connection.inputStream.use { it.readBytes() }
                if (bytes.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        val hashedName = hashUrl(url)
                        for (dir in getCacheDirs(context)) {
                            runCatching {
                                File(dir, hashedName).writeBytes(bytes)
                            }
                        }
                        putMemoryBitmap(url, bitmap)
                        return bitmap
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun prefetchImages(context: Context?, urls: Collection<String?>) {
        val ctx = context?.applicationContext ?: AppConfig.appContext ?: return
        val loader = getOrCreateCoilImageLoader(ctx)
        val distinctUrls = urls.mapNotNull { raw ->
            if (raw.isNullOrBlank() || raw == "null") null
            else AppConfig.resolveImageUrl(raw).takeIf { it.isNotBlank() }
        }.distinct()

        for (resolvedUrl in distinctUrls) {
            if (getMemoryBitmap(resolvedUrl) != null) continue
            prefetchScope.launch {
                val bmp = loadFromDisk(ctx, resolvedUrl) ?: downloadAndCache(ctx, resolvedUrl)
                if (bmp == null) {
                    runCatching {
                        val req = ImageRequest.Builder(ctx)
                            .data(resolvedUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                        loader.enqueue(req)
                    }
                }
            }
        }
    }

    fun clearCache(context: Context? = AppConfig.appContext) {
        synchronized(memoryCache) {
            memoryCache.evictAll()
        }
        val ctx = context?.applicationContext ?: AppConfig.appContext ?: return
        runCatching {
            getCacheDirs(ctx).forEach { dir ->
                dir.listFiles()?.forEach { it.delete() }
            }
        }
    }
}

@Composable
fun SnowhiteNetworkImage(
    url: String?,
    productName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resolvedUrl = remember(url, productName) { resolveImageUrl(url, productName) }

    // 1. Check In-Memory LruCache synchronously on the very first frame (zero flicker, no spinner)
    val initialMemBitmap = remember(resolvedUrl) {
        SnowhiteImageCache.getMemoryBitmap(resolvedUrl)
    }

    var bitmapState by remember(resolvedUrl) { mutableStateOf(initialMemBitmap) }

    if (bitmapState == null && resolvedUrl.isNotBlank()) {
        LaunchedEffect(resolvedUrl) {
            // 2. Check Disk Cache on Dispatchers.IO, or download & save permanently to Disk + LruCache
            val loaded = withContext(Dispatchers.IO) {
                SnowhiteImageCache.loadFromDisk(context, resolvedUrl)
                    ?: SnowhiteImageCache.downloadAndCache(context, resolvedUrl)
                    ?: run {
                        val fallbackUrl = getUnsplashFallbackUrl(productName)
                        if (fallbackUrl != resolvedUrl) {
                            SnowhiteImageCache.loadFromDisk(context, fallbackUrl)
                                ?: SnowhiteImageCache.downloadAndCache(context, fallbackUrl)
                        } else null
                    }
            }
            if (loaded != null) {
                bitmapState = loaded
            }
        }
    }

    val currentBitmap = bitmapState ?: initialMemBitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap.asImageBitmap(),
            contentDescription = productName,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Instant local drawable placeholder without any loading spinner flicker
        val isFolded = productName.contains("Folded", ignoreCase = true) ||
                productName.contains("Wash", ignoreCase = true) ||
                productName.contains("Powder", ignoreCase = true)
        Box(
            modifier = modifier.background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = if (isFolded) R.drawable.img_folded_clothes_bg else R.drawable.img_dryclean_bg
                ),
                contentDescription = productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}

fun resolveImageUrl(url: String?, productName: String): String {
    if (url.isNullOrBlank() || url == "null") {
        return getUnsplashFallbackUrl(productName)
    }
    return AppConfig.resolveImageUrl(url)
}

fun getUnsplashFallbackUrl(productName: String): String {
    val nameLower = productName.lowercase()
    return when {
        nameLower.contains("suit") -> "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&q=80"
        nameLower.contains("shirt") -> "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600&q=80"
        nameLower.contains("kurta") || nameLower.contains("shalwar") || nameLower.contains("qameez") -> "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=600&q=80"
        nameLower.contains("sherwani") || nameLower.contains("achkan") -> "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=600&q=80"
        nameLower.contains("dress") || nameLower.contains("gown") || nameLower.contains("saree") -> "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=600&q=80"
        nameLower.contains("blanket") || nameLower.contains("comforter") || nameLower.contains("duvet") -> "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=600&q=80"
        nameLower.contains("curtain") -> "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600&q=80"
        nameLower.contains("powder") || nameLower.contains("detergent") || nameLower.contains("softener") || nameLower.contains("liquid") -> "https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=600&q=80"
        else -> "https://images.unsplash.com/photo-1585421514738-01798e348b17?w=600&q=80"
    }
}
