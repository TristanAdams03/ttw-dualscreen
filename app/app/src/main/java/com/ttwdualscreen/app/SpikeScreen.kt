package com.ttwdualscreen.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

val Amber = Color(0xFFFFB642)
private val Mono = TextStyle(fontFamily = FontFamily.Monospace, color = Amber, fontSize = 14.sp)

@Composable
fun SpikeScreen(
    ui: SpikeUi,
    log: List<String>,
    focusOn: Boolean,
    onEquip: () -> Unit,
    onDumpWorld: () -> Unit,
    onToggleFocus: () -> Unit,
    onCopyReport: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(Color.Black).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LoopBanner(ui)
        val w = ui.welcome
        Line(if (w == null) "plugin: -" else "plugin ${w.plugin} p${w.protocol} | ${w.game} | xNVSE ${w.xnvse} | ttw=${w.ttw}")
        val s = ui.state
        Line("HP ${s?.hp?.let { "%.0f".format(Locale.ROOT, it) } ?: "-"} / ${s?.hpMax?.let { "%.0f".format(Locale.ROOT, it) } ?: "-"}" +
            "   hardcore=${s?.hardcore ?: "-"}   save=${s?.saveLoaded ?: "-"}")
        Line("bInDCWasteland=${s?.bInDCWasteland ?: "-"}  bInNVWasteland=${s?.bInNVWasteland ?: "-"}")
        Line("${s?.worldspace ?: "-"} / ${s?.cell ?: "-"}${if (s?.interior == true) " (interior)" else ""}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val colors = s?.colors?.named()
            if (colors == null) Line("colors: -")
            colors?.forEach { (name, c) -> Swatch(name, c) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AmberButton("Equip first weapon", onEquip)
            AmberButton("Dump world", onDumpWorld)
            AmberButton("Focus mode ${if (focusOn) "on" else "off"}", onToggleFocus)
            AmberButton("Copy report", onCopyReport)
        }
        Line("last result: ${ui.lastResult ?: "-"}")
        LogPane(log, Modifier.weight(1f))
    }
}

@Composable
private fun LoopBanner(ui: SpikeUi) {
    val (fg, bg) = when (ui.loop) {
        LoopStatus.Alive -> Color.Black to Amber
        LoopStatus.Stalled -> Amber to Color(0xFF5A2A00)
        LoopStatus.SocketDead -> Amber to Color(0xFF400000)
    }
    val fps = ui.fps?.let { "%.1f fps".format(Locale.ROOT, it) } ?: "- fps"
    val rtt = ui.rttMs?.let { "  rtt ${it}ms" } ?: ""
    Box(Modifier.fillMaxWidth().background(bg).padding(vertical = 10.dp, horizontal = 12.dp)) {
        Text(
            "LOOP: ${ui.loop.label}   $fps$rtt",
            style = Mono.copy(color = fg, fontSize = 28.sp, fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun Line(text: String) = Text(text, style = Mono)

@Composable
private fun Swatch(name: String, rgb: List<Int>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val c = Color(rgb.getOrElse(0) { 0 }, rgb.getOrElse(1) { 0 }, rgb.getOrElse(2) { 0 })
        Box(Modifier.size(40.dp).background(c).border(1.dp, Amber))
        Text(name, style = Mono.copy(fontSize = 10.sp))
    }
}

@Composable
private fun AmberButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Amber),
        modifier = Modifier.border(1.dp, Amber),
    ) { Text(label, style = Mono) }
}

@Composable
private fun LogPane(log: List<String>, modifier: Modifier) {
    val state = rememberLazyListState()
    LaunchedEffect(log.size, log.lastOrNull()) { if (log.isNotEmpty()) state.scrollToItem(log.size - 1) }
    Column(modifier.fillMaxWidth().border(1.dp, Amber).padding(6.dp)) {
        Text("log (last ${log.size})", style = Mono.copy(fontSize = 11.sp))
        Spacer(Modifier.height(4.dp))
        LazyColumn(state = state) {
            items(log) { Text(it, style = Mono.copy(fontSize = 11.sp)) }
        }
    }
}
