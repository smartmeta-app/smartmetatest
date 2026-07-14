package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartMetaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMetaApp(viewModel: SmartMetaViewModel = viewModel()) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val loggedInRole by viewModel.loggedInRole.collectAsStateWithLifecycle()

    // Dynamic state trackers
    val workingHours by viewModel.workingHours.collectAsStateWithLifecycle()
    val pointPerKg by viewModel.pointPerKg.collectAsStateWithLifecycle()

    // Permission launcher for Location and Notifications
    val permissionsToRequest = mutableListOf<String>().apply {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            Toast.makeText(context, "Izin lokasi disetujui untuk pelacakan petugas.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val needsRequest = permissionsToRequest.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needsRequest) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.Login -> LoginScreen(viewModel)
                    Screen.PetugasDashboard -> PetugasDashboardScreen(viewModel)
                    Screen.WargaDashboard -> WargaDashboardScreen(viewModel)
                    Screen.AdminSettings -> AdminSettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: SmartMetaViewModel) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var activeLoginRole by remember { mutableStateOf<String?>(null) } // "Petugas" or "Warga"
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    // When the login role changes, auto-fill the credentials
    LaunchedEffect(activeLoginRole) {
        if (activeLoginRole == "Petugas") {
            username = "testpetugas"
            password = "test"
        } else if (activeLoginRole == "Warga") {
            username = "testwarga"
            password = "test"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header from "Sophisticated Dark" Theme
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SMART",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1.5).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "META",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.5).sp
                        ),
                        color = Color(0xFFD0BCFF)
                    )
                }
                Text(
                    text = "Integrated Smart Environment System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Role 1: Petugas (Officer) Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeLoginRole = "Petugas" },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Box
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF4F378B), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Petugas Icon",
                                tint = Color(0xFFEADDFF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Live Tracking Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(Color(0xFF2B2930), RoundedCornerShape(100.dp))
                                .border(1.dp, Color(0x3322C55E), RoundedCornerShape(100.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Text(
                                text = "LIVE TRACKING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF22C55E)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Petugas",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Login for tracking, attendance, and reporting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Feature Pill Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Foreground Tracking", "Absensi GPS", "Chat Service").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { activeLoginRole = "Petugas" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        )
                    ) {
                        Text(
                            text = "LOGIN PETUGAS",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Role 2: Warga (Citizen) Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeLoginRole = "Warga" },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Box
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF2E312E), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = "Warga Icon",
                                tint = Color(0xFFB4F0B5),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFF2B2930),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "BANK SAMPAH",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Warga",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Report waste, earn points, and exchange for groceries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Feature Pill Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Poin & Pajak", "Nearby Maps", "Transfer Poin").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { activeLoginRole = "Warga" },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB4F0B5),
                            contentColor = Color(0xFF00390A)
                        )
                    ) {
                        Text(
                            text = "LOGIN WARGA",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Bottom Hints matching "Sophisticated Dark" HTML design
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Petugas: testpetugas / test",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(14.dp)
                            .background(Color(0xFF1E293B))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Warga: testwarga / test",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = Color(0xFF64748B)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(4.dp)
                        .background(Color(0xFF334155), CircleShape)
                )
            }
        }

        // Credentials Login Sheet Modal
        AnimatedVisibility(
            visible = activeLoginRole != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp), // allow some spacing from top
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                border = BorderStroke(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Login as $activeLoginRole",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        IconButton(onClick = { activeLoginRole = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFD0BCFF)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFF64748B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD0BCFF)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFF64748B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (authError != null) {
                        Text(
                            text = authError ?: "",
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeLoginRole = null },
                            modifier = Modifier
                                .weight(1.0f)
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val success = viewModel.login(username, password)
                                if (success) {
                                    activeLoginRole = null
                                    Toast.makeText(context, "Selamat datang kembali!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("submit_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeLoginRole == "Warga") Color(0xFFB4F0B5) else Color(0xFFD0BCFF),
                                contentColor = if (activeLoginRole == "Warga") Color(0xFF00390A) else Color(0xFF381E72)
                            )
                        ) {
                            Text(
                                text = "Masuk",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ----------------------------------------------------
// PETUGAS (OFFICER) SCREEN & SECTIONS
// ----------------------------------------------------
@Composable
fun PetugasDashboardScreen(viewModel: SmartMetaViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val isTrackingServiceRunning by TrackingService.isServiceRunning.collectAsStateWithLifecycle()
    val isClockedIn by viewModel.isClockedIn.collectAsStateWithLifecycle()
    val workingHours by viewModel.workingHours.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // App Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text("Budi Santoso", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(if (isClockedIn) Color.Green else Color.Red, CircleShape))
                                Text(
                                    text = if (isClockedIn) "Sedang Bertugas" else "Tidak Aktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isTrackingServiceRunning) {
                                TrackingService.stopService(context)
                            }
                            viewModel.logout()
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Highlight foreground tracking status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Pelacakan Latar Belakang (Foreground)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            if (isTrackingServiceRunning) "AKTIF - Posisi Anda dilacak oleh warga" else "NONAKTIF - Warga tidak dapat melihat posisi Anda",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isTrackingServiceRunning,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                TrackingService.startService(context)
                                Toast.makeText(context, "Foreground Service Dimulai", Toast.LENGTH_SHORT).show()
                            } else {
                                TrackingService.stopService(context)
                                Toast.makeText(context, "Foreground Service Dihentikan", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Navigation Tabs for Petugas
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Work, null) }, text = { Text("Absen") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.List, null) }, text = { Text("Laporan") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Chat, null) }, text = { Text("Komunikasi") })
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> PetugasAbsensiTab(viewModel)
                1 -> PetugasLaporanTab(viewModel)
                2 -> PetugasChatTab(viewModel)
            }
        }

        // Admin Dashboard access drawer-link for easy configuration set up
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { viewModel.navigateTo(Screen.AdminSettings) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Admin", tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Buka Dashboard Admin (Set Jam Kerja & Poin Bank Sampah)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PetugasAbsensiTab(viewModel: SmartMetaViewModel) {
    val isClockedIn by viewModel.isClockedIn.collectAsStateWithLifecycle()
    val clockInTime by viewModel.clockInTime.collectAsStateWithLifecycle()
    val clockOutTime by viewModel.clockOutTime.collectAsStateWithLifecycle()
    val workingHours by viewModel.workingHours.collectAsStateWithLifecycle()
    val isTrackingServiceRunning by TrackingService.isServiceRunning.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Schedule Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Column {
                    Text("Jadwal Kerja Hari Ini", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Jam Operasional: $workingHours", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Pastikan menghidupkan GPS & Foreground Service saat bertugas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Clock In/Out controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isClockedIn) "Status: SEDANG BERTUGAS" else "Status: BELUM ABSEN MASUK",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isClockedIn) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = {
                        if (!isClockedIn) {
                            viewModel.clockIn()
                            // Force-start foreground service automatically for convenience
                            if (!isTrackingServiceRunning) {
                                TrackingService.startService(context)
                            }
                        } else {
                            viewModel.clockOut()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClockedIn) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                    )
                ) {
                    Icon(
                        imageVector = if (isClockedIn) Icons.Default.ExitToApp else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isClockedIn) "Absen Keluar (Clock Out)" else "Absen Masuk (Clock In)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Masuk", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(clockInTime ?: "--:--", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Keluar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(clockOutTime ?: "--:--", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Live Simulated Map Widget to show tracking
        Text("Pelacakan Peta Lokasi Petugas (Live GPS)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            val coords by TrackingService.simulatedLocation.collectAsStateWithLifecycle()
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Draw simulated municipal map grid
                        val w = size.width
                        val h = size.height
                        val stroke = 2f
                        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                        // Draw grid lines
                        for (i in 1..4) {
                            drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(w * i / 5f, 0f), Offset(w * i / 5f, h), strokeWidth = stroke)
                            drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, h * i / 5f), Offset(w, h * i / 5f), strokeWidth = stroke)
                        }

                        // Drawing central office location
                        drawCircle(Color.Gray.copy(alpha = 0.1f), center = Offset(w / 2f, h / 2f), radius = 60f)
                        drawCircle(Color.Blue.copy(alpha = 0.6f), center = Offset(w / 2f, h / 2f), radius = 8f)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Animated location beacon representing the simulated position
                val pulseScale = rememberInfiniteTransition(label = "").animateFloat(
                    initialValue = 10f,
                    targetValue = 40f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = ""
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    // Position status indicator text
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "GPS: %.6f, %.6f".format(coords.first, coords.second),
                            modifier = Modifier.padding(6.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Simulated tracking marker in center
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .align(Alignment.Center)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(Color.Green.copy(alpha = 0.3f), radius = pulseScale.value)
                            drawCircle(Color.Green, radius = 6f)
                        }
                    }

                    Text(
                        text = "Lokasi Anda saat ini (Latar Belakang Aktif)",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun PetugasLaporanTab(viewModel: SmartMetaViewModel) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var reportTitle by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }
    var reportCategory by remember { mutableStateOf("Kebersihan") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daftar Laporan Kerja", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Button(
                onClick = { showForm = !showForm },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lapor Kerja")
            }
        }

        // Form collapse
        AnimatedVisibility(visible = showForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Kirim Laporan Pekerjaan Baru", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = reportTitle,
                        onValueChange = { reportTitle = it },
                        label = { Text("Judul Pekerjaan/Laporan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("Deskripsi Pekerjaan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simulated Camera selector placeholder
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Camera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Ambil Foto Laporan Kerja", style = MaterialTheme.typography.bodyMedium)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text("Simulasi Terlampir", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showForm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (reportTitle.isNotBlank() && reportDesc.isNotBlank()) {
                                    viewModel.addWorkReport(reportTitle, reportDesc, false, "Budi Santoso")
                                    reportTitle = ""
                                    reportDesc = ""
                                    showForm = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kirim")
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reports) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (report.isFromCitizen) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (report.isFromCitizen) "Laporan Warga" else "Laporan Kerja Petugas",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (report.isFromCitizen) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(report.timestamp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(report.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(report.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Dilaporkan oleh: ${report.reporter}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Text(report.status, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetugasChatTab(viewModel: SmartMetaViewModel) {
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    var selectedUser by remember { mutableStateOf("testwarga") } // Default chat with citizen
    var messageText by remember { mutableStateOf("") }
    val listState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Chat Partner Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { selectedUser = "testwarga" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedUser == "testwarga") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chat Warga", style = MaterialTheme.typography.labelMedium, color = if (selectedUser == "testwarga") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("testwarga", style = MaterialTheme.typography.bodySmall, color = if (selectedUser == "testwarga") MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }

            Button(
                onClick = { selectedUser = "Admin SMARTMETA" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedUser == "Admin SMARTMETA") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chat Admin", style = MaterialTheme.typography.labelMedium, color = if (selectedUser == "Admin SMARTMETA") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Pusat Kontrol", style = MaterialTheme.typography.bodySmall, color = if (selectedUser == "Admin SMARTMETA") MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }

        Divider()

        // Conversation History
        val filteredChats = chats.filter {
            (it.sender == "testpetugas" && it.receiver == selectedUser) ||
                    (it.sender == selectedUser && it.receiver == "testpetugas") ||
                    (it.sender == "Budi Santoso" && it.receiver == selectedUser) ||
                    (it.sender == selectedUser && it.receiver == "Budi Santoso")
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(listState)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredChats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada obrolan. Kirim pesan pertama Anda!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                filteredChats.forEach { msg ->
                    val isOwn = msg.isFromOfficer || msg.sender == "testpetugas" || msg.sender == "Budi Santoso"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOwn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isOwn) 16.dp else 0.dp,
                                bottomEnd = if (isOwn) 0.dp else 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.timestamp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Ketik pesan Anda...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage("testpetugas", selectedUser, messageText, true)
                        val textSent = messageText
                        messageText = ""
                        scope.launch {
                            delay(100)
                            listState.animateScrollTo(listState.maxValue)
                            
                            // Mock auto-reply in 2 seconds for interactive simulation!
                            delay(2000)
                            val reply = when {
                                textSent.contains("sampah", ignoreCase = true) -> "Siap pak, saya akan siapkan tumpukan sampah di depan rumah agar mudah diangkut."
                                textSent.contains("jemput", ignoreCase = true) -> "Oke siap pak! Ditunggu kedatangan armadanya."
                                else -> "Terima kasih infonya pak petugas! Sangat membantu."
                            }
                            viewModel.sendChatMessage(selectedUser, "testpetugas", reply, false)
                            delay(100)
                            listState.animateScrollTo(listState.maxValue)
                        }
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// ----------------------------------------------------
// WARGA (CITIZEN) SCREEN & SECTIONS
// ----------------------------------------------------
@Composable
fun WargaDashboardScreen(viewModel: SmartMetaViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val citizenPoints by viewModel.citizenPoints.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // App Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text("Andi Wijaya (Warga)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onTertiary)
                            Text("Eco-Citizen RT 05", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f))
                        }
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.onTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accumulator Points Board
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Saldo Poin Bank Sampah",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(24.dp))
                            Text(
                                "$citizenPoints POIN",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.Yellow
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Yellow
                    ) {
                        Text(
                            text = "Eco-Aktif",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Navigation Tabs for Warga
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Map, null) }, text = { Text("Peta & Lapor") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.ShoppingBag, null) }, text = { Text("Bank Sampah") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Chat, null) }, text = { Text("Hubungi") })
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> WargaMapLaporTab(viewModel)
                1 -> WargaBankSampahTab(viewModel)
                2 -> WargaChatTab(viewModel)
            }
        }

        // Configuration shortcut for demo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { viewModel.navigateTo(Screen.AdminSettings) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Admin", tint = MaterialTheme.colorScheme.tertiary)
                Text(
                    "Buka Dashboard Admin (Set Jam Kerja & Poin Bank Sampah)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun WargaMapLaporTab(viewModel: SmartMetaViewModel) {
    val officers by viewModel.officers.collectAsStateWithLifecycle()
    var selectedOfficer by remember { mutableStateOf<Officer?>(null) }
    var reportTitle by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Map Info banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Peta Lokasi Petugas Terdekat", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text("GPS Live", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
        }

        // Interactive Map of Nearby Officers
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val w = size.width
                        val h = size.height

                        // Draw Grid lines
                        for (i in 1..4) {
                            drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(w * i / 5f, 0f), Offset(w * i / 5f, h), strokeWidth = 2f)
                            drawLine(Color.LightGray.copy(alpha = 0.4f), Offset(0f, h * i / 5f), Offset(w, h * i / 5f), strokeWidth = 2f)
                        }

                        // Municipal Center
                        drawCircle(Color.LightGray.copy(alpha = 0.2f), center = Offset(w / 2f, h / 2f), radius = 80f)
                    }
            ) {
                // Citizen Node in the center of map
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), CircleShape)
                        .clickable {
                            Toast
                                .makeText(
                                    viewModel.getApplication(),
                                    "Lokasi Anda saat ini (Eco-Citizen RT 05)",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Lokasi Saya", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                }

                // Interactive Node for Officer 1 (Budi Santoso) - Top Left
                Box(
                    modifier = Modifier
                        .offset(x = 60.dp, y = 40.dp)
                        .size(36.dp)
                        .background(Color(0xFF2E7D32), CircleShape)
                        .clickable { selectedOfficer = officers.firstOrNull { it.id == "O1" } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }

                // Interactive Node for Officer 2 (Rahmat Hidayat) - Bottom Right
                Box(
                    modifier = Modifier
                        .offset(x = 240.dp, y = 140.dp)
                        .size(36.dp)
                        .background(Color(0xFFF57C00), CircleShape)
                        .clickable { selectedOfficer = officers.firstOrNull { it.id == "O2" } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }

                Text(
                    text = "Klik pada ikon petugas hijau/oranye untuk berinteraksi",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Selection details card with chat integration!
        AnimatedVisibility(visible = selectedOfficer != null) {
            selectedOfficer?.let { officer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(officer.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(officer.role, style = MaterialTheme.typography.bodyMedium)
                            Text("Status: ${officer.status}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (officer.status == "Aktif") Color(0xFF2E7D32) else Color(0xFFF57C00))
                        }

                        Button(
                            onClick = {
                                // Close this card and trigger simulated communication
                                Toast.makeText(viewModel.getApplication(), "Menghubungi ${officer.name}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat")
                        }
                    }
                }
            }
        }

        // Create Report Form (Lapor)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layanan Pengaduan Warga", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Buat Laporan")
            }
        }

        AnimatedVisibility(visible = showForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Buat Pengaduan / Laporan Masalah Lingkungan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = reportTitle,
                        onValueChange = { reportTitle = it },
                        label = { Text("Judul Laporan (misal: Selokan RT 05 Sumbat)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("Deskripsi Detail Masalah & Alamat Lokasi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showForm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (reportTitle.isNotBlank() && reportDesc.isNotBlank()) {
                                    viewModel.addWorkReport(reportTitle, reportDesc, true, "Andi (Warga)", "Diproses")
                                    reportTitle = ""
                                    reportDesc = ""
                                    showForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kirim Laporan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WargaBankSampahTab(viewModel: SmartMetaViewModel) {
    val citizenPoints by viewModel.citizenPoints.collectAsStateWithLifecycle()
    val rewards by viewModel.rewards.collectAsStateWithLifecycle()
    val pointRate by viewModel.pointPerKg.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Simulation states
    var plasticWeightInput by remember { mutableStateOf("") }
    var transferNameInput by remember { mutableStateOf("") }
    var transferAmountInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Points balance visual header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Eko-Kalkulator & Informasi Nilai Tukar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Setiap 1 Kg Sampah Plastik/Logam terpilah dihargai: $pointRate POIN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Text("Kumpulkan sampah rumah tangga Anda, timbang di pos, dan tukarkan poin menjadi sembako berharga atau bayar pajak!", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Action Section 1: Setor Sampah (Simulate trash weigh-in)
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Simulasi Setoran Bank Sampah RT 05", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Simulasikan Anda menyetorkan sampah plastik ke pos timbangan:", style = MaterialTheme.typography.bodySmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = plasticWeightInput,
                        onValueChange = { plasticWeightInput = it },
                        label = { Text("Berat Sampah (Kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val weight = plasticWeightInput.toDoubleOrNull()
                            if (weight != null && weight > 0) {
                                viewModel.earnPoints(weight)
                                plasticWeightInput = ""
                                Toast.makeText(context, "Setoran berhasil dicatat! Poin ditambahkan.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Masukkan angka berat yang valid!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Simulasikan Setor")
                    }
                }
            }
        }

        // Action Section 2: Tukar Poin (Redeem points grid)
        Text("Tukar Poin Menjadi Sembako & Bayar Pajak", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rewards.forEach { reward ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (reward.category == "Sembako") Color(0xFFE8F5E9) else Color(0xFFE3F2FD),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (reward.category == "Sembako") Icons.Default.ShoppingBag else Icons.Default.Payment,
                                contentDescription = null,
                                tint = if (reward.category == "Sembako") Color(0xFF2E7D32) else Color(0xFF1565C0)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(reward.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(reward.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${reward.pointsNeeded} Poin dibutuhkan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }

                        Button(
                            onClick = {
                                val success = viewModel.redeemReward(reward)
                                if (success) {
                                    Toast.makeText(context, "Sukses! Kode penukaran dikirim via SMS.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Poin Anda tidak mencukupi!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (citizenPoints >= reward.pointsNeeded) Color(0xFF2E7D32) else Color.Gray
                            )
                        ) {
                            Text("Tukar")
                        }
                    }
                }
            }
        }

        // Action Section 3: Transfer Poin ke Sesama Warga
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Transfer Poin ke Sesama Warga", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Kirim poin Bank Sampah Anda secara instan ke tetangga:", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = transferNameInput,
                    onValueChange = { transferNameInput = it },
                    label = { Text("Username Penerima (Warga)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = transferAmountInput,
                        onValueChange = { transferAmountInput = it },
                        label = { Text("Jumlah Poin") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val amount = transferAmountInput.toIntOrNull()
                            if (transferNameInput.isNotBlank() && amount != null && amount > 0) {
                                val success = viewModel.transferPoints(transferNameInput, amount)
                                if (success) {
                                    Toast.makeText(context, "Poin berhasil dikirim ke $transferNameInput!", Toast.LENGTH_SHORT).show()
                                    transferNameInput = ""
                                    transferAmountInput = ""
                                } else {
                                    Toast.makeText(context, "Saldo poin Anda kurang!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Formulir tidak valid!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Kirim Poin")
                    }
                }
            }
        }
    }
}

@Composable
fun WargaChatTab(viewModel: SmartMetaViewModel) {
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    var selectedPartner by remember { mutableStateOf("Budi Santoso") } // Default chat with nearest officer
    var messageText by remember { mutableStateOf("") }
    val listState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Chat Partner Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { selectedPartner = "Budi Santoso" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPartner == "Budi Santoso") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Petugas Terdekat", style = MaterialTheme.typography.labelMedium, color = if (selectedPartner == "Budi Santoso") MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Budi Santoso", style = MaterialTheme.typography.bodySmall, color = if (selectedPartner == "Budi Santoso") MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }

            Button(
                onClick = { selectedPartner = "Admin SMARTMETA" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPartner == "Admin SMARTMETA") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chat Admin", style = MaterialTheme.typography.labelMedium, color = if (selectedPartner == "Admin SMARTMETA") MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Pusat Pengaduan", style = MaterialTheme.typography.bodySmall, color = if (selectedPartner == "Admin SMARTMETA") MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }

        Divider()

        // Conversation history
        val filteredChats = chats.filter {
            (it.sender == "testwarga" && it.receiver == selectedPartner) ||
                    (it.sender == selectedPartner && it.receiver == "testwarga") ||
                    (it.sender == "Andi (Warga)" && it.receiver == selectedPartner) ||
                    (it.sender == selectedPartner && it.receiver == "Andi (Warga)")
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(listState)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredChats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada obrolan dengan petugas ini.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                filteredChats.forEach { msg ->
                    val isOwn = !msg.isFromOfficer && msg.sender == "testwarga"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOwn) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isOwn) 16.dp else 0.dp,
                                bottomEnd = if (isOwn) 0.dp else 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.timestamp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Ketik pesan ke petugas...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage("testwarga", selectedPartner, messageText, false)
                        val textSent = messageText
                        messageText = ""
                        scope.launch {
                            delay(100)
                            listState.animateScrollTo(listState.maxValue)

                            // Mock auto-reply simulation in 2 seconds
                            delay(2000)
                            val reply = when {
                                textSent.contains("lapor", ignoreCase = true) || textSent.contains("sampah", ignoreCase = true) -> "Halo Pak Andi, laporan timbangan/sampah Anda sudah kami catat di dashboard. Petugas segera merapat ke lokasi Anda."
                                else -> "Siap pak, ada hal lain yang bisa kami bantu dari pos pelayanan terdekat?"
                            }
                            viewModel.sendChatMessage(selectedPartner, "testwarga", reply, true)
                            delay(100)
                            listState.animateScrollTo(listState.maxValue)
                        }
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN DASHBOARD SETTINGS SCREEN
// ----------------------------------------------------
@Composable
fun AdminSettingsScreen(viewModel: SmartMetaViewModel) {
    val workingHours by viewModel.workingHours.collectAsStateWithLifecycle()
    val pointRate by viewModel.pointPerKg.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val role by viewModel.loggedInRole.collectAsStateWithLifecycle()

    var hoursInput by remember { mutableStateOf(workingHours) }
    var pointsInput by remember { mutableStateOf(pointRate.toString()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = {
                    if (role == "Petugas") {
                        viewModel.navigateTo(Screen.PetugasDashboard)
                    } else {
                        viewModel.navigateTo(Screen.WargaDashboard)
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Dashboard Admin SMARTMETA",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Pusat Pengendali Parameter Kota", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Konfigurasikan nilai operasional yang akan langsung disinkronisasi ke dashboard petugas dan eco-kalkulator warga di seluruh distrik.", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Working Hours configuration (clock-in limits)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Atur Jam Operasional Kerja", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                OutlinedTextField(
                    value = hoursInput,
                    onValueChange = { hoursInput = it },
                    label = { Text("Jam Operasional (misal: 08:00 - 17:00)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (hoursInput.isNotBlank()) {
                            viewModel.setWorkingHours(hoursInput)
                            Toast.makeText(context, "Jam kerja berhasil diubah!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Terapkan Jam Kerja")
                }
            }
        }

        // Point Exchange values
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("2. Atur Nilai Konversi Bank Sampah", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                OutlinedTextField(
                    value = pointsInput,
                    onValueChange = { pointsInput = it },
                    label = { Text("Poin per Kg Sampah Plastik/Logam") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val newRate = pointsInput.toIntOrNull()
                        if (newRate != null && newRate > 0) {
                            viewModel.setPointPerKg(newRate)
                            Toast.makeText(context, "Konversi poin berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Masukkan nilai poin yang valid!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Terapkan Konversi Poin")
                }
            }
        }

        // Live Municipal Activity Log History (Warga & Petugas actions consolidated)
        Text("Log Aktivitas Kota SMARTMETA (Real-Time)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(logs) { log ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("•", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        Text(log, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
