package dev.gurk.slurpy.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CryptoWallet(
    val symbol: String,
    val name: String,
    val address: String,
)

val APP_WALLETS = listOf(
    CryptoWallet(
        symbol = "BTC",
        name = "Bitcoin",
        address = "bc1qytee2a0z2tg4k4zdtqj08zpellkrecrgdgg36z",
    ),
    CryptoWallet(
        symbol = "ETH",
        name = "Ethereum",
        address = "0x49372575383aEA56b78524C4FD0873DE1175e9be",
    ),
    CryptoWallet(
        symbol = "LTC",
        name = "Litecoin",
        address = "ltc1qk2pe4srtcjt2cnnp5fcfvmknwckjus9ge8v5p9",
    ),
    CryptoWallet(
        symbol = "XMR",
        name = "Monero",
        address = "43HUf74vj1XTo9utkkLoniUSqKfXzM2ST1msHfc22CrjRg1RyBspUoxdLa2rergVgMSXoep2wYrwsiR4F1jRGcok7jgGvUz",
    ),
)

const val BUY_ME_A_COFFEE_URL = "https://buymeacoffee.com/gurkenwerfer"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportDialog(
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var copiedSymbol by remember { mutableStateOf<String?>(null) }

    fun copyAddress(wallet: CryptoWallet) {
        clipboardManager.setText(AnnotatedString(wallet.address))
        copiedSymbol = wallet.symbol
        Toast.makeText(context, "${wallet.symbol} address copied to clipboard!", Toast.LENGTH_SHORT).show()
        scope.launch {
            delay(2000)
            if (copiedSymbol == wallet.symbol) {
                copiedSymbol = null
            }
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF19191C),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B2B30)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Row: Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "About",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C30))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // App Branding Header
                Text(
                    text = "Slurpy",
                    color = slurpy.accent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { uriHandler.openUri("https://github.com/stefexec") }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "© 2026 Slurpy v1.1.0 • by ",
                        color = Color(0xFF8E8E93),
                        fontSize = 13.sp,
                    )
                    Text(
                        text = "Gurkenwerfer",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(22.dp))

                // Section Label: SUPPORT THE DEVELOPER
                Text(
                    text = "SUPPORT THE DEVELOPER",
                    color = Color(0xFF8E8E93),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )

                Spacer(Modifier.height(12.dp))

                // Crypto Wallets
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    APP_WALLETS.forEach { wallet ->
                        val isCopied = copiedSymbol == wallet.symbol
                        val truncated = if (wallet.address.length > 20) {
                            "${wallet.address.take(18)}..."
                        } else {
                            wallet.address
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF212126))
                                .border(1.dp, if (isCopied) Color(0xFF30D158) else Color(0xFF2D2D33), RoundedCornerShape(14.dp))
                                .clickable { copyAddress(wallet) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = wallet.symbol,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(44.dp),
                            )

                            Text(
                                text = truncated,
                                color = if (isCopied) Color(0xFF30D158) else Color(0xFF9E9EA6),
                                fontSize = 12.5.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                            )

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isCopied) Color(0x3330D158) else Color(0xFF2A2A30)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                    contentDescription = "Copy address",
                                    tint = if (isCopied) Color(0xFF30D158) else Color(0xFFB0B0B8),
                                    modifier = Modifier.size(15.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Buy Me A Coffee Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFDD00))
                        .clickable { uriHandler.openUri(BUY_ME_A_COFFEE_URL) }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x22000000)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalCafe,
                            contentDescription = "Coffee",
                            tint = Color.Black,
                            modifier = Modifier.size(19.dp),
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Alternatively, you can Buy\nMe a Coffee. Thank you! 💖",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 17.sp,
                    )
                }
            }
        }
    }
}
