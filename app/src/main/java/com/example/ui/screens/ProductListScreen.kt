package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.network.models.Product
import com.example.ui.components.SnowhiteNetworkImage
import com.example.viewmodel.SnowhiteViewModel

@Composable
fun ProductListScreen(
    departmentType: String,
    viewModel: SnowhiteViewModel,
    onNavigateToCart: () -> Unit
) {
    val allProducts by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedModalProduct by viewModel.selectedProductForModal.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val departmentFiltered = remember(allProducts, departmentType) {
        when (departmentType) {
            "laundry" -> allProducts.filter { p ->
                val cat = p.categoryName?.lowercase() ?: ""
                !cat.contains("powder") && !cat.contains("retail")
            }
            "retail" -> allProducts.filter { p ->
                val cat = p.categoryName?.lowercase() ?: ""
                cat.contains("powder") || cat.contains("retail")
            }
            else -> allProducts
        }
    }

    val sortedCategories = remember(categories) {
        categories.sortedBy { getCategorySequenceScore(it.name) }
    }

    val finalProducts = remember(departmentFiltered, selectedCategoryId, searchQuery) {
        var filtered = departmentFiltered
        if (selectedCategoryId != null) {
            filtered = filtered.filter { it.categoryId == selectedCategoryId }
        }
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim().lowercase()
            filtered = filtered.filter { p ->
                p.name.lowercase().contains(query) ||
                (p.categoryName?.lowercase()?.contains(query) == true) ||
                (p.description?.lowercase()?.contains(query) == true) ||
                (p.serviceType?.lowercase()?.contains(query) == true)
            }
        }
        filtered.sortedBy { getProductSequenceScore(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Search Bar Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search products (e.g. Towel, Suit, Powder)...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF0061E0)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0061E0),
                unfocusedBorderColor = Color(0xFFCBD5E1)
            )
        )

        if (sortedCategories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0061E0),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(sortedCategories) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0061E0),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (finalProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No products found matching \"$searchQuery\"." else "No products found in this category.",
                        fontSize = 15.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { searchQuery = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear Search", color = Color.White)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(finalProducts) { product ->
                    ProductRowCard(
                        product = product,
                        viewModel = viewModel,
                        onProductClick = { viewModel.selectProductForModal(product) }
                    )
                }
            }
        }
    }

    selectedModalProduct?.let { product ->
        ProductDetailModal(
            product = product,
            viewModel = viewModel,
            onDismiss = { viewModel.selectProductForModal(null) }
        )
    }
}

@Composable
fun ProductRowCard(
    product: Product,
    viewModel: SnowhiteViewModel,
    onProductClick: () -> Unit
) {
    val qty = viewModel.getCartQuantity(product.id)
    val effectivePrice = viewModel.calculateItemPrice(product)
    val isSwMember by viewModel.isSwMember.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SnowhiteNetworkImage(
                    url = product.imageUrl,
                    productName = product.name,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.categoryName ?: (product.serviceType ?: "Dry Clean"),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val swDiscountedPrice = viewModel.getDiscountedPrice(product, isSwMember = true)
                    if (product.numericManualDiscount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rs. ${product.price}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rs. ${effectivePrice.toInt()} PKR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    } else if (isSwMember && effectivePrice < product.numericPrice) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rs. ${product.price}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rs. ${effectivePrice.toInt()} PKR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Rs. ${product.price} PKR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0061E0)
                            )
                            if (!isSwMember && swDiscountedPrice < product.numericPrice) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "★ SW Member Price: Rs. ${swDiscountedPrice.toInt()} PKR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (qty == 0) {
                    Button(
                        onClick = { viewModel.addToCart(product, 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ ADD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.updateCartQuantity(product.id, -1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = Color(0xFF0061E0)
                            )
                        }
                        Text(
                            text = "$qty",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { viewModel.updateCartQuantity(product.id, 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = Color(0xFF0061E0)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color(0xFF0061E0))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun ProductDetailModal(
    product: Product,
    viewModel: SnowhiteViewModel,
    onDismiss: () -> Unit
) {
    var modalQty by remember { mutableIntStateOf(1) }
    val effectivePrice = viewModel.calculateItemPrice(product)
    val totalModalPrice = effectivePrice * modalQty

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    SnowhiteNetworkImage(
                        url = product.imageUrl,
                        productName = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = Color(0xFFF0F7FF),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
                    ) {
                        Text(
                            text = "${product.categoryName ?: "Garment Care"} • ${product.serviceType ?: "Dry Clean"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061E0),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = product.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val isSwMemberModal by viewModel.isSwMember.collectAsState()
                    val swMemberModalPrice = viewModel.getDiscountedPrice(product, isSwMember = true)
                    if (product.numericManualDiscount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rs. ${product.price} PKR",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8),
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rs. ${effectivePrice.toInt()} PKR (${product.numericManualDiscount}% OFF)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    } else if (isSwMemberModal && effectivePrice < product.numericPrice) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rs. ${product.price} PKR",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8),
                                style = TextStyle(textDecoration = TextDecoration.LineThrough)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rs. ${effectivePrice.toInt()} PKR (SW Member)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Rs. ${product.price} PKR",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0061E0)
                            )
                            if (!isSwMemberModal && swMemberModalPrice < product.numericPrice) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "★ SW Member Price: Rs. ${swMemberModalPrice.toInt()} PKR",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (!product.description.isNullOrBlank() && product.description != "null") product.description!! else "Professional laundry, dry cleaning, and garment care services by Snowhite.",
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { if (modalQty > 1) modalQty-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = Color(0xFF0061E0)
                                )
                            }
                            Text(
                                text = "$modalQty",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            IconButton(
                                onClick = { modalQty++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = Color(0xFF0061E0)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.addToCart(product, modalQty)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Add to Bag - Rs. ${totalModalPrice.toInt()} PKR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategorySequenceScore(categoryName: String): Int {
    val cat = categoryName.lowercase()
    if (cat.contains("laundry")) return 1
    if (cat.contains("gent") || cat.contains("dry clean")) return 2
    if (cat.contains("lady") || cat.contains("ladies")) return 3
    if (cat.contains("house") || cat.contains("linen")) return 4
    if (cat.contains("powder") || cat.contains("retail")) return 5
    return 99
}

private fun getProductSequenceScore(product: Product): Int {
    val name = product.name.trim().lowercase()
    val category = (product.categoryName ?: "").lowercase()
    val service = (product.serviceType ?: "").lowercase()

    val isLaundry = service.contains("laundry") || category.contains("laundry") || name.contains("laundry")
    val isDryClean = service.contains("dry clean") || category.contains("dry clean") || name.contains("dry clean")

    // 1. Gents Laundry (Start items: Qameez, Shalwar, Shalwar Suit, Shirt, Kurti Cotton, T-Shirt, Underwear, Vest)
    if (isLaundry || (category.contains("gent") && isLaundry)) {
        if (name == "qameez" || name.startsWith("qameez (laundry)")) return 101
        if (name == "shalwar" || name.startsWith("shalwar (laundry)")) return 102
        if (name.contains("shalwar suit (cotton") || (name.contains("shalwar suit") && isLaundry)) return 103
        if (name == "shirt" || (name.contains("shirt") && !name.contains("silk") && isLaundry)) return 104
        if (name.contains("kurti")) return 105
        if (name.contains("t-shirt") || name.contains("golf shirt")) return 106
        if (name.contains("under wear") || name.contains("underwear")) return 107
        if (name == "vest") return 108
        if (isLaundry) return 150
    }

    // 2. Gents Dry Cleaning (Pants/Jeans, Coat, Shirt Silk, Suit 2pcs, Suit 3pcs, Qameez Silk, Shalwar Silk, Shalwar Suit Silk, Cardigan, Sweater, Pajama, Safari Suit, Sherwani, Vest Coat, Tie)
    if (category.contains("gent") || isDryClean) {
        if (name.contains("pants") || (name.contains("jeans") && !category.contains("lady") && !category.contains("ladies"))) return 201
        if (name == "coat") return 202
        if (name.contains("shirt silk") || name.contains("silk shirt")) return 203
        if (name.contains("suit 2pcs") || name == "suit 2 pcs") return 204
        if (name.contains("suit 3pcs") || name.contains("suit pcs") || name.contains("suit 4pcs") || name.contains("suit")) return 205
        if (name.contains("qameez (silk") || name.contains("qameez silk") || name.contains("bosky") || name.contains("khadi")) return 206
        if (name.contains("shalwar (silk") || name.contains("shalwar silk")) return 207
        if (name.contains("shalwar suit (silk") || name.contains("shalwar suit silk")) return 208
        if (name.contains("cardigan")) return 209
        if (name.contains("sweater / pull over") || name.contains("sweater") || name.contains("pull over")) return 210
        if (name.contains("sweater sleeveless")) return 211
        if (name.contains("pajama") || name.contains("tracks")) return 212
        if (name.contains("safari suit")) return 213
        if (name.contains("sherwani")) return 214
        if (name.contains("vest coat")) return 215
        if (name == "tie") return 216
        if (category.contains("gent")) return 250
    }

    // 3. Ladies Wear (Qameez Plain/Cotton, Shalwar Plain/Cotton, Dupatta, Qameez Poly/Silk, Shalwar Poly/Silk, Dupatta Poly/Silk, Jeans/Trousers, Shawl)
    if (category.contains("lady") || category.contains("ladies")) {
        if (name.contains("qameez (plain") || name.contains("qameez cotton") || (name.contains("qameez") && !name.contains("silk"))) return 301
        if (name.contains("shalwar (plain") || name.contains("shalwar cotton") || (name.contains("shalwar") && !name.contains("silk"))) return 302
        if (name.contains("dupatta (plain") || name.contains("dupatta cotton") || (name.contains("dupatta") && !name.contains("silk"))) return 303
        if (name.contains("qameez (poly") || name.contains("qameez silk")) return 304
        if (name.contains("shalwar (poly") || name.contains("shalwar silk")) return 305
        if (name.contains("dupatta (poly") || name.contains("dupatta silk")) return 306
        if (name.contains("jeans") || name.contains("trousers")) return 307
        if (name.contains("shawl")) return 308
        return 350
    }

    // 4. House Hold / Linen (Blankets, Carpets, Curtains, Bed Sheets, Pillow Covers)
    if (category.contains("house") || category.contains("linen") || category.contains("carpet") || category.contains("curtain") || category.contains("blanket")) {
        if (name.contains("blanket")) return 401
        if (name.contains("carpet woolen") || name.contains("carpet synthetic")) return 402
        if (name.contains("carpet chinese") || name.contains("carpet persian")) return 403
        if (name.contains("exclusive carpet") || name.contains("carpet")) return 404
        if (name.contains("curtain")) return 405
        if (name.contains("bed sheet double") || name.contains("bedsheet double") || name.contains("bed sheet")) return 406
        if (name.contains("bed sheet single") || name.contains("bedsheet single")) return 407
        if (name.contains("pillow")) return 408
        return 450
    }

    // 5. Washing Powder & Products / Retail
    if (category.contains("powder") || category.contains("retail") || category.contains("product")) {
        return 500
    }

    return 999
}
