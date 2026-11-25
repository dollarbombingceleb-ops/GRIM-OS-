package com.grimsystems.grimos

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Sky = Color(0xFF8FC9F8)
private val Glass = Color(0xBFFFFFFF)
private val Ink = Color(0xFF17202A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { IOSHome() }
    }
}

private data class PhoneApp(val name: String, val packageName: String, val icon: android.graphics.drawable.Drawable?)

@Composable
private fun IOSHome() {
    val context = LocalContext.current
    var controlOpen by remember { mutableStateOf(false) }
    var appDrawer by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf(emptyList<PhoneApp>()) }
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { PhoneApp(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }
    val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    val time = SimpleDateFormat("h:mm", Locale.getDefault()).format(Date())

    MaterialTheme(colorScheme = lightColorScheme(primary = Sky, background = Color(0xFFEAF2F9))) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF9EC9E8), Color(0xFFE7EDF3), Color(0xFFD5E1EC))))) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(time, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SignalCellularAlt, null, tint = Ink, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.Wifi, null, tint = Ink, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Icon(Icons.Default.BatteryFull, null, tint = Ink, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(30.dp))
                Text(date, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(time, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Surface(color = Glass, shape = RoundedCornerShape(22.dp), modifier = Modifier.clickable { controlOpen = true }) {
                        Row(Modifier.padding(horizontal = 15.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, null, tint = Ink)
                            Spacer(Modifier.width(8.dp))
                            Text("Control Centre", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                if (appDrawer) {
                    Surface(color = Color(0xDDF8FBFF), shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().weight(1.4f)) {
                        LazyVerticalGrid(columns = GridCells.Fixed(4), contentPadding = PaddingValues(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(apps) { app ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                                    val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (launch != null) context.startActivity(launch)
                                }) {
                                    Icon(Icons.Default.Apps, null, tint = Color(0xFF526A80), modifier = Modifier.size(34.dp))
                                    Text(app.name, color = Ink, fontSize = 10.sp, maxLines = 2)
                                }
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        HomeIcon(Icons.Default.Phone, "Phone") { openApp(context, apps, listOf("phone", "dialer")) }
                        HomeIcon(Icons.Default.Message, "Messages") { openApp(context, apps, listOf("message", "sms")) }
                        HomeIcon(Icons.Default.CameraAlt, "Camera") { openApp(context, apps, listOf("camera")) }
                        HomeIcon(Icons.Default.Photo, "Photos") { openApp(context, apps, listOf("photo", "gallery")) }
                    }
                    Spacer(Modifier.height(18.dp))
                    Surface(color = Color(0xBFFFFFFF), shape = RoundedCornerShape(30.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(vertical = 13.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            HomeIcon(Icons.Default.PhoneAndroid, "Apps") { appDrawer = true }
                            HomeIcon(Icons.Default.MusicNote, "Apple Music") { openAppleApp(context, "com.apple.android.music") }
                            HomeIcon(Icons.Default.Settings, "Settings") { openApp(context, apps, listOf("settings")) }
                            HomeIcon(Icons.Default.Folder, "Files") { openApp(context, apps, listOf("file")) }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Swipe up · App Library", color = Color(0xCCFFFFFF), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).clickable { appDrawer = !appDrawer }.padding(bottom = 8.dp))
            }
            if (controlOpen) {
                Box(Modifier.fillMaxSize().background(Color(0x990A1725)).clickable { controlOpen = false }, contentAlignment = Alignment.TopEnd) {
                    Surface(color = Color(0xF2EAF2F9), shape = RoundedCornerShape(30.dp), modifier = Modifier.fillMaxWidth().padding(12.dp).statusBarsPadding().clickable(enabled = false) {}) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Control Centre", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ControlTile("Airplane", Icons.Default.Flight, Modifier.weight(1f))
                                ControlTile("Wi-Fi", Icons.Default.Wifi, Modifier.weight(1f))
                                ControlTile("Bluetooth", Icons.Default.Bluetooth, Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ControlTile("Focus", Icons.Default.DoNotDisturbOn, Modifier.weight(1f))
                                ControlTile("Rotation", Icons.Default.ScreenRotation, Modifier.weight(1f))
                                ControlTile("Flashlight", Icons.Default.FlashlightOn, Modifier.weight(1f))
                            }
                            Text("Brightness", color = Ink, fontWeight = FontWeight.SemiBold)
                            var brightness by remember { mutableFloatStateOf(.7f) }
                            Slider(value = brightness, onValueChange = { brightness = it })
                            Text("Volume", color = Ink, fontWeight = FontWeight.SemiBold)
                            var volume by remember { mutableFloatStateOf(.55f) }
                            Slider(value = volume, onValueChange = { volume = it })
                            Text("These are interface controls in this preview. Device-level actions require Android APIs and permissions.", color = Color.DarkGray, fontSize = 11.sp)
                            TextButton(onClick = { controlOpen = false }) { Text("Done") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Surface(color = Color(0xEFFFFFFF), shape = RoundedCornerShape(17.dp), modifier = Modifier.size(53.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color(0xFF273746), modifier = Modifier.size(26.dp)) }
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ControlTile(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(color = Color(0xFFCFDFEC), shape = RoundedCornerShape(18.dp), modifier = modifier.height(74.dp)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = Ink)
            Text(label, color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun openAppleApp(context: android.content.Context, packageName: String) {
    val launch = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launch != null) { context.startActivity(launch); return }
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName))
    try { context.startActivity(market) }
    catch (_: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName))) }
}

private fun openApp(context: android.content.Context, apps: List<PhoneApp>, terms: List<String>) {
    val app = apps.firstOrNull { a -> terms.any { a.name.contains(it, true) || a.packageName.contains(it, true) } } ?: return
    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { context.startActivity(it) }
}
