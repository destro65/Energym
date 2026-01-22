package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun ShopScreen() {
    val apiService = remember { ApiService() }
    var articulos by remember { mutableStateOf<List<Articulo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedArticulo by remember { mutableStateOf<Articulo?>(null) }

    LaunchedEffect(Unit) {
        articulos = apiService.getArticulos()
        isLoading = false
    }

    val products = articulos.filter { it.categoria == "Articulo" || it.categoria == "Suplemento" }
    val apparel = articulos.filter { it.categoria == "Vestimenta" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color(0xFF39006F))
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFB39DDB))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.widthIn(max = 1200.dp)
            ) {
                if (products.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Artículos",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (products.size > 2) {
                                Icon(Icons.Default.ArrowForwardIos, null, tint = Color(0xFFB39DDB).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(end = 32.dp)
                        ) {
                            items(products) { product ->
                                ProductCard(product, onClick = { selectedArticulo = it })
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                if (apparel.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Vestimenta",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (apparel.size > 2) {
                                Icon(Icons.Default.ArrowForwardIos, null, tint = Color(0xFFB39DDB).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(end = 32.dp)
                        ) {
                            items(apparel) { product ->
                                ProductCard(product, onClick = { selectedArticulo = it })
                            }
                        }
                    }
                }
                
                if (articulos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay artículos disponibles en este momento.", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // VISTA DE DETALLE
        AnimatedVisibility(
            visible = selectedArticulo != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedArticulo?.let { articulo ->
                ProductDetailOverlay(
                    articulo = articulo,
                    onDismiss = { selectedArticulo = null }
                )
            }
        }
    }
}

@Composable
fun ProductCard(product: Articulo, onClick: (Articulo) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick(product) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val fullUrl = getFullImageUrl(product.foto_url)
                if (fullUrl != null) {
                    KamelImage(
                        resource = asyncPainterResource(data = fullUrl),
                        contentDescription = product.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFB39DDB)) }
                    )
                } else {
                    Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(60.dp), tint = Color.White.copy(alpha = 0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                product.nombre, 
                color = Color.White, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            
            Text(
                "${product.precio} USD", 
                color = Color(0xFFB39DDB), 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                "Stock: ${product.stock}", 
                color = if (product.stock > 0) Color.Gray else Color.Red, 
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun ProductDetailOverlay(articulo: Articulo, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val message = "Hola, estoy interesado en el artículo: ${articulo.nombre}"
    val whatsappUrl = "https://wa.me/5930993347400?text=${message.replace(" ", "%20")}"
    
    var mainPhotoUrl by remember { mutableStateOf(articulo.foto_url) }
    val allPhotos = remember(articulo) {
        (listOf(articulo.foto_url) + (articulo.fotos_adicionales ?: emptyList())).filterNotNull().distinct()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) { },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF220044)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera con botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = articulo.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Galería de fotos
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val fullUrl = getFullImageUrl(mainPhotoUrl)
                        if (fullUrl != null) {
                            KamelImage(
                                resource = asyncPainterResource(data = fullUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                onLoading = { CircularProgressIndicator() }
                            )
                        }
                    }

                    if (allPhotos.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allPhotos) { url ->
                                val fullUrl = getFullImageUrl(url)
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 2.dp,
                                            color = if (mainPhotoUrl == url) Color(0xFFB39DDB) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { mainPhotoUrl = url }
                                ) {
                                    if (fullUrl != null) {
                                        KamelImage(
                                            resource = asyncPainterResource(data = fullUrl),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Información del producto
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFB39DDB),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = articulo.descripcion ?: "Sin descripción disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Precio",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${articulo.precio} USD",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFB39DDB),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Disponibles",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${articulo.stock} unidades",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (articulo.stock > 0) Color.White else Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { uriHandler.openUri(whatsappUrl) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB39DDB),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = articulo.stock > 0
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (articulo.stock > 0) "Adquirir ahora" else "Agotado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
