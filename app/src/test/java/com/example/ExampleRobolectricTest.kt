package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CartItem
import com.example.model.Department
import com.example.model.GarmentAudience
import com.example.model.Product
import com.example.model.RetailCategory
import com.example.model.ServiceCategory
import com.example.model.ServiceType
import com.example.viewmodel.OrderViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies strict Snowhite branding`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Snowhite", appName)
    }

    @Test
    fun `cart totals calculation applies SW Card discount and free delivery for premium care`() {
        val testProduct = Product(
            id = "test_1",
            name = "Two-Piece Suit (Premium Plus)",
            department = Department.GARMENT_CARE,
            audience = GarmentAudience.GENTS,
            serviceType = ServiceType.DRY_CLEANING,
            serviceCategory = ServiceCategory.PREMIUM_PLUS_SERVICE,
            originalPrice = 1200.0
        )
        val cartItems = listOf(CartItem(product = testProduct, quantity = 2))

        // Non-member calculation: Subtotal = 2400, Discount = 0, Delivery = 250, Total = 2650
        val nonMemberTotals = OrderViewModel.calculateCartTotals(cartItems, isMember = false)
        assertEquals(2400.0, nonMemberTotals.subtotal, 0.01)
        assertEquals(0.0, nonMemberTotals.discount, 0.01)
        assertEquals(250.0, nonMemberTotals.deliveryFee, 0.01)
        assertEquals(2650.0, nonMemberTotals.finalTotal, 0.01)

        // Member calculation: 30% discount on 2400 = 720 PKR discount, Free delivery = 0, Total = 1680
        val memberTotals = OrderViewModel.calculateCartTotals(cartItems, isMember = true)
        assertEquals(2400.0, memberTotals.subtotal, 0.01)
        assertEquals(720.0, memberTotals.discount, 0.01)
        assertEquals(0.0, memberTotals.deliveryFee, 0.01)
        assertEquals(1680.0, memberTotals.finalTotal, 0.01)
    }

    @Test
    fun `cart totals calculation applies 20 percent discount for regular service and retail`() {
        val retailProduct = Product(
            id = "retail_test_1",
            name = "Snowhite Fabric Whitener (500ml)",
            department = Department.RETAIL,
            retailCategory = RetailCategory.PRODUCTS,
            serviceCategory = ServiceCategory.WASHING_POWDER_RETAIL,
            originalPrice = 450.0
        )
        val cartItems = listOf(CartItem(product = retailProduct, quantity = 2))

        // Non-member: Subtotal = 900, Discount = 0, Delivery = 250, Total = 1150
        val nonMemberTotals = OrderViewModel.calculateCartTotals(cartItems, isMember = false)
        assertEquals(900.0, nonMemberTotals.subtotal, 0.01)
        assertEquals(0.0, nonMemberTotals.discount, 0.01)
        assertEquals(250.0, nonMemberTotals.deliveryFee, 0.01)
        assertEquals(1150.0, nonMemberTotals.finalTotal, 0.01)

        // Member: 20% discount on 900 = 180 PKR discount, Free delivery = 0, Total = 720
        val memberTotals = OrderViewModel.calculateCartTotals(cartItems, isMember = true)
        assertEquals(900.0, memberTotals.subtotal, 0.01)
        assertEquals(180.0, memberTotals.discount, 0.01)
        assertEquals(0.0, memberTotals.deliveryFee, 0.01)
        assertEquals(720.0, memberTotals.finalTotal, 0.01)
    }

    @Test
    fun `empty cart returns zero totals`() {
        val totals = OrderViewModel.calculateCartTotals(emptyList(), isMember = false)
        assertEquals(0.0, totals.subtotal, 0.01)
        assertEquals(0.0, totals.discount, 0.01)
        assertEquals(0.0, totals.deliveryFee, 0.01)
        assertEquals(0.0, totals.finalTotal, 0.01)
        assertEquals(0, totals.totalItemCount)
    }
}
