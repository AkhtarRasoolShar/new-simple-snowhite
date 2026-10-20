package com.example.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "order_id") val orderId: Long? = null
)

@JsonClass(generateAdapter = true)
data class GlobalSettings(
    @Json(name = "app_name") val appName: String? = "Snowhite",
    @Json(name = "currency") val currency: String? = "PKR",
    @Json(name = "delivery_fee") val deliveryFee: String? = "150",
    @Json(name = "whatsapp_number") val whatsappNumber: String? = "03111110500",
    @Json(name = "support_email") val supportEmail: String? = "cs@snowhite.com.pk",
    @Json(name = "logo_url") val logoUrl: String? = null,
    @Json(name = "sw_premium_discount") val swPremiumDiscount: Any? = "30",
    @Json(name = "sw_regular_discount") val swRegularDiscount: Any? = "20",
    @Json(name = "default_free_orders") val defaultFreeOrders: Any? = "0",
    @Json(name = "free_order_cycle") val freeOrderCycle: Any? = null
) {
    val numericSwPremiumDiscount: Double?
        get() = when (swPremiumDiscount) {
            is Number -> swPremiumDiscount.toDouble()
            is String -> swPremiumDiscount.toDoubleOrNull()
            else -> null
        }

    val numericSwRegularDiscount: Double?
        get() = when (swRegularDiscount) {
            is Number -> swRegularDiscount.toDouble()
            is String -> swRegularDiscount.toDoubleOrNull()
            else -> null
        }
}

@JsonClass(generateAdapter = true)
data class Category(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "is_active") val isActive: Int? = 1,
    @Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "category_id") val categoryId: Long? = null,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "service_type") val serviceType: String? = "Dry Clean",
    @Json(name = "price") val price: String? = "0.00",
    @Json(name = "description") val description: String? = "",
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "is_active") val isActive: Any? = 1,
    @Json(name = "discount_tier") val discountTier: String? = "auto",
    @Json(name = "manual_discount") val manualDiscount: Any? = 0
) {
    val numericPrice: Double
        get() = price?.toDoubleOrNull() ?: 0.0

    val numericManualDiscount: Int
        get() = when (manualDiscount) {
            is Number -> manualDiscount.toInt()
            is String -> manualDiscount.toIntOrNull() ?: 0
            else -> 0
        }

    val activeInt: Int
        get() = when (isActive) {
            is Number -> isActive.toInt()
            is String -> isActive.toIntOrNull() ?: 1
            else -> 1
        }
}

@JsonClass(generateAdapter = true)
data class SwMember(
    @Json(name = "card_number") val cardNumber: String? = null,
    @Json(name = "member_name") val memberName: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "branch_code") val branchCode: String? = null,
    @Json(name = "voucher_1") val voucher1: String? = null,
    @Json(name = "voucher_2") val voucher2: String? = null,
    @Json(name = "discount_premium_percent") val discountPremiumPercent: Any? = null,
    @Json(name = "discount_regular_percent") val discountRegularPercent: Any? = null,
    @Json(name = "expiry_date") val expiryDate: String? = null
) {
    val premiumPct: Double?
        get() = when (discountPremiumPercent) {
            is Number -> discountPremiumPercent.toDouble()
            is String -> discountPremiumPercent.toDoubleOrNull()
            else -> null
        }

    val regularPct: Double?
        get() = when (discountRegularPercent) {
            is Number -> discountRegularPercent.toDouble()
            is String -> discountRegularPercent.toDoubleOrNull()
            else -> null
        }
}

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "service_zone") val serviceZone: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "sw_card_number") val swCardNumber: String? = null,
    @Json(name = "is_sw_member") val isSwMember: Boolean? = false,
    @Json(name = "free_orders_balance") val freeOrdersBalance: Int? = 0
)

@JsonClass(generateAdapter = true)
data class OrderItem(
    @Json(name = "item") val item: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "price") val price: Any? = 0,
    @Json(name = "qty") val qty: Int? = 1,
    @Json(name = "quantity") val quantity: Int? = 1,
    @Json(name = "service_type") val serviceType: String? = "Dry Clean"
) {
    val itemName: String get() = item ?: name ?: "Item"
    val itemQty: Int get() = qty ?: quantity ?: 1
    val numericPrice: Double
        get() = when (price) {
            is Number -> price.toDouble()
            is String -> price.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
}

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "order_id") val orderId: Long? = null,
    @Json(name = "status") val status: String? = "COLLECTING",
    @Json(name = "total_amount") val totalAmount: Any? = "0",
    @Json(name = "initial_offer_price") val initialOfferPrice: Any? = "0",
    @Json(name = "date") val date: String? = null,
    @Json(name = "pickup_address") val pickupAddress: String? = null,
    @Json(name = "pickup_time_slot") val pickupTimeSlot: String? = null,
    @Json(name = "laundry_status") val laundryStatus: String? = null,
    @Json(name = "rider_name") val riderName: String? = "Rider Assigned",
    @Json(name = "rider_phone") val riderPhone: String? = "03000000000",
    @Json(name = "items") val items: List<OrderItem>? = emptyList()
) {
    val displayId: Long get() = id ?: orderId ?: 0L
    val effectiveStatus: String get() = laundryStatus ?: status ?: "COLLECTING"
    val numericTotal: Double
        get() = when (val amt = totalAmount ?: initialOfferPrice) {
            is Number -> amt.toDouble()
            is String -> amt.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
}

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "order_id") val orderId: Long,
    @Json(name = "sender_type") val senderType: String,
    @Json(name = "message") val message: String,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class PromoCode(
    @Json(name = "id") val id: Long,
    @Json(name = "code") val code: String,
    @Json(name = "discount_percent") val discountPercent: Double,
    @Json(name = "description") val description: String? = null
)

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val timestamp: String = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
    val orderId: Long? = null,
    val isRead: Boolean = false,
    val type: String = "order" // "order", "chat", or "test"
)
