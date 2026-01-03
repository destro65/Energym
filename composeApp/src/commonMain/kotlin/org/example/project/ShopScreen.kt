package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Product(val name: String, val price: String, val icon: @Composable () -> Unit)

@Composable
fun ShopScreen() {
    val supplements = listOf(
        Product("Proteína Whey", "$49.99", { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(60.dp), tint = Color.White) }),
        Product("Creatina Monohidrato", "$29.99", { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(60.dp), tint = Color.White) }),
        Product("Pre-entreno Fusión", "$39.99", { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(60.dp), tint = Color.White) })
    )

    val apparel = listOf(
        Product("Camiseta Dry-Fit", "$24.99", { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(60.dp), tint = Color.White) }),
        Product("Leggings Compresión", "$44.99", { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(60.dp), tint = Color.White) }),
        Product("Sudadera Energym", "$59.99", { Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(60.dp), tint = Color.White) })
    )

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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.widthIn(max = 1200.dp)
        ) {
            item {
                Text(
                    "Suplementos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(supplements) { product ->
                        ProductCard(product)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    "Vestimenta",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(apparel) { product ->
                        ProductCard(product)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    val uriHandler = LocalUriHandler.current
    val message = "Hola, ayudame con una ${product.name}"
    val whatsappUrl = "https://wa.me/5930993347400?text=${message.replace(" ", "%20")}"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.width(180.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            product.icon()
            Spacer(modifier = Modifier.height(16.dp))
            Text(product.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(product.price, color = Color(0xFFB39DDB), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { uriHandler.openUri(whatsappUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB), contentColor = Color.Black)
            ) {
                // Usamos Call como proxy de WhatsApp ya que no hay icono oficial en material
                Icon(Icons.Default.Call, null) 
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pedir")
            }
        }
    }
}