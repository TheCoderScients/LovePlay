package com.loveplay

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlin.math.min
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LovePlayApp()
        }
    }
}

@Composable
fun LovePlayApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("loveplay", Context.MODE_PRIVATE) }

    var leftName by rememberSaveable { mutableStateOf("Aku") }
    var rightName by rememberSaveable { mutableStateOf("Kamu") }
    var accentIndex by rememberSaveable { mutableStateOf(0) } // default: pink

    // Load from SharedPreferences once
    LaunchedEffect(Unit) {
        leftName = prefs.getString("leftName", leftName) ?: leftName
        rightName = prefs.getString("rightName", rightName) ?: rightName
        accentIndex = prefs.getInt("accentIndex", accentIndex)
    }

    LovePlayTheme(accentIndex = accentIndex) {
        val nav = rememberNavController()
        val scheme = MaterialTheme.colorScheme
        val bgGradient = remember(scheme.primary, scheme.secondary) {
            Brush.verticalGradient(
                colors = listOf(
                    scheme.primary.copy(alpha = 0.15f),
                    scheme.secondary.copy(alpha = 0.08f),
                    Color.Transparent
                )
            )
        }
        Box(modifier = Modifier.fillMaxSize().background(bgGradient)) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { LoveBottomBar(nav) }
            ) { inner ->
                Box(modifier = Modifier.padding(inner).fillMaxSize()) {
                    NavHost(
                        navController = nav,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                        leftName = leftName,
                        rightName = rightName,
                        onGoWheel = { nav.navigate(Screen.Wheel.route) },
                        onGoTruthDare = { nav.navigate(Screen.Truth.route) },
                        onGoCounter = { nav.navigate(Screen.Counter.route) }
                    )
                }
                composable(Screen.Wheel.route) { WheelScreen() }
                composable(Screen.Truth.route) { TruthOrDareScreen() }
                composable(Screen.Counter.route) {
                    LoveCounterScreen(
                        leftName = leftName,
                        rightName = rightName
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        leftName = leftName,
                        rightName = rightName,
                        accentIndex = accentIndex,
                        onSave = { ln, rn, ai ->
                            leftName = ln
                            rightName = rn
                            accentIndex = ai
                            prefs.edit()
                                .putString("leftName", ln)
                                .putString("rightName", rn)
                                .putInt("accentIndex", ai)
                                .apply()
                        }
                    )
                }
            }
        }
    }
}

private sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Home : Screen("home", "Beranda", { Icon(Icons.Default.Home, contentDescription = null) })
    object Wheel : Screen("wheel", "Wheel", { Icon(Icons.Default.Sync, contentDescription = null) })
    object Truth : Screen("truth", "Truth/Dare", { Icon(Icons.Default.QuestionAnswer, contentDescription = null) })
    object Counter : Screen("counter", "Counter", { Icon(Icons.Default.Favorite, contentDescription = null) })
    object Settings : Screen("settings", "Pengaturan", { Icon(Icons.Default.Settings, contentDescription = null) })
}

@Composable
private fun LoveBottomBar(nav: NavHostController) {
    val items = listOf(Screen.Home, Screen.Wheel, Screen.Truth, Screen.Counter, Screen.Settings)
    val current by nav.currentBackStackEntryAsState()
    val currentRoute = current?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    nav.navigate(item.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = item.icon,
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    leftName: String,
    rightName: String,
    onGoWheel: () -> Unit,
    onGoTruthDare: () -> Unit,
    onGoCounter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header romantis
        Text(
            text = "LovePlay ❤",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Hai $leftName ❤ $rightName!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Pilih permainan untuk memulai",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedButton(
                onClick = onGoWheel,
                modifier = Modifier.weight(1f)
            ) { Text("Spin Wheel") }
            ElevatedButton(
                onClick = onGoTruthDare,
                modifier = Modifier.weight(1f)
            ) { Text("Truth or Dare") }
        }
        ElevatedButton(
            onClick = onGoCounter,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Love Counter") }

        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Tips Seru:", fontWeight = FontWeight.Bold)
                Text("- Gunakan Settings untuk ubah nama dan warna tema.")
                Text("- Coba Spin Wheel untuk ide kencan spontan.")
                Text("- Truth or Dare untuk tantangan santai.")
            }
        }

        // Dorong kredit ke bagian bawah layar
        Spacer(Modifier.weight(1f))

        CreditChip(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun WheelScreen() {
    val ideas = remember {
        listOf(
            "Nonton film romantis",
            "Masak bareng",
            "Piknik sederhana",
            "Jalan sore sambil foto-foto",
            "Main board game",
            "Ngopi di kafe baru",
            "Baca buku berdua",
            "Olahraga ringan",
            "Belajar resep baru",
            "Video call sambil makan"
        )
    }

    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val rotation = remember { Animatable(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Spin the Wheel Ide Kencan", style = MaterialTheme.typography.titleMedium)

        Box(
            modifier = Modifier
                .size(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Simple visual spinner: rotating pointer line
            Canvas(modifier = Modifier.fillMaxSize().padding(24.dp).rotate(rotation.value)) {
                val w = size.width
                val h = size.height
                val r = min(w, h) / 2f
                // pointer line
                drawLine(
                    color = MaterialTheme.colorScheme.primary,
                    start = androidx.compose.ui.geometry.Offset(w/2f, h/2f),
                    end = androidx.compose.ui.geometry.Offset(w/2f, h/2f - r + 8f),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            }
            Text("🎡", fontSize = 40.sp)
        }

        ElevatedButton(onClick = {
            val turns = Random.nextInt(6, 12)
            val end = rotation.value + 360f * turns
            selected = ideas.random()
            LaunchedEffect("spin") {
                rotation.animateTo(
                    end,
                    animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing)
                )
                rotation.snapTo(rotation.value % 360f)
            }
        }) {
            Text("Putar Roda")
        }

        selected?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(12.dp))
        Text("Ide lainnya:", fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.fillMaxWidth()) {
            items(ideas) { idea ->
                Text("• $idea", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun TruthOrDareScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("loveplay", Context.MODE_PRIVATE) }

    val truthsBuiltin = remember {
        listOf(
            "Apa hal kecil yang bikin kamu bahagia?",
            "Kapan pertama kali kamu merasa sangat dicintai?",
            "Sifatku yang paling kamu suka?",
            "Apa rahasia kecil yang belum kamu ceritakan?",
            "Momen paling lucu bareng aku?"
        )
    }
    val daresBuiltin = remember {
        listOf(
            "Kirim voice note bilang 'sayang' dengan gaya lucu",
            "Tiru gaya fotoku favoritmu",
            "Peluk aku 30 detik",
            "Buat pantun romantis spontan",
            "Nyanyikan lagu cinta 10 detik"
        )
    }

    val customTruths = remember { mutableStateListOf<String>() }
    val customDares = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        prefs.getStringSet("customTruths", emptySet())?.let { set ->
            customTruths.clear()
            customTruths.addAll(set.filter { it.isNotBlank() })
        }
        prefs.getStringSet("customDares", emptySet())?.let { set ->
            customDares.clear()
            customDares.addAll(set.filter { it.isNotBlank() })
        }
    }

    var modeTruth by rememberSaveable { mutableStateOf(true) }
    var current by rememberSaveable { mutableStateOf<String?>(null) }
    var input by rememberSaveable { mutableStateOf("") }

    fun saveCustoms() {
        prefs.edit()
            .putStringSet("customTruths", customTruths.toSet())
            .putStringSet("customDares", customDares.toSet())
            .apply()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Truth or Dare", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = modeTruth,
                onClick = { modeTruth = true },
                label = { Text("Truth") }
            )
            FilterChip(
                selected = !modeTruth,
                onClick = { modeTruth = false },
                label = { Text("Dare") }
            )
        }

        // Input buatan pengguna
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(if (modeTruth) "Tulis Truth kamu" else "Tulis Dare kamu") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                val text = input.trim()
                if (text.isNotEmpty()) {
                    if (modeTruth) {
                        if (!customTruths.contains(text)) customTruths.add(text)
                    } else {
                        if (!customDares.contains(text)) customDares.add(text)
                    }
                    saveCustoms()
                    input = ""
                }
            }) {
                Text("Tambah")
            }
        }

        ElevatedButton(onClick = {
            val pool = if (modeTruth) truthsBuiltin + customTruths else daresBuiltin + customDares
            if (pool.isNotEmpty()) {
                current = pool.random()
            } else {
                current = "Belum ada tantangan. Tambahkan dulu ya!"
            }
        }) { Text("Ambil Tantangan") }

        current?.let {
            Card(shape = RoundedCornerShape(16.dp)) {
                Text(
                    it,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tampilkan daftar
        Spacer(Modifier.height(8.dp))
        Text("Koleksi Kamu:", fontWeight = FontWeight.Bold)
        val customList = if (modeTruth) customTruths else customDares
        if (customList.isEmpty()) {
            Text("Belum ada. Tambahkan lewat kolom di atas.")
        } else {
            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 8.dp)) {
                items(customList) { item ->
                    Text("• $item", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        Text("Ide lainnya:", fontWeight = FontWeight.Bold)
        val builtin = if (modeTruth) truthsBuiltin else daresBuiltin
        LazyColumn(Modifier.fillMaxWidth()) {
            items(builtin) { idea ->
                Text("• $idea", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun LoveCounterScreen(leftName: String, rightName: String) {
    var left by rememberSaveable { mutableStateOf(0) }
    var right by rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Love Counter", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CounterCard(name = leftName, count = left, onTap = { left++ })
            CounterCard(name = rightName, count = right, onTap = { right++ })
        }
        OutlinedButton(onClick = { left = 0; right = 0 }) { Text("Reset") }
    }
}

@Composable
private fun CounterCard(name: String, count: Int, onTap: () -> Unit) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onTap() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Text(name, fontWeight = FontWeight.Bold)
            Text("$count", style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun SettingsScreen(
    leftName: String,
    rightName: String,
    accentIndex: Int,
    onSave: (String, String, Int) -> Unit
) {
    var ln by rememberSaveable { mutableStateOf(leftName) }
    var rn by rememberSaveable { mutableStateOf(rightName) }
    var ai by rememberSaveable { mutableStateOf(accentIndex) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pengaturan", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = ln,
            onValueChange = { ln = it },
            label = { Text("Nama Kiri") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = rn,
            onValueChange = { rn = it },
            label = { Text("Nama Kanan") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Warna Aksen")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AccentPalette.forEachIndexed { index, color ->
                val selected = ai == index
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, CircleShape)
                        .border(width = if (selected) 3.dp else 1.dp, color = if (selected) Color.Black else Color.Gray, shape = CircleShape)
                        .clickable { ai = index }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onSave(ln, rn, ai) }, modifier = Modifier.fillMaxWidth()) { Text("Simpan") }
    }
}

@Composable
private fun CreditChip(modifier: Modifier = Modifier) {
    val gradient = Brush.horizontalGradient(
        listOf(
            Color(0xFFFF8FAB), // soft pink
            Color(0xFFE91E63), // pink
            Color(0xFF9C27B0)  // purple
        )
    )
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(vertical = 12.dp, horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Dibuat oleh M. Samjaya",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
            }
        }
    }
}

private val AccentPalette = listOf(
    Color(0xFFE91E63), // pink
    Color(0xFFF06292), // pink light
    Color(0xFF9C27B0), // purple
    Color(0xFF03A9F4), // blue
    Color(0xFFFF9800), // orange
    Color(0xFF4CAF50)  // green
)

@Composable
private fun LovePlayTheme(accentIndex: Int, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val primary = AccentPalette.getOrElse(accentIndex) { AccentPalette.first() }
    val scheme = if (dark) darkColorScheme(primary = primary, secondary = primary) else lightColorScheme(primary = primary, secondary = primary)
    MaterialTheme(colorScheme = scheme, content = content)
}
