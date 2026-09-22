package com.example.projectm1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import okhttp3.*
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

@Composable
fun Button2Screen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    var grid by remember { mutableStateOf(Array(16) { arrayOfNulls<String>(16) }) }
    var status by remember { mutableStateOf("Connecting...") }
    var pixelCount by remember { mutableStateOf(0) }
    var lastReset by remember { mutableStateOf(0L) }
    var seenZero by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val client = OkHttpClient() // <-- was getUnsafeClient(), change to normal client
            val request = Request.Builder().url("ws://20.43.80.147:8080").build()
            client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) { status = "LIVE" }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    val data = try { JSONObject(text) } catch (e: Exception) { return }
                    val x = try { data.getInt("x") } catch (e: Exception) { return }
                    val y = try { data.getInt("y") } catch (e: Exception) { return }
                    val color = try { data.getString("color") } catch (e: Exception) { return }
                    if (x!in 0..15 || y!in 0..15) return
                    val now = System.currentTimeMillis()
                    val isZero = x == 0 && y == 0
                    val existingZeroColor = grid[0][0]
                    val isActuallyNewPicture = isZero && seenZero && existingZeroColor!= null && existingZeroColor!= color && pixelCount > 10 && now - lastReset > 800
                    val shouldReset = pixelCount >= 256 || data.has("clear") || isActuallyNewPicture
                    var base = grid
                    if (shouldReset) {
                        base = Array(16) { arrayOfNulls<String>(16) }
                        pixelCount = 0; lastReset = now; seenZero = false
                    }
                    val newGrid = base.map { it.clone() }.toTypedArray()
                    val wasEmpty = newGrid[y][x] == null
                    newGrid[y][x] = color
                    grid = newGrid
                    if (wasEmpty) pixelCount++
                    if (isZero) seenZero = true
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { status = "Failed ${t.message}" }
            })
        } catch (e: Exception) { status = "Error ${e.message}" }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        // Grid - TRUE center of screen
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.background(Color.Black, RoundedCornerShape(6.dp)).padding(2.dp)) {
                Column(modifier = Modifier.background(Color(0xFFE0E0E0))) {
                    for (y in 0..15) {
                        Row {
                            for (x in 0..15) {
                                val c = grid[y][x]
                                Box(
                                    modifier = Modifier.size(22.dp).padding(0.5.dp)
                                        .background(if (c == null) Color.White else parseHex(c))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top controls - overlayed at top, so they don't push grid down
        Column(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack) { Text("← Back") }
                Text("Live Updates", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            Text(status, color = Color.Black, style = MaterialTheme.typography.bodySmall)
            Text("$pixelCount / 256", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { pixelCount / 256f },
                modifier = Modifier.fillMaxWidth(0.6f).height(8.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0),
            )
        }
    }
}

fun parseHex(hex: String): Color {
    return try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.White }
}

private fun getUnsafeClient(): OkHttpClient {
    val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(c: Array<X509Certificate>, a: String) {}
        override fun checkServerTrusted(c: Array<X509Certificate>, a: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    val ssl = SSLContext.getInstance("SSL")
    ssl.init(null, trustAll, SecureRandom())
    return OkHttpClient.Builder()
        .sslSocketFactory(ssl.socketFactory, trustAll[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()
}