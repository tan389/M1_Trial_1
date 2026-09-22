package com.example.projectm1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import kotlinx.coroutines.delay

@Composable
fun Button3Screen(onBack: () -> Unit) {
    BackHandler { onBack() }

    var totalSeconds by remember { mutableStateOf(60) }
    var timeLeft by remember { mutableStateOf(60) }
    var running by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var showCat by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (android.os.Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }.build()
    }

    LaunchedEffect(running, timeLeft) {
        if (running && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (running && timeLeft == 0) {
            running = false
            showCat = true
        }
    }

    fun formatMinSec(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }

    val percent = if (totalSeconds > 0) timeLeft.toFloat() / totalSeconds.toFloat() else 0f

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Top bar - stays at top
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { running = false; onBack() }) { Text("← Back") }
            Text("Timer", style = MaterialTheme.typography.titleMedium)
        }

        // === THIS BOX CENTERS EVERYTHING ===
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (showCat) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Time's Up! 🎉", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    AsyncImage(
                        model = "https://media.giphy.com/media/sIIhZliB2McAo/giphy.gif",
                        contentDescription = "Nyan Cat",
                        imageLoader = imageLoader,
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Done: ${formatMinSec(totalSeconds)}")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        showCat = false; hasStarted = false; timeLeft = totalSeconds; running = false
                    }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("Set New Timer")
                    }
                }
            } else if (!hasStarted) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Set your timer", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(32.dp))
                    Text(formatMinSec(totalSeconds), fontSize = 64.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(32.dp))
                    Slider(
                        value = totalSeconds.toFloat(),
                        onValueChange = { totalSeconds = it.toInt(); timeLeft = totalSeconds },
                        valueRange = 5f..900f,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0:05", color = Color.Gray)
                        Text("15:00", color = Color.Gray)
                    }
                    Spacer(Modifier.height(48.dp))
                    Button(
                        onClick = { hasStarted = true; running = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(64.dp)
                    ) {
                        Text("Start ${formatMinSec(totalSeconds)}", fontSize = 20.sp)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color(0xFFE0E0E0), style = Stroke(width = 20f))
                            drawArc(
                                color = Color(0xFF00C853),
                                startAngle = -90f,
                                sweepAngle = 360f * percent,
                                useCenter = false,
                                style = Stroke(width = 20f, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = formatMinSec(timeLeft), fontSize = 56.sp, fontWeight = FontWeight.Bold)
                            Text(text = "/ ${formatMinSec(totalSeconds)}", fontSize = 14.sp, color = Color.Gray)
                            Text(text = "${(percent * 100).toInt()}% left", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Button(
                            onClick = { running = !running },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (running) Color.Gray else Color(0xFF00C853))
                        ) { Text(if (running) "Pause" else "Resume") }
                        Button(
                            onClick = { running = false; hasStarted = false; timeLeft = totalSeconds; showCat = false },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                        ) { Text("Reset") }
                    }
                }
            }
        }
    }
}