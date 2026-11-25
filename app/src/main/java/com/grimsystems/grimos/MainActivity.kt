package com.grimsystems.grimos

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF07131B)
private val Mint = Color(0xFF8FFFE0)
private val WhiteGlass = Color(0x24FFFFFF)
private val SoftWhite = Color(0xFFEAF5F3)

private data class PhoneApp(val name: String, val packageName: String, val icon: android.graphics.drawable.Drawable?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { GrimExperience() }
    }
}

@Composable
private fun GrimExperience() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(emptyList<PhoneApp>()) }
    var screen by remember { mutableStateOf("home") }
    var query by remember { mutableStateOf("") }
    var tick by remember { mutableIntStateOf(0) }
    var wallpaper by remember { mutableIntStateOf(0) }
    var iconScale by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(.72f) }
    var volume by remember { mutableFloatStateOf(.55f) }
    var showQuick by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var page by remember { mutableIntStateOf(0) }

    val wallpapers = listOf(
        listOf(Color(0xFF07131B), Color(0xFF123B3B), Color(0xFF286B62)),
        listOf(Color(0xFF090D20), Color(0xFF202B57), Color(0xFF5266A8)),
        listOf(Color(0xFF160D22), Color(0xFF4A245B), Color(0xFF9A527C)),
        listOf(Color(0xFF101820), Color(0xFF344A5E), Color(0xFF8AA6B7))
    )
    val bg = wallpapers[wallpaper]
    LaunchedEffect(Unit) {
        while (true) { tick++; delay(1000) }
    }
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        apps = pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
            .map { PhoneApp(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }
    val time = remember(tick) { SimpleDateFormat("h:mm", Locale.getDefault()).format(Date()) }
    val date = remember(tick) { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }
    val filtered = remember(apps, query) { apps.filter { it.name.contains(query, true) || it.packageName.contains(query, true) } }

    MaterialTheme(colorScheme = darkColorScheme(primary = Mint, background = Night, surface = Color(0xFF12242B), onSurface = SoftWhite)) {
        Box(
            Modifier.fillMaxSize()
                .background(Brush.linearGradient(bg))
                .pointerInput(screen, showQuick) {
                    var total = 0f
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dy -> total += dy },
                        onDragEnd = {
                            if (total < -65f && screen == "home") screen = "library"
                            if (total > 65f && screen == "home") showQuick = true
                            if (total > 65f && screen == "library") screen = "home"
                            total = 0f
                        }
                    )
                }
                .pointerInput(screen) {
                    var total = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dx -> total += dx },
                        onDragEnd = { if (screen == "home" && kotlin.math.abs(total) > 70f) page = if (total < 0) 1 else 0; total = 0f }
                    )
                }
        ) {
            when (screen) {
                "library" -> AppLibrary(filtered, query, { query = it }, { launchApp(context, it) }, { screen = "home" }, { showSearch = true })
                "settings" -> SettingsPage(wallpaper, { wallpaper = it }, iconScale, { iconScale = it }, { screen = "home" })
                "lock" -> LockPreview(time, date, { screen = "home" })
                else -> HomePage(
                    time, date, apps, page, iconScale,
                    onLaunch = { launchApp(context, it) },
                    onLibrary = { screen = "library" },
                    onSettings = { screen = "settings" },
                    onLock = { screen = "lock" },
                    onQuick = { showQuick = true },
                    onSearch = { screen = "library"; showSearch = true },
                    onPage = { page = it }
                )
            }
            if (showQuick) {
                QuickPanel(
                    brightness, { brightness = it; setWindowBrightness(context, it) },
                    volume, { volume = it; setDeviceVolume(context, it) },
                    onSettings = { showQuick = false; openSystemSettings(context, Settings.ACTION_SETTINGS) },
                    onWifi = { openSystemSettings(context, Settings.ACTION_WIFI_SETTINGS) },
                    onBluetooth = { openSystemSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS) },
                    onNotifications = { openSystemSettings(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) },
                    onDismiss = { showQuick = false }
                )
            }
            if (showSearch) {
                LaunchedEffect(Unit) { showSearch = false }
            }
        }
    }
}

@Composable
private fun HomePage(
    time: String, date: String, apps: List<PhoneApp>, page: Int, iconScale: Float,
    onLaunch: (PhoneApp) -> Unit, onLibrary: () -> Unit, onSettings: () -> Unit,
    onLock: () -> Unit, onQuick: () -> Unit, onSearch: () -> Unit, onPage: (Int) -> Unit
) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("GRIM SYSTEMS", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wifi, null, tint = SoftWhite, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.BatteryFull, null, tint = SoftWhite, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Default.Tune, "Control Centre", tint = SoftWhite, modifier = Modifier.size(20.dp).clickable { onQuick() })
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(date, color = SoftWhite.copy(alpha = .78f), fontSize = 14.sp)
                Text(time, color = Color.White, fontSize = 68.sp, fontWeight = FontWeight.Light, letterSpacing = (-3).sp)
                Text("Your world, beautifully connected.", color = Mint.copy(alpha = .9f), fontSize = 11.sp)
            }
            Surface(color = WhiteGlass, shape = RoundedCornerShape(24.dp), modifier = Modifier.size(74.dp).clickable { onLock() }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Lock, null, tint = Mint, modifier = Modifier.size(23.dp))
                    Text("LOCK", color = SoftWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            GlassWidget(Modifier.weight(1.15f), "QUICK ACCESS", "Find anything", Icons.Default.Search, onSearch)
            GlassWidget(Modifier.weight(.85f), "APP LIBRARY", "${apps.size} apps", Icons.Default.Apps, onLibrary)
        }
        Spacer(Modifier.height(18.dp))
        Text(if (page == 0) "Favourites" else "Everyday", color = SoftWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(12.dp))
        val candidates = if (page == 0) listOf("WhatsApp", "Phone", "Camera", "Photos", "Chrome", "Messages", "Settings", "Gmail") else listOf("Music", "YouTube", "Maps", "Files", "Play Store", "Calendar", "Clock", "Contacts")
        val chosen = candidates.mapNotNull { term -> apps.firstOrNull { it.name.contains(term, true) || it.packageName.contains(term, true) } }.distinctBy { it.packageName }
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(chosen) { app -> AppTile(app, iconScale, { onLaunch(app) }) }
            item { SystemTile(Icons.Default.Apps, "All apps", iconScale, onLibrary) }
            item { SystemTile(Icons.Default.Settings, "GRIM Settings", iconScale, onClick = onSettings) }
            item { SystemTile(Icons.Default.Widgets, "Widgets", iconScale) { onSettings() } }
            item { SystemTile(Icons.Default.Lock, "Lock preview", iconScale, onLock) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            repeat(2) { index ->
                Box(Modifier.padding(5.dp).size(if (page == index) 8.dp else 6.dp).clip(CircleShape).background(if (page == index) Mint else Color.White.copy(alpha = .35f)).clickable { onPage(index) })
            }
        }
        Spacer(Modifier.height(10.dp))
        Surface(color = Color(0x42FFFFFF), shape = RoundedCornerShape(30.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                DockButton(Icons.Default.Phone, "Phone") { openByWords(context, apps, listOf("phone", "dialer")) }
                DockButton(Icons.Default.Message, "Messages") { openByWords(context, apps, listOf("message", "sms")) }
                DockButton(Icons.Default.MusicNote, "Music") { openByWords(context, apps, listOf("music")) }
                DockButton(Icons.Default.Apps, "Library", onLibrary)
                DockButton(Icons.Default.Settings, "Settings", onSettings)
            }
        }
        Spacer(Modifier.height(7.dp))
        Text("Swipe up for App Library  •  Swipe down for Control Centre", color = SoftWhite.copy(alpha = .65f), fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun GlassWidget(modifier: Modifier, eyebrow: String, title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(modifier = modifier.height(86.dp).clickable { onClick() }, color = WhiteGlass, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = Mint, modifier = Modifier.size(20.dp))
            Column {
                Text(eyebrow, color = SoftWhite.copy(alpha = .6f), fontSize = 8.sp, letterSpacing = 1.sp)
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AppTile(app: PhoneApp, scale: Float, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Surface(color = Color(0x28FFFFFF), shape = RoundedCornerShape(19.dp), modifier = Modifier.size((56 * scale).dp)) {
            Box(contentAlignment = Alignment.Center) { AppIcon(app.icon, Modifier.size((43 * scale).dp)) }
        }
        Spacer(Modifier.height(5.dp))
        Text(app.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun SystemTile(icon: ImageVector, label: String, scale: Float, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Surface(color = Color(0x38FFFFFF), shape = RoundedCornerShape(19.dp), modifier = Modifier.size((56 * scale).dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Mint, modifier = Modifier.size(27.dp)) }
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = Color.White, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun DockButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
        Text(label, color = SoftWhite.copy(alpha = .8f), fontSize = 8.sp)
    }
}

@Composable
private fun AppLibrary(apps: List<PhoneApp>, query: String, onQuery: (String) -> Unit, onLaunch: (PhoneApp) -> Unit, onBack: () -> Unit, onSearch: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("App Library", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(18.dp))
        Surface(color = Color(0x35FFFFFF), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = Mint)
                Spacer(Modifier.width(10.dp))
                BasicTextField(value = query, onValueChange = onQuery, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 15.sp), decorationBox = { inner ->
                    Box { if (query.isEmpty()) Text("Search apps", color = SoftWhite.copy(alpha = .55f)); inner() }
                }, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("${apps.size} INSTALLED APPS", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(4), contentPadding = PaddingValues(bottom = 28.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items(apps) { AppTile(it, 1f) { onLaunch(it) } }
        }
    }
}

@Composable
private fun SettingsPage(wallpaper: Int, onWallpaper: (Int) -> Unit, scale: Float, onScale: (Float) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("GRIM Settings", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))
        Text("PERSONALISATION", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(12.dp))
        Text("Choose your atmosphere", color = Color.White, fontSize = 16.sp)
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(Color(0xFF123B3B), Color(0xFF202B57), Color(0xFF4A245B), Color(0xFF344A5E)).forEachIndexed { index, color ->
                Box(Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(color, Night))).clickable { onWallpaper(index) }) {
                    if (wallpaper == index) Icon(Icons.Default.CheckCircle, null, tint = Mint, modifier = Modifier.align(Alignment.BottomEnd).padding(7.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Icon size", color = Color.White, fontSize = 16.sp)
        Slider(value = scale, onValueChange = onScale, valueRange = .8f..1.2f)
        Text("Home screen icon size: ${(scale * 100).toInt()}%", color = SoftWhite.copy(alpha = .7f), fontSize = 12.sp)
        Spacer(Modifier.height(24.dp))
        Text("DEVICE & PRIVACY", color = Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        SettingsRow(Icons.Default.Notifications, "Notification access", "Review Android notification access settings") { openSystemSettings(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
        SettingsRow(Icons.Default.Lock, "Lock screen", "Open Android lock screen settings") { openSystemSettings(context, Settings.ACTION_DISPLAY_SETTINGS) }
        SettingsRow(Icons.Default.Settings, "Android system settings", "Manage device-level settings") { openSystemSettings(context, Settings.ACTION_SETTINGS) }
        SettingsRow(Icons.Default.Info, "About GRIM OS", "GRIM SYSTEMS • Android launcher") { }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Mint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = SoftWhite.copy(alpha = .6f), fontSize = 10.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = SoftWhite.copy(alpha = .55f))
    }
    HorizontalDivider(color = Color.White.copy(alpha = .1f))
}

@Composable
private fun LockPreview(time: String, date: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1C28), Color(0xFF1C5550), Color(0xFF07131B)))).clickable { onBack() }, contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.statusBarsPadding().padding(top = 95.dp)) {
            Text(date, color = SoftWhite, fontSize = 16.sp)
            Text(time, color = Color.White, fontSize = 86.sp, fontWeight = FontWeight.ExtraLight, letterSpacing = (-4).sp)
            Spacer(Modifier.height(24.dp))
            Surface(color = Color(0x30FFFFFF), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 25.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("GRIM OS LOCK SCREEN PREVIEW", color = Mint, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Your notifications will appear here when notification access is enabled.", color = Color.White, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("Tap anywhere to return", color = SoftWhite.copy(alpha = .65f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun QuickPanel(brightness: Float, onBrightness: (Float) -> Unit, volume: Float, onVolume: (Float) -> Unit, onSettings: () -> Unit, onWifi: () -> Unit, onBluetooth: () -> Unit, onNotifications: () -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xCC02090E)).clickable { onDismiss() }, contentAlignment = Alignment.TopCenter) {
        Surface(color = Color(0xF21A3038), shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp), modifier = Modifier.fillMaxWidth().clickable(enabled = false) {}) {
            Column(Modifier.statusBarsPadding().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Control Centre", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.clickable { onDismiss() })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickTile(Icons.Default.Wifi, "Wi-Fi settings", onWifi, Modifier.weight(1f))
                    QuickTile(Icons.Default.Bluetooth, "Bluetooth", onBluetooth, Modifier.weight(1f))
                    QuickTile(Icons.Default.Settings, "Settings", onSettings, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickTile(Icons.Default.Notifications, "Notifications", onNotifications, Modifier.weight(1f))
                    QuickTile(Icons.Default.Brightness6, "Display", onSettings, Modifier.weight(1f))
                    QuickTile(Icons.Default.VolumeUp, "Sound", onSettings, Modifier.weight(1f))
                }
                Text("App window brightness", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Slider(value = brightness, onValueChange = onBrightness, valueRange = .05f..1f)
                Text("Device media volume", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Slider(value = volume, onValueChange = onVolume, valueRange = 0f..1f)
                Text("Wi-Fi, Bluetooth and protected system controls open Android settings. Brightness adjusts GRIM OS while it is on screen.", color = SoftWhite.copy(alpha = .65f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun QuickTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(color = Color(0x35FFFFFF), shape = RoundedCornerShape(18.dp), modifier = modifier.height(76.dp).clickable { onClick() }) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = Mint, modifier = Modifier.size(22.dp))
            Text(label, color = Color.White, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun AppIcon(drawable: android.graphics.drawable.Drawable?, modifier: Modifier = Modifier) {
    val bitmap = remember(drawable) {
        if (drawable == null) null else {
            val bmp = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, 96, 96)
            drawable.draw(canvas)
            bmp.asImageBitmap()
        }
    }
    if (bitmap != null) Image(bitmap, contentDescription = null, modifier = modifier)
    else Icon(Icons.Default.Apps, null, tint = Mint, modifier = modifier)
}

private fun launchApp(context: Context, app: PhoneApp) {
    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { context.startActivity(it) }
}

private fun openByWords(context: Context, apps: List<PhoneApp>, words: List<String>) {
    val found = apps.firstOrNull { app -> words.any { app.name.contains(it, true) || app.packageName.contains(it, true) } }
    if (found != null) launchApp(context, found)
    else openSystemSettings(context, Settings.ACTION_SETTINGS)
}

private fun openSystemSettings(context: Context, action: String) {
    try { context.startActivity(Intent(action)) }
    catch (_: Exception) { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
}

private fun setWindowBrightness(context: Context, value: Float) {
    val activity = context as? ComponentActivity ?: return
    val params = activity.window.attributes
    params.screenBrightness = value.coerceIn(.01f, 1f)
    activity.window.attributes = params
}

private fun setDeviceVolume(context: Context, value: Float) {
    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    audio.setStreamVolume(AudioManager.STREAM_MUSIC, (max * value).toInt(), 0)
}
