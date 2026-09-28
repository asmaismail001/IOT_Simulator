package com.example.iotsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iotsimulator.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IOTSimulatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgCreamLight
                ) {
                    Esp32Dashboard()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MqttService.disconnect()
    }
}

@Composable
fun Esp32Dashboard() {
    var rawTemperature by remember { mutableStateOf<String?>(null) }
    var isLedOn by remember { mutableStateOf(false) }
    var isMqttConnected by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Connecting to MQTT broker...") }

    // MQTT connection and telemetry subscription (Preserved exact logic)
    LaunchedEffect(Unit) {
        MqttService.connect(
            onConnected = {
                isMqttConnected = true
                statusText = "Connected to HiveMQ Broker"
                MqttService.subscribeToTemperature { msg ->
                    rawTemperature = msg.trim()
                    statusText = "Receiving live telemetry"
                }
            },
            onError = { err ->
                isMqttConnected = false
                statusText = err
            }
        )
    }

    // Main Single-Screen Layout Container (Warm Cream Background)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BgCreamLight,
                        BgCreamGradientEnd
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. HEADER SECTION
            HeaderSection(
                isConnected = isMqttConnected
            )

            // 2. TEMPERATURE SENSOR CARD (Hero Card with Thick Gauge & Large Font)
            TemperatureSensorCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.4f, fill = false),
                isLedOn = isLedOn,
                temperatureValue = rawTemperature
            )

            // 3. ACTUATOR CARD
            ActuatorControlCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f, fill = false),
                isLedOn = isLedOn,
                onTurnOn = {
                    isLedOn = true
                    MqttService.publishLedCommand("on")
                    statusText = "LED turned ON • Sensor active"
                },
                onTurnOff = {
                    isLedOn = false
                    MqttService.publishLedCommand("off")
                    statusText = "LED turned OFF • Standby mode"
                }
            )

            // 4. NETWORK & BROKER STATS CARD
            NetworkBrokerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f, fill = false),
                isConnected = isMqttConnected,
                statusText = statusText
            )
        }
    }
}

// -------------------------------------------------------------------
// 1. HEADER SECTION
// -------------------------------------------------------------------
@Composable
fun HeaderSection(
    isConnected: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ESP32 SMART NODE",
                style = MaterialTheme.typography.labelSmall,
                color = SageTeal,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "IoT Telemetry Hub",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Live ESP32 monitoring",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryMuted,
                fontSize = 12.sp
            )
        }

        // Clean Sage / Coral Status Pill Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isConnected) SagePillBg else CoralPillBg,
            border = BorderStroke(
                1.dp,
                if (isConnected) SagePillBorder else CoralPillBorder
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnected) SageTeal.copy(alpha = pulseAlpha)
                            else CoralRed.copy(alpha = pulseAlpha)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isConnected) "ONLINE" else "OFFLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isConnected) SageTeal else CoralRed,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------------
// 2. TEMPERATURE SENSOR CARD (Thick Modern Gauge + Large Typography)
// -------------------------------------------------------------------
@Composable
fun TemperatureSensorCard(
    modifier: Modifier = Modifier,
    isLedOn: Boolean,
    temperatureValue: String?
) {
    val cardBorderColor by animateColorAsState(
        targetValue = if (isLedOn) SageTeal.copy(alpha = 0.4f) else CardBorder,
        animationSpec = tween(400),
        label = "cardBorderColor"
    )

    Surface(
        modifier = modifier.clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, cardBorderColor),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEMPERATURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageTeal,
                    letterSpacing = 1.4.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLedOn) SagePillBg else CardSecondaryCream,
                    border = BorderStroke(
                        1.dp,
                        if (isLedOn) SagePillBorder else Color.Transparent
                    )
                ) {
                    Text(
                        text = if (isLedOn) "● LIVE" else "STANDBY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLedOn) SageTeal else TextSecondaryMuted,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Thick Elegant Circular Gauge (190dp, 16dp stroke)
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .drawBehind {
                        val strokeWidth = 16.dp.toPx()
                        // Thick background track ring
                        drawCircle(
                            color = GaugeTrackColor,
                            style = Stroke(width = strokeWidth)
                        )
                        // Thick active sage arc
                        if (isLedOn) {
                            drawArc(
                                color = SageTeal,
                                startAngle = -90f,
                                sweepAngle = 250f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isLedOn) {
                        val displayTemp = temperatureValue ?: "--"
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = displayTemp,
                                style = MaterialTheme.typography.displayLarge,
                                color = TextPrimaryDark,
                                fontSize = 56.sp
                            )
                            Text(
                                text = "°",
                                style = MaterialTheme.typography.displayLarge,
                                color = SageTeal,
                                fontSize = 38.sp,
                                modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                            )
                        }
                        Text(
                            text = "Celsius",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "STANDBY",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondaryMuted,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sensor Idle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Helper text
            Text(
                text = if (isLedOn) "Synchronized with ESP32 telemetry every 5s"
                else "Sensor idle • Turn LED ON below to stream live data",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryMuted,
                fontSize = 11.5.sp
            )
        }
    }
}

// -------------------------------------------------------------------
// 3. ACTUATOR CARD (Sage & Blush Pastel Buttons)
// -------------------------------------------------------------------
@Composable
fun ActuatorControlCard(
    modifier: Modifier = Modifier,
    isLedOn: Boolean,
    onTurnOn: () -> Unit,
    onTurnOff: () -> Unit
) {
    // Smooth status color animation (Sage for ON, Coral for OFF)
    val animatedStatusColor by animateColorAsState(
        targetValue = if (isLedOn) SageTeal else CoralRed,
        animationSpec = tween(400),
        label = "statusColor"
    )

    Surface(
        modifier = modifier.clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTUATOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageTeal,
                    letterSpacing = 1.4.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLedOn) SagePillBg else CoralPillBg,
                    border = BorderStroke(
                        1.dp,
                        if (isLedOn) SagePillBorder else CoralPillBorder
                    )
                ) {
                    Text(
                        text = if (isLedOn) "● LED IS ON" else "● LED IS OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = animatedStatusColor,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ESP32 GPIO 26 (LED)",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Soft Sage Filled Button ("TURN ON")
                Button(
                    onClick = onTurnOn,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SageTeal,
                        contentColor = SageButtonText,
                        disabledContainerColor = CardSecondaryCream,
                        disabledContentColor = TextMuted
                    ),
                    enabled = !isLedOn
                ) {
                    Text(
                        text = "TURN ON",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                // Soft Blush Button ("TURN OFF")
                Button(
                    onClick = onTurnOff,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlushPink,
                        contentColor = BlushButtonText,
                        disabledContainerColor = CardSecondaryCream,
                        disabledContentColor = TextMuted
                    ),
                    border = BorderStroke(1.dp, if (isLedOn) CoralRed.copy(alpha = 0.3f) else OutlinedButtonBorder),
                    enabled = isLedOn
                ) {
                    Text(
                        text = "TURN OFF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------
// 4. NETWORK & BROKER STATS CARD (Clean, Label Above Value)
// -------------------------------------------------------------------
@Composable
fun NetworkBrokerCard(
    modifier: Modifier = Modifier,
    isConnected: Boolean,
    statusText: String
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NETWORK & BROKER",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageTeal,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "MQTT v3.1.1",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryMuted,
                    fontSize = 11.5.sp
                )
            }

            // Row 1: Broker & Protocol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatField(
                    label = "BROKER",
                    value = "broker.hivemq.com:1883",
                    modifier = Modifier.weight(1.2f)
                )
                StatField(
                    label = "PROTOCOL",
                    value = "MQTT v3.1.1",
                    modifier = Modifier.weight(0.8f),
                    alignment = Alignment.End
                )
            }

            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

            // Row 2: Telemetry & Command Topics (Ellipsis on long strings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatField(
                    label = "TELEMETRY TOPIC",
                    value = "iotsimulator_myapp/temperature",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatField(
                    label = "COMMAND TOPIC",
                    value = "iotsimulator_myapp/led",
                    modifier = Modifier.weight(1f),
                    alignment = Alignment.End
                )
            }

            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

            // Row 3: Connection Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) SageTeal else CoralRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "Connected: $statusText" else statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "QoS 1",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 11.5.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun StatField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = SageTeal,
            letterSpacing = 1.2.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimaryDark,
            fontSize = 11.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}