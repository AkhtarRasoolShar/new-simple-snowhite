package com.example.network

import android.content.Context
import com.example.config.AppConfig
import com.example.network.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class SnowhiteRepository {
    private val apiEndpoint: String get() = AppConfig.apiEndpoint
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        private const val OFFLINE_CACHE_FILENAME = "snowhite_offline_cache.json"
        private const val OFFLINE_PREFS_NAME = "snowhite_offline_store"
        private const val KEY_OFFLINE_JSON = "offline_cache_json"
        private val cacheLock = Any()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private inline fun <reified T> parseApiResponse(jsonString: String): ApiResponse<T>? {
        return try {
            val type = Types.newParameterizedType(ApiResponse::class.java, T::class.java)
            val adapter = moshi.adapter<ApiResponse<T>>(type)
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun <T> parseApiResponseList(jsonString: String, elementClass: Class<T>): ApiResponse<List<T>>? {
        return try {
            val listType = Types.newParameterizedType(List::class.java, elementClass)
            val apiResponseType = Types.newParameterizedType(ApiResponse::class.java, listType)
            val adapter = moshi.adapter<ApiResponse<List<T>>>(apiResponseType)
            adapter.fromJson(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun executeGet(endpointUrl: String): String {
        val request = Request.Builder()
            .url(endpointUrl)
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP error ${response.code}: ${response.message}")
        }
        return response.body?.string() ?: throw IOException("Empty response body")
    }

    private fun executePost(endpointUrl: String, jsonBody: String): String {
        val requestBody = jsonBody.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(endpointUrl)
            .post(requestBody)
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP error ${response.code}: ${response.message}")
        }
        return response.body?.string() ?: throw IOException("Empty response body")
    }

    // ==================== OFFLINE JSON PERSISTENCE ====================

    private fun readOfflineRootJson(context: Context? = AppConfig.appContext): JSONObject {
        val ctx = context ?: return JSONObject()
        synchronized(cacheLock) {
            try {
                val file = File(ctx.filesDir, OFFLINE_CACHE_FILENAME)
                if (file.exists() && file.length() > 0L) {
                    val text = file.readText(Charsets.UTF_8)
                    if (text.isNotBlank()) {
                        return JSONObject(text)
                    }
                }
            } catch (_: Exception) {
            }
            try {
                val prefs = ctx.getSharedPreferences(OFFLINE_PREFS_NAME, Context.MODE_PRIVATE)
                val saved = prefs.getString(KEY_OFFLINE_JSON, null)
                if (!saved.isNullOrBlank()) {
                    return JSONObject(saved)
                }
            } catch (_: Exception) {
            }
            return JSONObject()
        }
    }

    private fun writeOfflineRootJson(root: JSONObject, context: Context? = AppConfig.appContext) {
        val ctx = context ?: return
        synchronized(cacheLock) {
            val jsonStr = root.toString()
            try {
                File(ctx.filesDir, OFFLINE_CACHE_FILENAME).writeText(jsonStr, Charsets.UTF_8)
            } catch (_: Exception) {
            }
            try {
                ctx.getSharedPreferences(OFFLINE_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_OFFLINE_JSON, jsonStr)
                    .apply()
            } catch (_: Exception) {
            }
        }
    }

    fun saveOfflineCategories(categories: List<Category>, context: Context? = AppConfig.appContext) {
        val root = readOfflineRootJson(context)
        val arr = JSONArray()
        categories.forEach { cat ->
            arr.put(JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("is_active", cat.isActive ?: 1)
                put("image_url", cat.imageUrl ?: "")
                put("sort_order", cat.sort_order ?: 999)
            })
        }
        root.put("categories", arr)
        writeOfflineRootJson(root, context)
    }

    fun saveOfflineProducts(products: List<Product>, context: Context? = AppConfig.appContext) {
        val root = readOfflineRootJson(context)
        val arr = JSONArray()
        products.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                if (p.categoryId != null) put("category_id", p.categoryId)
                put("category_name", p.categoryName ?: "")
                put("service_type", p.serviceType ?: "Dry Clean")
                put("price", p.price ?: "0.00")
                put("description", p.description ?: "")
                put("image_url", p.imageUrl ?: "")
                put("is_active", p.activeInt)
                put("discount_tier", p.discountTier ?: "auto")
                put("manual_discount", p.numericManualDiscount)
                put("sort_order", p.sort_order ?: 999)
                if (p.real_id != null) put("real_id", p.real_id)
            })
        }
        root.put("products", arr)
        writeOfflineRootJson(root, context)
    }

    fun saveOfflineBanners(banners: List<Banner>, context: Context? = AppConfig.appContext) {
        val root = readOfflineRootJson(context)
        val arr = JSONArray()
        banners.forEach { b ->
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("title", b.title ?: "")
                put("subtitle", b.subtitle ?: "")
                put("image_url", b.imageUrl ?: "")
                put("link", b.link ?: "")
                put("is_active", b.isActive ?: 1)
                put("sort_order", b.sort_order ?: 999)
            })
        }
        root.put("banners", arr)
        writeOfflineRootJson(root, context)
    }

    fun saveOfflineSettings(settings: GlobalSettings, context: Context? = AppConfig.appContext) {
        val root = readOfflineRootJson(context)
        val obj = JSONObject().apply {
            put("app_name", settings.appName ?: "Snowhite")
            put("currency", settings.currency ?: "PKR")
            put("delivery_fee", settings.deliveryFee ?: "150")
            put("whatsapp_number", settings.whatsappNumber ?: "03111110500")
            put("support_email", settings.supportEmail ?: "cs@snowhite.com.pk")
            put("logo_url", settings.logoUrl ?: "")
            put("sw_premium_discount", settings.swPremiumDiscount?.toString() ?: "30")
            put("sw_regular_discount", settings.swRegularDiscount?.toString() ?: "20")
            put("default_free_orders", settings.defaultFreeOrders?.toString() ?: "0")
        }
        root.put("settings", obj)
        writeOfflineRootJson(root, context)
    }

    fun loadOfflineSettings(context: Context? = AppConfig.appContext): GlobalSettings? {
        val root = readOfflineRootJson(context)
        val obj = root.optJSONObject("settings") ?: return null
        return GlobalSettings(
            appName = obj.optString("app_name", "Snowhite"),
            currency = obj.optString("currency", "PKR"),
            deliveryFee = obj.optString("delivery_fee", "150"),
            whatsappNumber = obj.optString("whatsapp_number", "03111110500"),
            supportEmail = obj.optString("support_email", "cs@snowhite.com.pk"),
            logoUrl = obj.optString("logo_url").takeIf { it.isNotBlank() },
            swPremiumDiscount = obj.optString("sw_premium_discount", "30"),
            swRegularDiscount = obj.optString("sw_regular_discount", "20"),
            defaultFreeOrders = obj.optString("default_free_orders", "0")
        )
    }

    fun loadOfflineCategories(context: Context? = AppConfig.appContext): List<Category> {
        val root = readOfflineRootJson(context)
        val arr = root.optJSONArray("categories")
        if (arr != null && arr.length() > 0) {
            val parsed = parseCategoriesJsonArray(arr)
            if (parsed.isNotEmpty()) {
                return parsed.sortedBy { it.sort_order ?: 999 }
            }
        }
        return getDefaultFallbackCategories()
    }

    fun loadOfflineProducts(context: Context? = AppConfig.appContext): List<Product> {
        val root = readOfflineRootJson(context)
        val arr = root.optJSONArray("products")
        if (arr != null && arr.length() > 0) {
            val parsed = parseProductsJsonArray(arr)
            if (parsed.isNotEmpty()) {
                return parsed.sortedWith(compareBy({ it.sort_order ?: 999 }, { it.id }))
            }
        }
        return getDefaultFallbackProducts()
    }

    fun loadOfflineBanners(context: Context? = AppConfig.appContext): List<Banner> {
        val root = readOfflineRootJson(context)
        val arr = root.optJSONArray("banners")
        if (arr != null && arr.length() > 0) {
            val parsed = parseBannersJsonArray(arr)
            if (parsed.isNotEmpty()) {
                return parsed.sortedBy { it.sort_order ?: 999 }
            }
        }
        return emptyList()
    }

    private fun parseCategoriesJsonArray(arr: JSONArray): List<Category> {
        val result = mutableListOf<Category>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id").toLongOrNull() ?: obj.optLong("id", (i + 1).toLong())
            val name = obj.optString("name", "")
            if (name.isBlank()) continue
            val isActive = obj.optString("is_active", "1").toIntOrNull() ?: obj.optInt("is_active", 1)
            val imageUrl = obj.optString("image_url").takeIf { it.isNotBlank() && it != "null" }
            val sortOrder = if (obj.has("sort_order") && !obj.isNull("sort_order")) {
                obj.optString("sort_order").toIntOrNull() ?: obj.optInt("sort_order", 999)
            } else {
                999
            }
            if (isActive == 1) {
                result.add(
                    Category(
                        id = id,
                        name = name,
                        isActive = isActive,
                        imageUrl = imageUrl,
                        sort_order = sortOrder
                    )
                )
            }
        }
        return result
    }

    private fun parseProductsJsonArray(arr: JSONArray): List<Product> {
        val result = mutableListOf<Product>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id").toLongOrNull() ?: obj.optLong("id", (i + 1).toLong())
            val name = obj.optString("name", "")
            if (name.isBlank()) continue
            val categoryId = obj.optString("category_id").toLongOrNull()
                ?: if (obj.has("category_id") && !obj.isNull("category_id")) obj.optLong("category_id") else null
            val categoryName = obj.optString("category_name").takeIf { it.isNotBlank() && it != "null" }
            val serviceType = obj.optString("service_type", "Dry Clean").ifBlank { "Dry Clean" }
            val price = obj.optString("price", "0.00").ifBlank { "0.00" }
            val description = obj.optString("description", "")
            val imageUrl = obj.optString("image_url").takeIf { it.isNotBlank() && it != "null" }
            val isActive = obj.optString("is_active", "1").toIntOrNull() ?: obj.optInt("is_active", 1)
            val discountTier = obj.optString("discount_tier", "auto").ifBlank { "auto" }
            val manualDiscount = obj.optString("manual_discount", "0").toIntOrNull() ?: obj.optInt("manual_discount", 0)
            val sortOrder = if (obj.has("sort_order") && !obj.isNull("sort_order")) {
                obj.optString("sort_order").toIntOrNull() ?: obj.optInt("sort_order", 999)
            } else {
                999
            }
            val realId = if (obj.has("real_id") && !obj.isNull("real_id")) {
                obj.optString("real_id").toIntOrNull() ?: obj.optInt("real_id")
            } else {
                null
            }
            if (isActive == 1) {
                result.add(
                    Product(
                        id = id,
                        name = name,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        serviceType = serviceType,
                        price = price,
                        description = description,
                        imageUrl = imageUrl,
                        isActive = isActive,
                        discountTier = discountTier,
                        manualDiscount = manualDiscount,
                        sort_order = sortOrder,
                        real_id = realId
                    )
                )
            }
        }
        return result
    }

    private fun parseBannersJsonArray(arr: JSONArray): List<Banner> {
        val result = mutableListOf<Banner>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id").toLongOrNull() ?: obj.optLong("id", (i + 1).toLong())
            val title = obj.optString("title").takeIf { it.isNotBlank() && it != "null" }
            val subtitle = obj.optString("subtitle").takeIf { it.isNotBlank() && it != "null" }
            val imageUrl = (obj.optString("image_url").takeIf { it.isNotBlank() && it != "null" }
                ?: obj.optString("image").takeIf { it.isNotBlank() && it != "null" }
                ?: obj.optString("banner_url").takeIf { it.isNotBlank() && it != "null" })
            val link = obj.optString("link").takeIf { it.isNotBlank() && it != "null" }
            val isActive = obj.optString("is_active", "1").toIntOrNull() ?: obj.optInt("is_active", 1)
            val sortOrder = if (obj.has("sort_order") && !obj.isNull("sort_order")) {
                obj.optString("sort_order").toIntOrNull() ?: obj.optInt("sort_order", 999)
            } else {
                999
            }
            if (isActive == 1 && !imageUrl.isNullOrBlank()) {
                result.add(
                    Banner(
                        id = id,
                        title = title,
                        subtitle = subtitle,
                        imageUrl = imageUrl,
                        link = link,
                        isActive = isActive,
                        sort_order = sortOrder
                    )
                )
            }
        }
        return result
    }

    // Fallback list strictly ordered by the official Snowhite PDF sequence (used ONLY if no cache and no live API yet)
    private fun getDefaultFallbackCategories(): List<Category> = listOf(
        Category(id = 1L, name = "Gent's", isActive = 1, sort_order = 1),
        Category(id = 2L, name = "Ladies", isActive = 1, sort_order = 2),
        Category(id = 3L, name = "Households", isActive = 1, sort_order = 3),
        Category(id = 4L, name = "Washing Powder", isActive = 1, sort_order = 4)
    )

    private fun getDefaultFallbackProducts(): List<Product> = listOf(
        // 1. Gents Laundry
        Product(id = 101L, name = "Qameez", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "190", discountTier = "regular", sort_order = 1),
        Product(id = 102L, name = "Shalwar", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "190", discountTier = "regular", sort_order = 2),
        Product(id = 103L, name = "Shalwar Suit (Cotton/Blended)", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "370", discountTier = "regular", sort_order = 3),
        Product(id = 104L, name = "Shirt", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "260", discountTier = "regular", sort_order = 4),
        Product(id = 105L, name = "Kurti Cotton", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "190", discountTier = "regular", sort_order = 5),
        Product(id = 106L, name = "T-Shirt / Golf Shirt", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "260", discountTier = "regular", sort_order = 6),
        Product(id = 107L, name = "Under Wear", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "75", discountTier = "regular", sort_order = 7),
        Product(id = 108L, name = "Vest", categoryId = 1L, categoryName = "Gent's", serviceType = "Laundry", price = "75", discountTier = "regular", sort_order = 8),
        // 2. Gents Dry Cleaning
        Product(id = 109L, name = "Pants/ Jeans", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "370", discountTier = "regular", sort_order = 9),
        Product(id = 110L, name = "Coat", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "980", discountTier = "regular", sort_order = 10),
        Product(id = 111L, name = "Shirt Silk", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "470", discountTier = "regular", sort_order = 11),
        Product(id = 112L, name = "Suit 2pcs", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "1350", discountTier = "regular", sort_order = 12),
        Product(id = 113L, name = "Suit 3pcs", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "1950", discountTier = "regular", sort_order = 13),
        Product(id = 114L, name = "Qameez (Silk/Bosky/Arcandi/Khadi)", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "450", discountTier = "regular", sort_order = 14),
        Product(id = 115L, name = "Shalwar (Silk/Bosky/Arcandi/Khadi)", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "450", discountTier = "regular", sort_order = 15),
        Product(id = 116L, name = "Shalwar Suit (Silk/Bosky/Arcandi/Khadi)", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "900", discountTier = "regular", sort_order = 16),
        Product(id = 117L, name = "Cardigan", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "750", discountTier = "regular", sort_order = 17),
        Product(id = 118L, name = "Sweater / Pull Over", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "750", discountTier = "regular", sort_order = 18),
        Product(id = 119L, name = "Sweater Sleeveless", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "460", discountTier = "regular", sort_order = 19),
        Product(id = 120L, name = "Pajama (tracks)", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "350", discountTier = "regular", sort_order = 20),
        Product(id = 121L, name = "Safari Suit", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "780", discountTier = "regular", sort_order = 21),
        Product(id = 122L, name = "Sherwani Plain", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "1400", discountTier = "regular", sort_order = 22),
        Product(id = 123L, name = "Vest Coat Sherwani Collar", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "650", discountTier = "regular", sort_order = 23),
        Product(id = 124L, name = "Tie", categoryId = 1L, categoryName = "Gent's", serviceType = "Dry Clean", price = "450", discountTier = "regular", sort_order = 24),
        // 3. Ladies Wear
        Product(id = 201L, name = "Qameez (Plain/Cotton)", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "230", discountTier = "regular", sort_order = 25),
        Product(id = 202L, name = "Shalwar (Plain/Cotton)", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "230", discountTier = "regular", sort_order = 26),
        Product(id = 203L, name = "Dupatta (Plain/Cotton)", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "230", discountTier = "regular", sort_order = 27),
        Product(id = 204L, name = "Qameez (Poly/Silk)", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "390", discountTier = "regular", sort_order = 28),
        Product(id = 205L, name = "Shalwar (Poly / Silk)", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "390", discountTier = "regular", sort_order = 29),
        Product(id = 206L, name = "Dupatta (Poly/ Silk)", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "390", discountTier = "regular", sort_order = 30),
        Product(id = 207L, name = "Jeans / Trousers", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "340", discountTier = "regular", sort_order = 31),
        Product(id = 208L, name = "Shawl", categoryId = 2L, categoryName = "Ladies", serviceType = "Dry Clean", price = "1000", discountTier = "regular", sort_order = 32),
        // 4. Households / Linen
        Product(id = 301L, name = "Blankets", categoryId = 3L, categoryName = "Households", serviceType = "Dry Clean", price = "1200", discountTier = "premium", sort_order = 33),
        Product(id = 302L, name = "Carpet Woolen / Synthetic (Per Sq Ft)", categoryId = 3L, categoryName = "Households", serviceType = "Dry Clean", price = "30", discountTier = "premium", sort_order = 34),
        Product(id = 303L, name = "Carpet Chinese/ Persian (Per Sq.ft)", categoryId = 3L, categoryName = "Households", serviceType = "Dry Clean", price = "140", discountTier = "premium", sort_order = 35),
        Product(id = 304L, name = "Exclusive Carpet (Per Sq. ft)", categoryId = 3L, categoryName = "Households", serviceType = "Dry Clean", price = "140", discountTier = "premium", sort_order = 36),
        Product(id = 305L, name = "Curtain Cleaning Per Panel", categoryId = 3L, categoryName = "Households", serviceType = "Dry Clean", price = "660", discountTier = "premium", sort_order = 37),
        Product(id = 306L, name = "Bed Sheet Double", categoryId = 3L, categoryName = "Households", serviceType = "Laundry", price = "260", discountTier = "regular", sort_order = 38),
        Product(id = 307L, name = "Bed Sheet Single", categoryId = 3L, categoryName = "Households", serviceType = "Laundry", price = "180", discountTier = "regular", sort_order = 39),
        Product(id = 308L, name = "Pillow Covers", categoryId = 3L, categoryName = "Households", serviceType = "Laundry", price = "120", discountTier = "regular", sort_order = 40)
    )

    // ==================== LIVE API CALLS WITH CACHE-BUSTING ====================

    suspend fun getSettings(): Result<GlobalSettings> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${apiEndpoint}?action=get_settings&t=${System.currentTimeMillis()}")
            val apiResponse = parseApiResponse<GlobalSettings>(json)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
                saveOfflineSettings(apiResponse.data)
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${apiEndpoint}?action=get_categories&t=${System.currentTimeMillis()}")
            val rootObj = JSONObject(json)
            if (rootObj.optString("status") == "success") {
                val dataArr = rootObj.optJSONArray("data")
                val parsed = if (dataArr != null) {
                    parseCategoriesJsonArray(dataArr)
                } else {
                    val apiResponse = parseApiResponseList(json, Category::class.java)
                    (apiResponse?.data ?: emptyList()).filter { it.isActive == 1 }
                }
                val sorted = parsed.sortedBy { it.sort_order ?: 999 }
                if (sorted.isNotEmpty()) {
                    saveOfflineCategories(sorted)
                }
                Result.success(sorted)
            } else {
                Result.failure(Exception(rootObj.optString("message", "Failed to fetch categories")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(categoryId: Long? = null): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val ts = System.currentTimeMillis()
            val url = if (categoryId != null && categoryId > 0) {
                "${apiEndpoint}?action=get_products&category_id=$categoryId&t=$ts"
            } else {
                "${apiEndpoint}?action=get_products&t=$ts"
            }
            val json = executeGet(url)
            val rootObj = JSONObject(json)
            if (rootObj.optString("status") == "success") {
                val dataArr = rootObj.optJSONArray("data")
                val parsed = if (dataArr != null) {
                    parseProductsJsonArray(dataArr)
                } else {
                    val apiResponse = parseApiResponseList(json, Product::class.java)
                    (apiResponse?.data ?: emptyList()).filter { it.activeInt == 1 }
                }
                val sorted = parsed.sortedWith(compareBy({ it.sort_order ?: 999 }, { it.id }))
                if (categoryId == null && sorted.isNotEmpty()) {
                    saveOfflineProducts(sorted)
                }
                Result.success(sorted)
            } else {
                Result.failure(Exception(rootObj.optString("message", "Failed to fetch products")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBanners(): Result<List<Banner>> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${apiEndpoint}?action=get_banners&t=${System.currentTimeMillis()}")
            val rootObj = JSONObject(json)
            if (rootObj.optString("status") == "success") {
                val dataArr = rootObj.optJSONArray("data")
                val parsed = if (dataArr != null) {
                    parseBannersJsonArray(dataArr)
                } else {
                    val apiResponse = parseApiResponseList(json, Banner::class.java)
                    (apiResponse?.data ?: emptyList()).filter { it.isActive == 1 }
                }
                val sorted = parsed.sortedBy { it.sort_order ?: 999 }
                saveOfflineBanners(sorted)
                Result.success(sorted)
            } else {
                Result.failure(Exception(rootObj.optString("message", "Failed to fetch banners")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(emailOrPhone: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("email", emailOrPhone)
                put("phone", emailOrPhone)
                put("password", password)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=login", jsonBody)
            val apiResponse = parseApiResponse<User>(responseJson)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Invalid email/phone or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, phone: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("name", name)
                put("phone", phone)
                put("email", email)
                put("password", password)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=register", jsonBody)
            val apiResponse = parseApiResponse<User>(responseJson)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPasswordRequest(identifier: String, channel: String = "email"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanId = identifier.trim()
            val jsonBody = JSONObject().apply {
                put("identifier", cleanId)
                put("email", cleanId)
                put("phone", cleanId)
                put("channel", channel)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=forgot_password_request", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                Result.success(if (message.isNotEmpty()) message else "OTP sent via $channel")
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Failed to send OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPasswordWithOtp(identifier: String, otp: String, newPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanId = identifier.trim()
            val jsonBody = JSONObject().apply {
                put("identifier", cleanId)
                put("email", cleanId)
                put("phone", cleanId)
                put("otp", otp.trim())
                put("new_password", newPassword)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=forgot_password_verify", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                Result.success(if (message.isNotEmpty()) message else "Password reset successful")
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Invalid OTP or reset failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifySwCard(cardNumber: String): Result<SwMember> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("card_number", cardNumber.trim())
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=verify_sw_card", jsonBody)
            val apiResponse = parseApiResponse<SwMember>(responseJson)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                val jsonObj = JSONObject(responseJson)
                if (jsonObj.optString("status") == "success") {
                    val dataObj = jsonObj.optJSONObject("data")
                    if (dataObj != null) {
                        val member = SwMember(
                            cardNumber = dataObj.optString("card_number"),
                            memberName = dataObj.optString("member_name"),
                            phone = dataObj.optString("phone"),
                            branchCode = dataObj.optString("branch_code"),
                            voucher1 = dataObj.optString("voucher_1"),
                            voucher2 = dataObj.optString("voucher_2"),
                            discountPremiumPercent = dataObj.opt("discount_premium_percent")?.toString(),
                            discountRegularPercent = dataObj.opt("discount_regular_percent")?.toString(),
                            expiryDate = dataObj.optString("expiry_date")
                        )
                        Result.success(member)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Member record not found"))
                    }
                } else {
                    val errorMsg = jsonObj.optString("message", apiResponse?.message ?: "Invalid SW Card Number or Phone. Please contact Snowhite support.")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        userId: Long,
        name: String,
        phone: String,
        email: String,
        address: String,
        newPassword: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("user_id", userId)
                put("customer_id", userId)
                put("name", name.trim())
                put("phone", phone.trim())
                put("email", email.trim())
                put("address", address.trim())
                put("service_zone", address.trim())
                put("new_phone", phone.trim())
                put("new_email", email.trim())
                if (!newPassword.isNullOrBlank()) {
                    put("new_password", newPassword)
                }
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=update_profile", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                val apiResponse = parseApiResponse<User>(responseJson)
                val updatedUser = apiResponse?.data ?: User(
                    id = userId,
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    serviceZone = address.trim(),
                    address = address.trim()
                )
                Result.success(updatedUser)
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestProfileUpdateOtp(userId: Long, channel: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("user_id", userId)
                put("channel", channel)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=request_profile_update_otp", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                Result.success(if (message.isNotEmpty()) message else "OTP sent via $channel")
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Failed to request OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyAndUpdateProfile(
        userId: Long,
        otp: String,
        newName: String,
        newPhone: String,
        newEmail: String,
        newAddress: String,
        newPassword: String?
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("user_id", userId)
                put("customer_id", userId)
                put("otp", otp.trim())
                put("new_name", newName.trim())
                put("new_phone", newPhone.trim())
                put("new_email", newEmail.trim())
                put("address", newAddress.trim())
                put("new_address", newAddress.trim())
                if (!newPassword.isNullOrBlank()) {
                    put("new_password", newPassword)
                }
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=verify_and_update_profile", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                val apiResponse = parseApiResponse<User>(responseJson)
                val updatedUser = apiResponse?.data ?: User(
                    id = userId,
                    name = newName.trim(),
                    phone = newPhone.trim(),
                    email = newEmail.trim(),
                    serviceZone = newAddress.trim(),
                    address = newAddress.trim()
                )
                Result.success(updatedUser)
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Invalid OTP or failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(
        customerId: Long,
        pickupAddress: String,
        pickupTimeSlot: String,
        totalAmount: Double,
        items: List<CartItem>
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val itemsArray = JSONArray()
            items.forEach { cartItem ->
                val itemObj = JSONObject().apply {
                    put("name", cartItem.product.name)
                    put("quantity", cartItem.quantity)
                    put("price", cartItem.product.numericPrice)
                    put("service_type", cartItem.product.serviceType ?: "Dry Clean")
                }
                itemsArray.put(itemObj)
            }
            val jsonBody = JSONObject().apply {
                put("customer_id", customerId)
                put("pickup_address", pickupAddress)
                put("pickup_time_slot", pickupTimeSlot)
                put("total_amount", totalAmount)
                put("hub_name", "Karachi")
                put("items", itemsArray)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=create_laundry_order", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            val orderId = rootObj.optLong("order_id", 0L)
            if (status == "success") {
                Result.success(orderId)
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Failed to schedule pickup"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrders(customerId: Long?, phone: String?): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "${apiEndpoint}?action=get_customer_orders".toHttpUrlOrNull()?.newBuilder()
            if (customerId != null && customerId > 0) {
                urlBuilder?.addQueryParameter("customer_id", customerId.toString())
            }
            if (!phone.isNullOrEmpty()) {
                urlBuilder?.addQueryParameter("phone", phone)
            }
            val url = urlBuilder?.build()?.toString() ?: "${apiEndpoint}?action=get_customer_orders"
            val json = executeGet(url)
            val apiResponse = parseApiResponseList(json, Order::class.java)
            if (apiResponse?.status == "success") {
                Result.success(apiResponse.data ?: emptyList())
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatMessages(orderId: Any): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        try {
            val cleanOrderId = orderId.toString().replace("#", "").replace("Order", "").trim().toLongOrNull() ?: 0L
            val url = "${apiEndpoint}?action=get_chat_messages&order_id=$cleanOrderId"
            val json = executeGet(url)
            val apiResponse = parseApiResponseList(json, ChatMessage::class.java)
            if (apiResponse?.status == "success") {
                Result.success(apiResponse.data ?: emptyList())
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to load chat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(orderId: Any, message: String, senderId: Long = 1): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanOrderId = orderId.toString().replace("#", "").replace("Order", "").trim().toLongOrNull() ?: 0L
            val jsonBody = JSONObject().apply {
                put("order_id", cleanOrderId)
                put("sender_type", "customer")
                put("sender_id", if (senderId > 0) senderId else 1)
                put("message", message)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=send_chat_message", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val messageText = rootObj.optString("message")
            if (status == "success" || rootObj.has("id")) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(if (messageText.isNotEmpty()) messageText else "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveOrderRating(orderId: Long, rating: Int, comment: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("order_id", orderId)
                put("rating", rating)
                put("comment", comment)
                put("review", comment)
                put("feedback", comment)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=save_order_rating", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val messageText = rootObj.optString("message")
            if (status == "success" || rootObj.has("id")) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(if (messageText.isNotEmpty()) messageText else "Failed to submit rating"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitRating(orderId: Long, rating: Int, review: String): Result<Unit> {
        return saveOrderRating(orderId, rating, review)
    }

    suspend fun getPromos(): Result<List<PromoCode>> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${apiEndpoint}?action=get_promos")
            val apiResponse = parseApiResponseList(json, PromoCode::class.java)
            if (apiResponse?.status == "success") {
                Result.success(apiResponse.data ?: emptyList())
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to load promos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(userId: Long?, fcmToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("action", "update_fcm_token")
                if (userId != null && userId > 0) put("user_id", userId)
                put("fcm_token", fcmToken)
                put("token", fcmToken)
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=update_fcm_token", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            if (status == "success") {
                Result.success(Unit)
            } else {
                Result.failure(Exception(rootObj.optString("message", "Failed to update FCM token")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitMembershipForm(
        userId: Long,
        name: String,
        phone: String,
        email: String,
        address: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("user_id", userId)
                put("name", name.trim())
                put("phone", phone.trim())
                put("email", email.trim())
                put("address", address.trim())
            }.toString()
            val responseJson = executePost("${apiEndpoint}?action=submit_membership_form", jsonBody)
            val rootObj = JSONObject(responseJson)
            val status = rootObj.optString("status")
            val message = rootObj.optString("message")
            if (status == "success") {
                Result.success(if (message.isNotEmpty()) message else "Application Submitted Successfully")
            } else {
                Result.failure(Exception(if (message.isNotEmpty()) message else "Failed to submit membership application"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
