package com.example

import com.example.model.CartItem
import com.example.model.Department
import com.example.model.GarmentAudience
import com.example.model.PaymentMethod
import com.example.model.Product
import com.example.model.ServiceCategory
import com.example.model.ServiceType
import com.example.model.formatPkr
import com.example.viewmodel.OrderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var testSuitProduct: Product
    private lateinit var testShirtProduct: Product

    @Before
    fun setUp() {
        testSuitProduct = Product(
            id = "test_suit",
            name = "Two-Piece Suit",
            department = Department.GARMENT_CARE,
            audience = GarmentAudience.GENTS,
            serviceType = ServiceType.DRY_CLEANING,
            serviceCategory = ServiceCategory.REGULAR_SERVICE,
            originalPrice = 850.0
        )
        testShirtProduct = Product(
            id = "test_shirt",
            name = "Everyday Casual Wash",
            department = Department.GARMENT_CARE,
            audience = GarmentAudience.GENTS,
            serviceType = ServiceType.LAUNDRY,
            serviceCategory = ServiceCategory.REGULAR_SERVICE,
            originalPrice = 250.0
        )
    }

    @Test
    fun pkrFormatting_isCorrect() {
        assertEquals("Rs. 850 PKR", formatPkr(850.0))
        assertEquals("Rs. 1,200 PKR", formatPkr(1200.0))
        assertEquals("Rs. 0 PKR", formatPkr(0.0))
    }

    @Test
    fun memberDiscountedPrice_inPkr_isCalculatedCorrectly() {
        // Suit: 850 PKR with 20% discount = 680 PKR
        assertEquals(680.0, testSuitProduct.memberPrice, 0.01)
        assertEquals("Rs. 680 PKR", testSuitProduct.formattedMemberPrice)

        // Shirt: 250 PKR with 20% discount = 200 PKR
        assertEquals(200.0, testShirtProduct.memberPrice, 0.01)
        assertEquals("Rs. 200 PKR", testShirtProduct.formattedMemberPrice)
    }

    @Test
    fun cartTotals_forNonMemberUnderThreshold_includesStandardDelivery() {
        val items = listOf(
            CartItem(product = testSuitProduct, quantity = 1), // 850 PKR
            CartItem(product = testShirtProduct, quantity = 2)  // 500 PKR
        )
        // Subtotal = 1350 PKR, Non-member (no discount), Under 2500 PKR -> Delivery = 250 PKR
        val totals = OrderViewModel.calculateCartTotals(
            items = items,
            isMember = false
        )

        assertEquals(1350.0, totals.subtotal, 0.01)
        assertEquals(0.0, totals.discount, 0.01)
        assertEquals(250.0, totals.deliveryFee, 0.01)
        assertEquals(1600.0, totals.finalTotal, 0.01)
        assertEquals(3, totals.totalItemCount)
    }

    @Test
    fun cartTotals_forNonMemberOverThreshold_hasFreeDelivery() {
        val items = listOf(
            CartItem(product = testSuitProduct, quantity = 3) // 3 * 850 = 2550 PKR (>= 2500 threshold)
        )
        val totals = OrderViewModel.calculateCartTotals(
            items = items,
            isMember = false
        )

        assertEquals(2550.0, totals.subtotal, 0.01)
        assertEquals(0.0, totals.discount, 0.01)
        assertEquals(0.0, totals.deliveryFee, 0.01)
        assertEquals(2550.0, totals.finalTotal, 0.01)
    }

    @Test
    fun cartTotals_forSWMember_appliesTwentyPercentDiscountAndFreeDelivery() {
        val items = listOf(
            CartItem(product = testSuitProduct, quantity = 2), // 1700 PKR
            CartItem(product = testShirtProduct, quantity = 2)  // 500 PKR
        )
        // Subtotal = 2200 PKR
        // Member discount = 20% of 2200 = 440 PKR
        // SW Member delivery fee = 0 PKR (perk)
        // Final Total = 1760 PKR
        val totals = OrderViewModel.calculateCartTotals(
            items = items,
            isMember = true
        )

        assertEquals(2200.0, totals.subtotal, 0.01)
        assertEquals(440.0, totals.discount, 0.01)
        assertEquals(0.0, totals.deliveryFee, 0.01)
        assertEquals(1760.0, totals.finalTotal, 0.01)
        assertEquals(4, totals.totalItemCount)
    }

    @Test
    fun validateMembershipCode_validatesCorrectPrefix() {
        val viewModel = OrderViewModel()

        assertTrue(viewModel.validateMembershipCode("B 2152"))
        assertTrue(viewModel.membership.value.isMember)
        assertEquals("B 2152", viewModel.membership.value.memberCode)

        assertTrue(viewModel.validateMembershipCode("SW-GOLD"))
        assertTrue(viewModel.validateMembershipCode("B 1092"))

        assertFalse(viewModel.validateMembershipCode("   "))
    }

    @Test
    fun registerNewMember_generatesCardAndSetsMemberState() {
        val viewModel = OrderViewModel()

        val membership = viewModel.registerNewMember(
            name = "Ali Raza",
            phone = "+92 300 1234567",
            email = "ali@example.com",
            address = "Sector F-7/2, Islamabad"
        )

        assertTrue(membership.isMember)
        assertTrue(membership.memberCode.startsWith("B "))
        assertEquals(20, membership.discountPercent)
        assertTrue(membership.freeDelivery)
        assertEquals("Ali Raza", viewModel.checkoutName.value)
    }

    @Test
    fun cartOperations_addUpdateRemove_workCorrectly() {
        val viewModel = OrderViewModel()

        viewModel.addToCart(testSuitProduct)
        assertEquals(1, viewModel.cart.value[testSuitProduct.id]?.quantity)

        viewModel.addToCart(testSuitProduct)
        assertEquals(2, viewModel.cart.value[testSuitProduct.id]?.quantity)

        viewModel.removeFromCart(testSuitProduct.id)
        assertEquals(1, viewModel.cart.value[testSuitProduct.id]?.quantity)

        viewModel.removeFromCart(testSuitProduct.id)
        assertFalse(viewModel.cart.value.containsKey(testSuitProduct.id))
    }

    @Test
    fun placeOrder_createsValidOrderAndClearsCart() {
        val viewModel = OrderViewModel()
        viewModel.addToCart(testSuitProduct)
        viewModel.updateCheckoutName("Kamran Khan")
        viewModel.updateCheckoutPhone("+92 300 9990000")
        viewModel.updateCheckoutAddress("Gulberg III, Lahore")
        viewModel.updatePaymentMethod(PaymentMethod.CASH_ON_DELIVERY)

        val placedOrder = viewModel.placeOrder()

        assertNotNull(placedOrder)
        assertTrue(placedOrder.id.startsWith("SW-"))
        assertEquals("Kamran Khan", placedOrder.customerName)
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, placedOrder.paymentMethod)
        assertEquals(1, placedOrder.items.size)

        assertTrue(viewModel.cart.value.isEmpty())
        assertEquals(placedOrder.id, viewModel.orders.value.first().id)
    }

    @Test
    fun retrofitClient_baseUrl_pointsToSnowRemoteServer() {
        assertEquals("https://snow.akfasft.com/api/", com.example.network.RetrofitClient.BASE_URL)
        assertNotNull(com.example.network.RetrofitClient.apiService)
    }

    @Test
    fun apiModels_dataClasses_initializeCorrectly() {
        val authPayload = com.example.network.LiveAuthPayload(
            phone = "+92 300 1234567",
            name = "Tariq Mahmood",
            swCardNumber = "SW-9988"
        )
        assertEquals("+92 300 1234567", authPayload.phone)
        assertEquals("SW-9988", authPayload.swCardNumber)
    }
}
