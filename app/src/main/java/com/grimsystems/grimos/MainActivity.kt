package com.grimsystems.grimos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF07100D)
private val Panel = Color(0xFF12211B)
private val Emerald = Color(0xFF39E69A)
private val Muted = Color(0xFF9BB5A8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GrimHome() }
    }
}

private data class Tool(val title: String, val subtitle: String, val icon: ImageVector)
@Composable
fun GrimHome() {
    var selected by remember { mutableStateOf("Home") }
    var showInfo by remember { mutableStateOf(false) }
    val tools = listOf(
        Tool("Settings", "System preferences", Icons.Default.Settings),
        Tool("Files", "Open your files", Icons.Default.Folder),
        Tool("Device", "Device information", Icons.Default.PhoneAndroid),
        Tool("Privacy", "Security overview", Icons.Default.Security),
        Tool("Display", "Screen & appearance", Icons.Default.Palette),
        Tool("About", "GRIM SYSTEMS", Icons.Default.Info)
    )
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    MaterialTheme(colorScheme = darkColorScheme(primary = Emerald, background = Ink, surface = Panel)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF10251B), Ink, Color(0xFF050807))))) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("GRIM SYSTEMS", color = Emerald, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        Text("GRIM OS", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    }
                    Surface(color = Color(0x3339E69A), shape = RoundedCornerShape(50)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(7.dp).background(Emerald, RoundedCornerShape(50)))
                            Spacer(Modifier.width(7.dp))
                            Text("SYSTEM READY", color = Emerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(42.dp))
                Text(time, color = Color.White, fontSize = 66.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                Text(date, color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(28.dp))
                Surface(color = Color(0xCC15271F), shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("YOUR DEVICE. REIMAGINED.", color = Emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("A cleaner Android\nexperience starts here.", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Explore your tools and make your phone feel like yours.", color = Muted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(25.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("QUICK ACCESS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("6 MODULES", color = Muted, fontSize = 9.sp)
                }
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tools) { tool ->
                        Surface(color = Panel.copy(alpha = .92f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().clickable { selected = tool.title; showInfo = true }) {
                            Column(Modifier.padding(16.dp)) {
                                Icon(tool.icon, null, tint = Emerald, modifier = Modifier.size(25.dp))
                                Spacer(Modifier.height(16.dp))
                                Text(tool.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(tool.subtitle, color = Muted, fontSize = 10.sp)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Home" to Icons.Default.Home, "Apps" to Icons.Default.Apps, "Profile" to Icons.Default.Person).forEach { (label, icon) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selected = label }) {
                            Icon(icon, null, tint = if (selected == label) Emerald else Muted)
                            Text(label, color = if (selected == label) Emerald else Muted, fontSize = 10.sp)
                        }
                    }
                }
            }
            if (showInfo) AlertDialog(onDismissRequest = { showInfo = false }, containerColor = Panel, title = { Text(selected, color = Color.White) }, text = { Text("This module is part of the GRIM OS foundation. Some device controls require Android permissions or a future module update.", color = Muted) }, confirmButton = { TextButton(onClick = { showInfo = false }) { Text("GOT IT", color = Emerald) } })
        }
    }
}
