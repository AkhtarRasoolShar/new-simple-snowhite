package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.SnowhiteRepository
import com.example.network.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SnowhiteViewModel : ViewModel() {

    private val repository = SnowhiteRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _settings = MutableStateFlow(GlobalSettings())
    val settings: StateFlow<GlobalSettings> = _settings.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _swCardNumber = MutableStateFlow<String?>(null)
    val swCardNumber: StateFlow<String?> = _swCardNumber.asStateFlow()

    private val _isSwMember = MutableStateFlow(false)
    val isSwMember: StateFlow<Boolean> = _isSwMember.asStateFlow()

    private val _swMember = MutableStateFlow<SwMember?>(null)
    val swMember: StateFlow<SwMember?> = _swMember.asStateFlow()

    private val _selectedProductForModal = MutableStateFlow<Product?>(null)
    val selectedProductForModal: StateFlow<Product?> = _selectedProductForModal.asStateFlow()

    private val _notificationsList = MutableStateFlow<List<AppNotification>>(emptyList())
    val notificationsList: StateFlow<List<AppNotification>> = _notificationsList.asStateFlow()

    private val _unreadChatCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val unreadChatCounts: StateFlow<Map<Long, Int>> = _unreadChatCounts.asStateFlow()

    private val _activeChatOrderId = MutableStateFlow<Long?>(null)
    val activeChatOrderId: StateFlow<Long?> = _activeChatOrderId.asStateFlow()

    private val _freeOrdersBalance = MutableStateFlow(0)
    val freeOrdersBalance: StateFlow<Int> = _freeOrdersBalance.asStateFlow()

    private val lastKnownOrderStatuses = mutableMapOf<Long, String>()
    private val lastSeenChatMessageId = mutableMapOf<Long, Long>()
    private var isWatcherStarted = false
    private var isInitialOrderLoad = true

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSettings().onSuccess { _settings.value = it }
            repository.getCategories().onSuccess { _categories.value = it }
            repository.getProducts().onSuccess { _products.value = it }
            _isLoading.value = false
        }
    }

    fun setSwMemberStatus(isMember: Boolean) {
        _isSwMember.value = isMember
        if (!isMember) {
            _swCardNumber.value = null
            _swMember.value = null
        }
    }

    fun verifySwCardLive(cardNumberInput: String, onResult: (Boolean, SwMember?, String?) -> Unit) {
        val trimmed = cardNumberInput.trim()
        if (trimmed.isEmpty()) {
            onResult(false, null, "Please enter SW Card Number or Registered Phone")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.verifySwCard(trimmed).fold(
                onSuccess = { member ->
                    _isLoading.value = false
                    _swMember.value = member
                    val cardNo = member.cardNumber ?: trimmed
                    _swCardNumber.value = cardNo
                    _isSwMember.value = true

                    val current = _currentUser.value
                    val newName = if (!current?.name.isNullOrBlank()) current?.name else member.memberName
                    val newPhone = if (!current?.phone.isNullOrBlank()) current?.phone else member.phone
                    if (current == null) {
                        _currentUser.value = User(
                            id = System.currentTimeMillis(),
                            name = member.memberName,
                            phone = member.phone,
                            email = "",
                            isSwMember = true,
                            swCardNumber = cardNo
                        )
                    } else {
                        _currentUser.value = current.copy(
                            name = newName,
                            phone = newPhone,
                            swCardNumber = cardNo,
                            isSwMember = true
                        )
                    }

                    val welcomeName = member.memberName?.ifBlank { "Member" } ?: "Member"
                    onResult(true, member, "SW Gold Membership Verified! Welcome, $welcomeName")
                },
                onFailure = { error ->
                    _isLoading.value = false
                    val msg = error.message.takeIf { !it.isNullOrBlank() }
                        ?: "Invalid SW Card Number or Phone. Please contact Snowhite support."
                    onResult(false, null, msg)
                }
            )
        }
    }

    fun verifySwCard(cardNumber: String): Boolean {
        val trimmed = cardNumber.trim()
        if (trimmed.length >= 2) {
            verifySwCardLive(trimmed) { _, _, _ -> }
            return true
        }
        return false
    }

    fun selectProductForModal(product: Product?) {
        _selectedProductForModal.value = product
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + quantity
            if (newQty > 0) {
                currentList[index] = existing.copy(quantity = newQty)
            } else {
                currentList.removeAt(index)
            }
        } else if (quantity > 0) {
            currentList.add(CartItem(product, quantity))
        }
        _cartItems.value = currentList
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + delta
            if (newQty > 0) {
                currentList[index] = existing.copy(quantity = newQty)
            } else {
                currentList.removeAt(index)
            }
            _cartItems.value = currentList
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartQuantity(productId: Long): Int {
        return _cartItems.value.find { it.product.id == productId }?.quantity ?: 0
    }

    fun getDiscountedPrice(product: Product, isSwMember: Boolean): Double {
        val basePrice = product.numericPrice
        if (product.numericManualDiscount > 0) {
            val manualPct = product.numericManualDiscount / 100.0
            return basePrice * (1.0 - manualPct)
        }

        if (!isSwMember) return basePrice

        val settings = _settings.value
        val verifiedMember = _swMember.value

        val premiumPctRaw = verifiedMember?.premiumPct
            ?: settings.numericSwPremiumDiscount
            ?: 30.0
        val regularPctRaw = verifiedMember?.regularPct
            ?: settings.numericSwRegularDiscount
            ?: 20.0

        val premiumPct = if (premiumPctRaw > 1.0) premiumPctRaw / 100.0 else premiumPctRaw
        val regularPct = if (regularPctRaw > 1.0) regularPctRaw / 100.0 else regularPctRaw

        val tier = product.discountTier?.lowercase() ?: "auto"
        val isPremiumTier = when (tier) {
            "premium" -> true
            "regular" -> false
            else -> {
                val catName = product.categoryName ?: ""
                product.name.contains("Bridal", ignoreCase = true) ||
                        product.name.contains("Heavy", ignoreCase = true) ||
                        product.name.contains("Suit", ignoreCase = true) ||
                        product.name.contains("Formal", ignoreCase = true) ||
                        product.name.contains("Panel", ignoreCase = true) ||
                        product.name.contains("Comforter", ignoreCase = true) ||
                        product.name.contains("Premium", ignoreCase = true) ||
                        catName.contains("Premium", ignoreCase = true) ||
                        catName.contains("Household", ignoreCase = true) ||
                        product.name.contains("Curtain", ignoreCase = true) ||
                        product.name.contains("Blanket", ignoreCase = true) ||
                        product.name.contains("Carpet", ignoreCase = true) ||
                        product.name.contains("Sofa", ignoreCase = true)
            }
        }

        val discountPercent = if (isPremiumTier) premiumPct else regularPct
        return basePrice * (1.0 - discountPercent)
    }

    fun calculateItemPrice(product: Product): Double {
        return getDiscountedPrice(product, _isSwMember.value)
    }

    fun calculateSubtotal(): Double {
        return _cartItems.value.sumOf { cartItem ->
            calculateItemPrice(cartItem.product) * cartItem.quantity
        }
    }

    fun calculateDeliveryFee(): Double {
        return _settings.value.deliveryFee?.toDoubleOrNull() ?: 150.0
    }

    fun calculateOriginalGrandTotal(): Double {
        val subtotal = calculateSubtotal()
        if (subtotal <= 0) return 0.0
        return subtotal + calculateDeliveryFee()
    }

    fun calculateGrandTotal(): Double {
        if (_freeOrdersBalance.value > 0) return 0.0
        return calculateOriginalGrandTotal()
    }

    fun login(emailOrPhone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(emailOrPhone, password)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _freeOrdersBalance.value = user.freeOrdersBalance ?: 0
                    if (user.isSwMember == true || !user.swCardNumber.isNullOrEmpty()) {
                        _isSwMember.value = true
                        _swCardNumber.value = user.swCardNumber
                    }
                    loadOrders()
                    onResult(true, null)
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "Login failed")
                }
            )
        }
    }

    fun register(name: String, phone: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(name, phone, email, password)
            _isLoading.value = false
            result.fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    _freeOrdersBalance.value = user.freeOrdersBalance ?: 0
                    loadOrders()
                    onResult(true, null)
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "Registration failed")
                }
            )
        }
    }

    fun loadOrders() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.getOrders(user.id, user.phone ?: user.email)
            res.onSuccess { list ->
                _orders.value = list
            }
        }
    }

    fun createOrder(
        address: String,
        timeSlotPkt: String,
        totalPayableOverride: Double? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "Please log in to place an order")
            return
        }
        val items = _cartItems.value
        if (items.isEmpty()) {
            onResult(false, "Cart is empty")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val hasFreeOrder = _freeOrdersBalance.value > 0
            val total = if (hasFreeOrder) 0.0 else (totalPayableOverride ?: calculateGrandTotal())
            val result = repository.createOrder(
                customerId = user.id,
                pickupAddress = address,
                pickupTimeSlot = timeSlotPkt,
                totalAmount = maxOf(0.0, total),
                items = items
            )
            _isLoading.value = false
            result.fold(
                onSuccess = { orderId ->
                    if (hasFreeOrder) {
                        val newBal = maxOf(0, _freeOrdersBalance.value - 1)
                        _freeOrdersBalance.value = newBal
                        _currentUser.value = _currentUser.value?.copy(freeOrdersBalance = newBal)
                    }
                    clearCart()
                    loadOrders()
                    onResult(true, "Order #$orderId created successfully!")
                },
                onFailure = { error ->
                    onResult(false, error.message ?: "Order creation failed")
                }
            )
        }
    }

    fun requestProfileUpdateOtp(channel: String, onResult: (Boolean, String?) -> Unit) {
        val user = _currentUser.value ?: run {
            onResult(false, "User not logged in")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.requestProfileUpdateOtp(user.id, channel)
            _isLoading.value = false
            result.fold(
                onSuccess = { msg -> onResult(true, msg) },
                onFailure = { err -> onResult(false, err.message ?: "Failed to send OTP") }
            )
        }
    }

    fun verifyAndUpdateProfile(
        otp: String,
        newName: String,
        newPhone: String,
        newEmail: String,
        newAddress: String,
        newPassword: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onResult(false, "User not logged in")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.verifyAndUpdateProfile(
                userId = user.id,
                otp = otp,
                newName = newName,
                newPhone = newPhone,
                newEmail = newEmail,
                newAddress = newAddress,
                newPassword = newPassword
            )
            _isLoading.value = false
            result.fold(
                onSuccess = { updated ->
                    _currentUser.value = updated.copy(
                        swCardNumber = updated.swCardNumber ?: user.swCardNumber,
                        isSwMember = updated.isSwMember ?: user.isSwMember,
                        freeOrdersBalance = updated.freeOrdersBalance ?: _freeOrdersBalance.value
                    )
                    _freeOrdersBalance.value = _currentUser.value?.freeOrdersBalance ?: 0
                    onResult(true, "Profile updated successfully!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to verify and update profile")
                }
            )
        }
    }

    fun updateProfile(
        name: String,
        phone: String,
        email: String,
        address: String,
        newPassword: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value ?: run {
            onResult(false, "User not logged in")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateProfile(user.id, name, phone, email, address, newPassword)
            _isLoading.value = false
            result.fold(
                onSuccess = { updated ->
                    _currentUser.value = updated.copy(
                        swCardNumber = updated.swCardNumber ?: user.swCardNumber,
                        isSwMember = updated.isSwMember ?: user.isSwMember,
                        freeOrdersBalance = updated.freeOrdersBalance ?: _freeOrdersBalance.value
                    )
                    _freeOrdersBalance.value = _currentUser.value?.freeOrdersBalance ?: 0
                    onResult(true, "Profile updated successfully!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to update profile")
                }
            )
        }
    }

    fun logout() {
        _currentUser.value = null
        _isSwMember.value = false
        _swCardNumber.value = null
        _freeOrdersBalance.value = 0
        _orders.value = emptyList()
    }

    fun startBackgroundWatcher(context: android.content.Context) {
        if (isWatcherStarted) return
        isWatcherStarted = true

        viewModelScope.launch {
            while (isActive) {
                val user = _currentUser.value
                if (user != null) {
                    try {
                        val ordersResult = repository.getOrders(user.id, user.phone ?: user.email)
                        ordersResult.onSuccess { freshOrders ->
                            _orders.value = freshOrders

                            for (order in freshOrders) {
                                val orderId = order.displayId
                                val newStatus = order.effectiveStatus

                                if (lastKnownOrderStatuses.containsKey(orderId)) {
                                    val oldStatus = lastKnownOrderStatuses[orderId]
                                    if (oldStatus != null && !oldStatus.equals(newStatus, ignoreCase = true)) {
                                        val formattedStatus = newStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                                        val title = "Order #$orderId Status Updated!"
                                        val body = "Your order is now: $formattedStatus"

                                        com.example.util.SnowhiteNotificationHelper.showNotification(
                                            context = context,
                                            title = title,
                                            message = body,
                                            notificationId = orderId.toInt()
                                        )

                                        addNotification(
                                            title = title,
                                            body = body,
                                            orderId = orderId,
                                            type = "order"
                                        )
                                    }
                                } else {
                                    if (!isInitialOrderLoad) {
                                        val formattedStatus = newStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                                        val title = "New Order #$orderId Confirmed!"
                                        val body = "Your order status: $formattedStatus"

                                        com.example.util.SnowhiteNotificationHelper.showNotification(
                                            context = context,
                                            title = title,
                                            message = body,
                                            notificationId = orderId.toInt()
                                        )

                                        addNotification(
                                            title = title,
                                            body = body,
                                            orderId = orderId,
                                            type = "order"
                                        )
                                    }
                                }
                                lastKnownOrderStatuses[orderId] = newStatus

                                // Poll rider chat for this active order
                                val chatResult = repository.getChatMessages(orderId)
                                chatResult.onSuccess { messages ->
                                    val lastSeenId = lastSeenChatMessageId[orderId] ?: 0L
                                    val riderMessages = messages.filter {
                                        it.senderType.equals("rider", ignoreCase = true) || it.senderType.equals("admin", ignoreCase = true) || it.senderType.equals("driver", ignoreCase = true)
                                    }

                                    var newRiderCount = 0
                                    for (msg in riderMessages) {
                                        val msgId = msg.id ?: 0L
                                        if (msgId > lastSeenId) {
                                            if (lastSeenId > 0L) {
                                                val title = "New Message from Rider (Order #$orderId)"
                                                val body = msg.message

                                                com.example.util.SnowhiteNotificationHelper.showNotification(
                                                    context = context,
                                                    title = title,
                                                    message = body,
                                                    notificationId = (orderId * 1000 + (msgId % 1000)).toInt()
                                                )

                                                addNotification(
                                                    title = title,
                                                    body = body,
                                                    orderId = orderId,
                                                    type = "chat"
                                                )
                                            }
                                            newRiderCount++
                                        }
                                    }

                                    if (riderMessages.isNotEmpty()) {
                                        val maxMsgId = riderMessages.maxOfOrNull { it.id ?: 0L } ?: 0L
                                        if (maxMsgId > lastSeenId) {
                                            lastSeenChatMessageId[orderId] = maxMsgId
                                        }
                                    }

                                    if (newRiderCount > 0) {
                                        val currentUnread = _unreadChatCounts.value.toMutableMap()
                                        val prevUnread = currentUnread[orderId] ?: 0
                                        currentUnread[orderId] = prevUnread + newRiderCount
                                        _unreadChatCounts.value = currentUnread
                                    }
                                }
                            }
                            if (isInitialOrderLoad) {
                                isInitialOrderLoad = false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                kotlinx.coroutines.delay(6000)
            }
        }
    }

    fun addNotification(title: String, body: String, orderId: Long? = null, type: String = "order") {
        val newNotification = AppNotification(
            title = title,
            body = body,
            orderId = orderId,
            type = type,
            isRead = false
        )
        _notificationsList.value = listOf(newNotification) + _notificationsList.value
    }

    fun markAllNotificationsAsRead() {
        _notificationsList.value = _notificationsList.value.map { it.copy(isRead = true) }
    }

    fun markNotificationAsRead(id: String) {
        _notificationsList.value = _notificationsList.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun clearUnreadChatForOrder(orderId: Long) {
        val current = _unreadChatCounts.value.toMutableMap()
        current.remove(orderId)
        _unreadChatCounts.value = current
    }

    fun openChatForOrder(orderId: Long) {
        _activeChatOrderId.value = orderId
    }

    fun closeChatForOrder() {
        _activeChatOrderId.value = null
    }

    fun saveOrderRating(orderId: Long, rating: Int, comment: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.saveOrderRating(orderId, rating, comment)
            _isLoading.value = false
            result.fold(
                onSuccess = {
                    onResult(true, "Thank you for rating your laundry service!")
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Rating saved")
                }
            )
        }
    }

    fun testNotificationSound(context: android.content.Context) {
        val testTitle = "Snowhite Alert Test"
        val testBody = "Sound tone, heads-up banner, and vibration are active!"
        com.example.util.SnowhiteNotificationHelper.showNotification(
            context = context,
            title = testTitle,
            message = testBody
        )
        addNotification(
            title = testTitle,
            body = testBody,
            type = "test"
        )
    }

    fun submitMembershipForm(
        name: String,
        phone: String,
        email: String,
        address: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = _currentUser.value
        val userId = user?.id ?: 0L
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.submitMembershipForm(
                userId = userId,
                name = name,
                phone = phone,
                email = email,
                address = address
            )
            _isLoading.value = false
            result.fold(
                onSuccess = { msg ->
                    onResult(true, msg)
                },
                onFailure = { err ->
                    onResult(false, err.message ?: "Failed to submit membership application")
                }
            )
        }
    }
}
