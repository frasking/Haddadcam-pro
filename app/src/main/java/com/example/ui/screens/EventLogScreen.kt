package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EventSeverity
import com.example.model.EventType
import com.example.model.SecurityEvent
import com.example.ui.theme.AlertRed
import com.example.ui.theme.SurveillanceBgDark
import com.example.ui.theme.SurveillanceSurfaceDark
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SurveillanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventLogScreen(
    viewModel: SurveillanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.DASHBOARD)
    }

    val events by viewModel.events.collectAsState()
    var selectedFilter by remember { mutableStateOf("الكل") }

    val filteredEvents = when (selectedFilter) {
        "حرجة" -> events.filter { it.severity == EventSeverity.CRITICAL }
        "حركة" -> events.filter { it.type == EventType.MOTION }
        "أمان" -> events.filter { it.type == EventType.SECURITY_SHIELD }
        else -> events
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "سجل التنبيهات والأحداث الأمنية",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${events.size} حدث أمني مسجل ومؤرخ",
                            color = Color(0xFFA2B4A8),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.DASHBOARD) },
                        modifier = Modifier.testTag("events_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearEvents() },
                        modifier = Modifier.testTag("clear_events_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Logs",
                            tint = Color(0xFFBACBC1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurveillanceSurfaceDark)
            )
        },
        containerColor = SurveillanceBgDark
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("الكل", "حرجة", "حركة", "أمان").forEach { f ->
                    val isSel = selectedFilter == f
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedFilter = f },
                        label = { Text(f, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SyrianGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF141F18),
                            labelColor = Color(0xFFB1C4B8)
                        )
                    )
                }
            }

            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد أحداث أمنية مسجلة في هذا التصنيف",
                        color = Color(0xFF8BA094),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        SecurityEventCard(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityEventCard(event: SecurityEvent) {
    val sdf = SimpleDateFormat("yyyy/MM/dd • HH:mm:ss", Locale.ENGLISH)
    val dateStr = sdf.format(Date(event.timestamp))

    val (icon, color) = when (event.type) {
        EventType.MOTION -> Icons.Default.DirectionsRun to SyrianGreenLight
        EventType.AUDIO -> Icons.Default.Hearing to WarningAmber
        EventType.TAMPER -> Icons.Default.Warning to AlertRed
        EventType.OFFLINE -> Icons.Default.WifiOff to AlertRed
        EventType.SECURITY_SHIELD -> Icons.Default.Security to Color(0xFF0EA5E9)
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111915)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF223228))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.cameraName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.type.titleAr,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.description,
                    color = Color(0xFFBACBC1),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateStr,
                    color = Color(0xFF758A7E),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
