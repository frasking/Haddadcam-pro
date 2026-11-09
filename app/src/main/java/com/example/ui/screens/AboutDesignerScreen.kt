package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.SyrianRevolutionFlag
import com.example.ui.theme.SurveillanceBgDark
import com.example.ui.theme.SurveillanceSurfaceDark
import com.example.ui.theme.SyrianGreenLight
import com.example.ui.theme.SyrianGreenPrimary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.SurveillanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDesignerScreen(
    viewModel: SurveillanceViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(ScreenDestination.DASHBOARD)
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "حول البرنامج والمصمم",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.DASHBOARD) },
                        modifier = Modifier.testTag("about_back_button")
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
            // Syrian Flag Hero Banner Graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF254131), RoundedCornerShape(14.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.syrian_flag_emblem_1793982185231),
                    contentDescription = "Syrian Revolutionary Green Flag and Security Shield",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Designer Profile Card (بطاقة المصمم فراس باسم حداد)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B15)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF274433))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Images Row: Designer Personal Portrait & App Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Designer Portrait Photo (صورة المصمم فراس باسم حداد)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF16251E))
                                    .border(2.5.dp, SyrianGreenLight, RoundedCornerShape(20.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.firas_designer_crop),
                                    contentDescription = "المصمم فراس باسم حداد",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "المصمم الشخصي",
                                color = SyrianGreenLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // App Shield & Camera Logo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF16251E))
                                    .border(2.dp, Color(0xFF2C4436), RoundedCornerShape(20.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.haddad_cam_icon_1793982172567),
                                    contentDescription = "Haddad Cam Pro Icon",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "شعار البرنامج",
                                color = Color(0xFF8FA89A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "المصمم: فراس باسم حداد",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "مبتكر ومطور نظام «كام حداد برو» للمراقبة والتشفير",
                        color = SyrianGreenLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Syrian Flag Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF16251E))
                            .border(1.dp, Color(0xFF2C4436), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SyrianRevolutionFlag(
                            modifier = Modifier.size(width = 24.dp, height = 16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "علم سوريا الأخضر الجديد • رمز السيادة والأمان",
                            color = Color(0xFFD6E6DC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Contact Info Section (معلومات التواصل مع المصمم)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111D17)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2A4A38))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SyrianGreenPrimary.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, SyrianGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = SyrianGreenLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "معلومات التواصل مع المصمم",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "فراس باسم حداد • للدعم الفني والاستفسارات",
                                color = Color(0xFF8FA89A),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phone 1: 0931800545
                    ContactItemRow(
                        icon = Icons.Default.Call,
                        label = "هاتف 1",
                        value = "0931800545"
                    ) {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0931800545"))
                        context.startActivity(dialIntent)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Phone 2: 0969140300
                    ContactItemRow(
                        icon = Icons.Default.Call,
                        label = "هاتف 2",
                        value = "0969140300"
                    ) {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0969140300"))
                        context.startActivity(dialIntent)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email 1: Syria@live.ca
                    ContactItemRow(
                        icon = Icons.Default.Email,
                        label = "البريد الإلكتروني 1",
                        value = "Syria@live.ca"
                    ) {
                        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:Syria@live.ca"))
                        context.startActivity(emailIntent)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email 2: Power.fras@gmail.com
                    ContactItemRow(
                        icon = Icons.Default.Email,
                        label = "البريد الإلكتروني 2",
                        value = "Power.fras@gmail.com"
                    ) {
                        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:Power.fras@gmail.com"))
                        context.startActivity(emailIntent)
                    }
                }
            }

            // Spiritual Dedication / Prayer Card (الدعاء لوالدي بالرحمة والغفران)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SyrianGreenLight.copy(alpha = 0.6f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Photo with beloved father
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, SyrianGreenLight, RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.firas_designer_photo),
                            contentDescription = "المصمم فراس حداد مع والده رحمه الله",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = SyrianGreenLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الدعاء لوالدي بالرحمة والغفران",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "اللهم اغفر لوالدي وارحمه وعافه واعف عنه، وأكرم نزله ووسع مدخله، واغسله بالماء والثلج والبرد، ونقه من الذنوب والخطايا كما ينقى الثوب الأبيض من الدنس، واجعل قبره روضة من رياض الجنة وأسكنه الفردوس الأعلى.",
                        color = Color(0xFFD3E7DC),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "نسألكم خالص الدعاء لوالدي بالرحمة والمغفرة",
                        color = SyrianGreenLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // App Identity & Vision (Professional text without any commercial comparisons)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111A15)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF23352A))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "عن تطبيق «كام حداد برو - المخصص والتشفير الآمن»",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نظام أمني متكامل وشامل للمراقبة والتشفير المخصص، صممه وبرمجه فراس باسم حداد ليمنحك تحكماً حراً وسلساً في متابعة وإدارة الكاميرات بأعلى درجات الخصوصية والتشفير والأمان الرقمي المستقل.",
                        color = Color(0xFFBACDC2),
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Camera Protocol Capabilities
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111A15)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF23352A))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "التوافقية الشاملة مع جميع الكاميرات والأنظمة:",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "كاميرات IPTV وقنوات البث" to "دعم كامل لصيغ HLS و m3u8 و RTMP و HTTP للبث عالي الجودة.",
                        "كاميرات WiFi و ONVIF الذكية" to "اكتشاف تلقائي عبر الشبكة المحلية مع دعم الملف الشخصي Profile S.",
                        "الكاميرات السلكية وأجهزة DVR/NVR" to "دعم منافذ RTSP وأجهزة Hikvision و Dahua و XMeye و Axis.",
                        "التحكم الحركي الكامل PTZ" to "عصا تحكم ثمانية الاتجاهات، تقريب بصري، وجولات دورية ذكية.",
                        "الاتصال الصوتي ثنائي الاتجاه" to "استماع حي مع ميكروفون مكبر للتحدث المباشر (Walkie-Talkie)."
                    ).forEach { (title, desc) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SyrianGreenLight, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = desc,
                                color = Color(0xFF8BA294),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }

            // System and Processor Compatibility Card (متطلبات النظام وتوافقية المعالجات)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111A15)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF23352A))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "التوافقية ومنصات أندرويد والمعالجات المدعومة:",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "إصدارات أندرويد (Android Versions)" to "يدعم من أندرويد 7.0 (Nougat) وحتى أحدث أندرويد 15 و 16 (يغطي أكثر من 98.5% من الأجهزة حول العالم).",
                        "معالجات كوالكوم (Qualcomm Snapdragon)" to "دعم كامل لجميع فئات Snapdragon 4xx و 6xx و 7xx و 8xx و Snapdragon 8 Gen 1/2/3/Elite.",
                        "معالجات ميدياتك (MediaTek)" to "دعم معالجات Dimensity الفائقة ومعالجات Helio (G/P/X) الاقتصادية والمتوسطة.",
                        "معالجات سامسونج وجوجل وهواوي" to "دعم معالجات Samsung Exynos و Google Tensor ومعالجات HiSilicon Kirin.",
                        "المعالجات الاقتصادية والشاشات" to "دعم معالجات UNISOC / Spreadtrum وأجهزة التلفاز الذكية وشاشات Android TV Box.",
                        "معماريات وحدة المعالجة المركزية (CPU)" to "يدعم معماريات ARM64 (arm64-v8a) ومعماريات ARM 32-bit (armeabi-v7a) ومعماريات x86_64."
                    ).forEach { (title, desc) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SyrianGreenLight, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = desc,
                                color = Color(0xFF8BA294),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }

            // Open Source License Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111A15)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF23352A))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = SyrianGreenLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ترخيص البرمجيات مفتوحة المصدر (GPL v3)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "هذا البرنامج حر ومفتوح المصدر بالكامل (Open Source تحت رخصة GNU General Public License v3). يمكنك مراجعة الكود، التعديل عليه، ونشره بحرية مع الحفاظ على حقوق المصمم فراس باسم حداد.",
                        color = Color(0xFFBACDC2),
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // APK Direct Download & Official Links Card
            val directApkDownloadUrl = "https://frasking.github.io/Haddadcam-pro/HaddadCamPro.apk"
            val githubPagesWebsiteUrl = "https://frasking.github.io/Haddadcam-pro/"
            val githubRepoUrl = "https://github.com/frasking/Haddadcam-pro"

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF142018)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SyrianGreenLight)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📥 تحميل وتثبيت تطبيق كام حداد برو (APK)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "ملف تثبيت أندرويد حقيقي ودائم مدى الحياة (حجم الملف: 29 ميغابايت)",
                        color = SyrianGreenLight,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Direct 1-Click APK Download Button
                    Button(
                        onClick = {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(directApkDownloadUrl))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                try {
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(githubPagesWebsiteUrl))
                                    context.startActivity(fallbackIntent)
                                } catch (ex: Exception) {
                                    viewModel.showMessage("تعذر فتح المتصفح تلقائياً")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SyrianGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_apk_browser_button")
                    ) {
                        Text(text = "⬇️ تحميل ملف APK المباشر الآن", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Open Official Website Button (GitHub Pages)
                    Button(
                        onClick = {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(githubPagesWebsiteUrl))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                viewModel.showMessage("تعذر فتح المتصفح")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16271D),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E4D3A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(text = "🌐 فتح الموقع الرسمي للتحميل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Open GitHub Repository Button
                    Button(
                        onClick = {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(githubRepoUrl))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                viewModel.showMessage("تعذر فتح المتصفح")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF101B14),
                            contentColor = Color(0xFFBACDC2)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF223529)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(text = "🐙 مستودع الكود المصدري على GitHub", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. 1-Click Copy Direct Download Link Button
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("HaddadCamPro APK URL", directApkDownloadUrl)
                            clipboard?.setPrimaryClip(clip)
                            viewModel.showMessage("✅ تم نسخ رابط التحميل المباشر إلى الحافظة بنجاح!")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F1A13),
                            contentColor = SyrianGreenLight
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SyrianGreenLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("copy_apk_link_button")
                    ) {
                        Text(text = "📋 نسخ رابط التحميل المباشر (Copy Link)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Direct Link text display
                    Text(
                        text = directApkDownloadUrl,
                        color = SyrianGreenLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "الرابط المباشر الدائم للمصمم فراس باسم حداد • شغال مدى الحياة",
                        color = Color(0xFF8BA294),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Share App Action
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "تطبيق «كام حداد برو» - المخصص والتشفير الآمن لمراقبة الكاميرات (IPTV و WiFi والسلكية) من تصميم فراس باسم حداد.\nرابط التحميل المباشر للـ APK:\n$directApkDownloadUrl\nالموقع الرسمي:\n$githubPagesWebsiteUrl"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة رابط تطبيق كام حداد برو"))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B2C23),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("share_app_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "مشاركة رابط التطبيق عبر الواتساب والتطبيقات", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ContactItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0C1611))
            .border(1.dp, Color(0xFF1E3327), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SyrianGreenLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = Color(0xFFA5BAAE),
                    fontSize = 11.sp
                )
            }

            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
