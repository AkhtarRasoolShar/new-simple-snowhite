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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

    val finalProducts = remember(departmentFiltered, selectedCategoryId) {
        if (selectedCategoryId == null) {
            departmentFiltered
        } else {
            departmentFiltered.filter { it.categoryId == selectedCategoryId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        if (categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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
                items(categories) { cat ->
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
                Text(
                    text = "No products found in this category.",
                    fontSize = 16.sp,
                    color = Color(0xFF64748B)
                )
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
