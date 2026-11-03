package com.ttwdualscreen.app

import com.ttwdualscreen.app.display.DisplayInfo
import com.ttwdualscreen.app.net.ServerMsg
import com.ttwdualscreen.app.net.parseServer
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportTest {
    private val displays = listOf(
        DisplayInfo(0, 1920, 1080, isOn = true, isPrivate = false),
        DisplayInfo(4, 1240, 1080, isOn = true, isPrivate = false),
    )

    private fun lines(r: String) = r.lines()

    @Test fun reportHasRequiredLines() {
        val ui = SpikeUi(
            connected = true,
            welcome = parseServer(Fixtures.load("welcome.json")) as ServerMsg.Welcome,
            state = (parseServer(Fixtures.load("state_spike.json")) as ServerMsg.State).data,
            loop = LoopStatus.Alive,
            fps = 59.9,
        )
        val r = buildReport(ui, listOf("12:00:00 connected"), "AYN Thor Android 13", displays)
        for (key in listOf("plugin=", "loop=", "fps=", "bInDCWasteland=", "displays=")) {
            assertTrue("missing $key in:\n$r", lines(r).any { it.startsWith(key) })
        }
        assertTrue(r, r.contains("plugin=0.0.1-spike"))
        assertTrue(r, r.contains("loop=ALIVE"))
        assertTrue(r, r.contains("bInDCWasteland=1"))
        assertTrue(r, r.contains("device=AYN Thor Android 13"))
        assertTrue(r, r.contains("4:1240x1080"))
        assertTrue(r, r.contains("12:00:00 connected"))
    }

    @Test fun reportWorksDisconnected() {
        val r = buildReport(SpikeUi(), emptyList(), "x", emptyList())
        for (key in listOf("plugin=", "loop=", "fps=", "bInDCWasteland=", "displays=")) {
            assertTrue("missing $key in:\n$r", lines(r).any { it.startsWith(key) })
        }
        assertTrue(r, r.contains("loop=SOCKET DEAD"))
    }
}
