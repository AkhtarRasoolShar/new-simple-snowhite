package com.example.network

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
import java.io.IOException
import java.util.concurrent.TimeUnit

class SnowhiteRepository {
    private val apiEndpoint: String get() = AppConfig.apiEndpoint
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

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

    suspend fun getSettings(): Result<GlobalSettings> = withContext(Dispatchers.IO) {
        try {
            val json = executeGet("${apiEndpoint}?action=get_settings")
            val apiResponse = parseApiResponse<GlobalSettings>(json)
            if (apiResponse?.status == "success" && apiResponse.data != null) {
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
            val json = executeGet("${apiEndpoint}?action=get_categories")
            val apiResponse = parseApiResponseList(json, Category::class.java)
            if (apiResponse?.status == "success") {
                val list = apiResponse.data ?: emptyList()
                val activeList = list.filter { it.isActive == 1 }
                Result.success(activeList)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(categoryId: Long? = null): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val url = if (categoryId != null && categoryId > 0) {
                "${apiEndpoint}?action=get_products&category_id=$categoryId"
            } else {
                "${apiEndpoint}?action=get_products"
            }
            val json = executeGet(url)
            val apiResponse = parseApiResponseList(json, Product::class.java)
            if (apiResponse?.status == "success") {
                val list = apiResponse.data ?: emptyList()
                val activeList = list.filter { it.activeInt == 1 }
                Result.success(activeList)
            } else {
                Result.failure(Exception(apiResponse?.message ?: "Failed to fetch products"))
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
