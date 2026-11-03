package com.ttwdualscreen.app

import com.ttwdualscreen.app.net.SpikeClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The plugin answers `hello` with ttw:false before DeferredInit, then pushes an unsolicited
 * `welcome` with ttw:true. The client must replace the first welcome with the second.
 */
class SpikeClientWelcomeTest {
    @Test fun unsolicitedWelcomeReplacesEarlierOne() {
        val c = SpikeClient()  // never started: no socket, frames fed directly
        val early = Fixtures.load("welcome.json").replace("\"ttw\":true", "\"ttw\":false")
        c.handle(early)
        assertNotNull(c.ui.value.welcome)
        assertFalse(c.ui.value.welcome!!.ttw)

        c.handle(Fixtures.load("welcome.json"))
        assertTrue(c.ui.value.welcome!!.ttw)
        assertEquals("0.0.1-spike", c.ui.value.welcome!!.plugin)
        assertTrue(c.log.value.last().endsWith("ttw=true"))
        c.stop()
    }
}
