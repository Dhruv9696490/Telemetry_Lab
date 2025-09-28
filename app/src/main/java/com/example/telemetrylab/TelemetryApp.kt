package com.example.telemetrylab

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemetryApp() {
    var isRunning by remember { mutableStateOf(false) }
    var computeLoad by remember { mutableIntStateOf(2) }
    val latency by AppStatus.frameLatencyMs.collectAsState()
    var counter by remember { mutableLongStateOf(0L) }
    var batterySaver by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            counter++
            delay(200L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Telemetry Lab",
                        color = Color.DarkGray,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    isRunning = !isRunning

                    if (isRunning) {
                        ctx.startForegroundService(
                            Intent(ctx, TelemetryService::class.java).apply {
                                putExtra("computeLoad", computeLoad)
                            }
                        )
                    } else {
                        ctx.stopService(Intent(ctx, TelemetryService::class.java))
                    }

                }) {
                    Text(if (isRunning) "Stop" else "Start")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Compute load: $computeLoad")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = computeLoad.toFloat(),
                onValueChange = { computeLoad = it.toInt() },
                valueRange = 1f..5f,
                steps = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Frame latency (ms): ${String.format("%.1f", latency)}")

            Spacer(modifier = Modifier.height(12.dp))

            if (batterySaver) {
                Text(
                    "Power-save mode",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Activity feed (visible UI work):")

            LazyColumn(modifier = Modifier.height(200.dp)) {
                items((0 until 50).toList()) { item ->
                    Text("Counter: ${item + (counter % 100)}")
                }
            }
        }
    }
}
