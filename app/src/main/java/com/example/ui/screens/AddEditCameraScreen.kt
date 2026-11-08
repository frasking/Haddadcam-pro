package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dvr
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.CameraFeed
import com.example.model.CameraProtocol
import com.example.model.DiscoveredDevice
import com.example.model.StreamQuality
import com.example.ui.theme.SurveillanceBgDark
import com.example.ui.theme.SurveillanceSurfaceDark
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SurveillanceViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCameraScreen(
    viewModel: SurveillanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.DASHBOARD)
    }

    val cameraToEdit by viewModel.cameraToEdit.collectAsState()
    val isScanningLan by viewModel.isScanningLan.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()
    val discoveredCameras by viewModel.discoveredCameras.collectAsState()

    var name by remember { mutableStateOf(cameraToEdit?.name ?: "كاميرا مراقبة جديدة") }
    var locationName by remember { mutableStateOf(cameraToEdit?.locationName ?: "المدخل الرئيسي") }
    var selectedProtocol by remember { mutableStateOf(cameraToEdit?.protocol ?: CameraProtocol.ONVIF) }
    var ipAddress by remember { mutableStateOf(cameraToEdit?.ipAddress ?: "192.168.1.100") }
    var portString by remember { mutableStateOf((cameraToEdit?.port ?: selectedProtocol.defaultPort).toString()) }
    var streamUrl by remember { mutableStateOf(cameraToEdit?.streamUrl ?: "rtsp://192.168.1.100:554/live/ch0") }
    var username by remember { mutableStateOf(cameraToEdit?.username ?: "admin") }
    var password by remember { mutableStateOf(cameraToEdit?.password ?: "") }
    var group by remember { mutableStateOf(cameraToEdit?.group ?: "الرئيسية") }
    var hasPtz by remember { mutableStateOf(cameraToEdit?.hasPtz ?: true) }
    var hasAudio by remember { mutableStateOf(cameraToEdit?.hasAudio ?: true) }
    var isEncrypted by remember { mutableStateOf(cameraToEdit?.isEncrypted ?: true) }
    var selectedQuality by remember { mutableStateOf(cameraToEdit?.quality ?: StreamQuality.FHD_1080P) }

    var showScanner by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (cameraToEdit != null) "تعديل إعدادات الكاميرا" else "إضافة كاميرا مراقبة جديدة",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.DASHBOARD) },
                        modifier = Modifier.testTag("add_camera_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LAN Network Scanner Card (كاشف الكاميرات الحقيقي بدون وهم)
            var scanSubnet by remember { mutableStateOf(viewModel.getDetectedSubnet()) }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101914)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF22382B))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Row with Icon and Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SyrianGreenPrimary.copy(alpha = 0.2f))
                                .border(1.dp, SyrianGreenLight, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                tint = SyrianGreenLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "كاشف كاميرات الشبكة الحقيقي (LAN Scanner)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "فحص فعلي لكاميرات ONVIF و WiFi و RTSP في شبكتك",
                                color = SyrianGreenLight,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Subnet Range Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "نطاق الشبكة (Subnet):",
                            color = Color(0xFF9EBAA9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = scanSubnet,
                            onValueChange = { scanSubnet = it },
                            placeholder = { Text("192.168.1", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SyrianGreenLight,
                                unfocusedBorderColor = Color(0xFF283A30),
                                focusedContainerColor = Color(0xFF0F1713),
                                unfocusedContainerColor = Color(0xFF0F1713),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Dedicated Full-Width Scan Button (زر فحص الشبكة الأفقي الواضح)
                    Button(
                        onClick = {
                            showScanner = true
                            viewModel.startLanScan(scanSubnet)
                        },
                        enabled = !isScanningLan,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SyrianGreenPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF1B2D22),
                            disabledContentColor = Color(0xFF788D80)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_lan_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScanningLan) "جاري فحص الشبكة المحلية..." else "بدء فحص الشبكة الآن (LAN Scan)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Scan in progress UI
                    if (isScanningLan) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { scanProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SyrianGreenLight,
                            trackColor = Color(0xFF1E2D24)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = scanStatusMessage,
                            color = SyrianGreenLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Scan finished but no cameras found
                    if (!isScanningLan && showScanner && discoveredCameras.isEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF181F1A))
                                .border(1.dp, Color(0xFF2A3D30), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "لم يتم العثور على أي كاميرا تستجيب في النطاق ($scanSubnet)",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• الفحص حقيقي 100%، لذا لا تظهر إلا الأجهزة التي ترد بالفعل على بروتوكول ONVIF أو منافذ RTSP.\n• تأكد من تشغيل كاميراتك وتوصيلها بنفس شبكة الراوتر (WiFi).\n• يمكنك إضافة الكاميرا يدوياً بإدخال عنوان الـ IP ورابط البث في الخانات أدناه.",
                                    color = Color(0xFFB5C9BD),
                                    fontSize = 10.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Discovered Devices List (راوتر، كاميرات، أجهزة الشبكة)
                    if (discoveredCameras.isNotEmpty() && showScanner) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "الأجهزة المكتشفة في شبكتك (الراوتر والكاميرات - ${discoveredCameras.size}):",
                            color = SyrianGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        discoveredCameras.forEach { dev ->
                            DiscoveredCameraRow(device = dev) {
                                name = dev.brand
                                ipAddress = dev.ip
                                portString = dev.port.toString()
                                selectedProtocol = dev.protocol
                                streamUrl = when (dev.protocol) {
                                    CameraProtocol.RTSP -> "rtsp://${dev.ip}:${dev.port}/live/ch0"
                                    CameraProtocol.ONVIF -> "rtsp://${dev.ip}:554/onvif1"
                                    CameraProtocol.WIRED_DVR -> "rtsp://${dev.ip}:554/cam/realmonitor?channel=1&subtype=0"
                                    else -> if (dev.brand.contains("راوتر")) "http://${dev.ip}:${dev.port}/" else "http://${dev.ip}:${dev.port}/video.cgi"
                                }
                            }
                        }
                    }
                }
            }

            // Protocol Selector
            Text(
                text = "نوع الكاميرا والبروتوكول المستخدم:",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CameraProtocol.values().take(3).forEach { proto ->
                    val isSel = selectedProtocol == proto
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedProtocol = proto
                            portString = proto.defaultPort.toString()
                        },
                        label = { Text(proto.displayName, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SyrianGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF141F18),
                            labelColor = Color(0xFFBACBC1)
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CameraProtocol.values().drop(3).forEach { proto ->
                    val isSel = selectedProtocol == proto
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedProtocol = proto
                            portString = proto.defaultPort.toString()
                        },
                        label = { Text(proto.displayName, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SyrianGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF141F18),
                            labelColor = Color(0xFFBACBC1)
                        )
                    )
                }
            }

            // Form Inputs
            SurveillanceInputField(
                value = name,
                onValueChange = { name = it },
                label = "اسم الكاميرا (مثال: بوابة دمشق، المدخل، المستودع)"
            )

            SurveillanceInputField(
                value = locationName,
                onValueChange = { locationName = it },
                label = "الموقع الجغرافي / التسمية التوضيحية"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(2f)) {
                    SurveillanceInputField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = "عنوان IP أو النطاق Host"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SurveillanceInputField(
                        value = portString,
                        onValueChange = { portString = it },
                        label = "المنفذ Port"
                    )
                }
            }

            SurveillanceInputField(
                value = streamUrl,
                onValueChange = { streamUrl = it },
                label = "رابط البث الكامل (RTSP / HLS / HTTP / m3u8)"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SurveillanceInputField(
                        value = username,
                        onValueChange = { username = it },
                        label = "اسم المستخدم"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SurveillanceInputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "كلمة المرور"
                    )
                }
            }

            SurveillanceInputField(
                value = group,
                onValueChange = { group = it },
                label = "المجموعة الأمنية (مثال: المحيط الخارجي، الإدارة)"
            )

            // Capabilities Toggles Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121B16)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF223328))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "خيارات الأمان والميزات المدعومة",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ToggleRow(
                        title = "تشفير البث والبيانات العسكري (AES-256)",
                        subtitle = "حماية الاتصال ضد التنصت والاختراق المحلي",
                        checked = isEncrypted,
                        onCheckedChange = { isEncrypted = it }
                    )

                    ToggleRow(
                        title = "دعم تحكم الحركة الميكانيكي (PTZ)",
                        subtitle = "تفعيل عصا التحكم الدائري والتقريب البصري",
                        checked = hasPtz,
                        onCheckedChange = { hasPtz = it }
                    )

                    ToggleRow(
                        title = "دعم نقل الصوت والاستماع (Audio)",
                        subtitle = "تمكين الاستماع والمحادثة ثنائية الاتجاه",
                        checked = hasAudio,
                        onCheckedChange = { hasAudio = it }
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val port = portString.toIntOrNull() ?: selectedProtocol.defaultPort
                    val newFeed = CameraFeed(
                        id = cameraToEdit?.id ?: UUID.randomUUID().toString(),
                        name = name.ifBlank { "كاميرا مراقبة" },
                        locationName = locationName.ifBlank { "موقع غير محدد" },
                        protocol = selectedProtocol,
                        streamUrl = streamUrl,
                        ipAddress = ipAddress,
                        port = port,
                        username = username,
                        password = password,
                        group = group.ifBlank { "الرئيسية" },
                        hasPtz = hasPtz,
                        hasAudio = hasAudio,
                        isEncrypted = isEncrypted,
                        quality = selectedQuality
                    )
                    viewModel.saveCamera(newFeed)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SyrianGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_camera_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (cameraToEdit != null) "حفظ التعديلات" else "إضافة الكاميرا إلى المراقبة",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SurveillanceInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SyrianGreenLight,
            unfocusedBorderColor = Color(0xFF283A30),
            focusedContainerColor = Color(0xFF0F1713),
            unfocusedContainerColor = Color(0xFF0F1713),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = SyrianGreenLight,
            unfocusedLabelColor = Color(0xFF8DA396)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = Color(0xFF8AA094), fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SyrianGreenPrimary,
                uncheckedThumbColor = Color(0xFF888888),
                uncheckedTrackColor = Color(0xFF222F26)
            )
        )
    }
}

@Composable
private fun DiscoveredCameraRow(
    device: DiscoveredDevice,
    onSelect: () -> Unit
) {
    val isRouter = device.brand.contains("راوتر")
    val isCamera = device.protocol == CameraProtocol.RTSP || device.protocol == CameraProtocol.ONVIF

    val icon = when {
        isRouter -> Icons.Default.Router
        device.protocol == CameraProtocol.WIRED_DVR -> Icons.Default.Dvr
        isCamera -> Icons.Default.Videocam
        else -> Icons.Default.Devices
    }

    val typeBadge = when {
        isRouter -> "🌐 راوتر / بوابة"
        device.protocol == CameraProtocol.RTSP -> "📹 كاميرا RTSP"
        device.protocol == CameraProtocol.ONVIF -> "🔍 كاميرا ONVIF"
        device.protocol == CameraProtocol.WIRED_DVR -> "📼 جهاز تسجيل DVR"
        else -> "💻 جهاز شبكة IP"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isRouter) Color(0xFF132319) else Color(0xFF101C15))
            .border(1.dp, if (isRouter) SyrianGreenLight else Color(0xFF243A2D), RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isRouter) SyrianGreenPrimary.copy(alpha = 0.25f) else Color(0xFF18281E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isRouter) SyrianGreenLight else Color(0xFFA5BBAE),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = device.brand,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${device.ip}:${device.port}",
                            color = SyrianGreenLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• $typeBadge",
                            color = Color(0xFF8BA294),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRouter) Color(0xFF193626) else SyrianGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "اختيار",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
