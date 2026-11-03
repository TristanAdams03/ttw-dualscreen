package com.ttwdualscreen.app

import com.ttwdualscreen.app.net.ClientMsg
import com.ttwdualscreen.app.net.ServerMsg
import com.ttwdualscreen.app.net.encodeClient
import com.ttwdualscreen.app.net.parseServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolTest {
    private fun fx(name: String) = Fixtures.load(name)
    private fun json(text: String): JsonElement = Json.parseToJsonElement(text)

    @Test fun welcomeFixtureParses() {
        val m = parseServer(fx("welcome.json"))
        assertTrue(m is ServerMsg.Welcome)
        m as ServerMsg.Welcome
        assertEquals("0.0.1-spike", m.plugin)
        assertEquals(0, m.protocol)
        assertEquals("FalloutNV 1.4.0.525", m.game)
        assertEquals("6.4.9", m.xnvse)
        assertTrue(m.ttw)
        assertEquals(listOf("spike"), m.features)
    }

    @Test fun stateFixtureParses() {
        val m = parseServer(fx("state_spike.json"))
        assertTrue(m is ServerMsg.State)
        m as ServerMsg.State
        assertEquals("spike", m.topic)
        val d = m.data
        assertEquals(12345L, d.frame)
        assertEquals(205750L, d.ms)
        assertTrue(d.saveLoaded)
        assertEquals(245.0, d.hp!!, 0.0)
        assertEquals(280.0, d.hpMax!!, 0.0)
        assertEquals(false, d.hardcore)
        assertEquals(1, d.bInDCWasteland)
        assertEquals(0, d.bInNVWasteland)
        assertEquals("Wasteland", d.worldspace)
        assertEquals("Megaton", d.cell)
        assertEquals(false, d.interior)
        assertEquals(listOf(255, 182, 66), d.colors!!.hudMain)
        assertEquals(listOf(33, 231, 121), d.colors!!.terminal)
    }

    @Test fun stateWithNullsParses() {
        val text = """{"type":"state","topic":"spike","data":{"frame":7,"ms":100,"saveLoaded":false,
            "hp":null,"hpMax":null,"hardcore":null,"bInDCWasteland":null,"bInNVWasteland":null,
            "worldspace":null,"cell":null,"interior":null,"colors":null}}"""
        val m = parseServer(text)
        assertTrue(m is ServerMsg.State)
        val d = (m as ServerMsg.State).data
        assertEquals(7L, d.frame)
        assertFalse(d.saveLoaded)
        assertNull(d.hp); assertNull(d.hpMax); assertNull(d.hardcore)
        assertNull(d.bInDCWasteland); assertNull(d.bInNVWasteland)
        assertNull(d.worldspace); assertNull(d.cell); assertNull(d.interior); assertNull(d.colors)
    }

    @Test fun resultFixturesParse() {
        val ok = parseServer(fx("result_ok.json"))
        assertTrue(ok is ServerMsg.Result)
        ok as ServerMsg.Result
        assertEquals("c1", ok.id)
        assertTrue(ok.ok)
        assertNull(ok.error)
        assertEquals("Lincoln's Repeater", ok.data!!["name"]!!.jsonPrimitive.content)

        val noSave = parseServer(fx("result_no_save.json"))
        assertTrue(noSave is ServerMsg.Result)
        noSave as ServerMsg.Result
        assertFalse(noSave.ok)
        assertEquals("no save loaded", noSave.error)
        assertNull(noSave.data)
    }

    @Test fun pongFixtureParses() {
        val m = parseServer(fx("pong.json"))
        assertEquals(ServerMsg.Pong(5), m)
    }

    @Test fun errorFixtureParses() {
        val m = parseServer(fx("error.json"))
        assertEquals(ServerMsg.Error("invalid json"), m)
    }

    @Test fun unknownTypeIsUnknown() {
        val m = parseServer("""{"type":"bogus","x":1}""")
        assertTrue(m is ServerMsg.Unknown)
        assertEquals("bogus", (m as ServerMsg.Unknown).type)
    }

    @Test fun malformedIsUnknown() {
        assertTrue(parseServer("not json") is ServerMsg.Unknown)
        assertTrue(parseServer("[1,2]") is ServerMsg.Unknown)
        assertTrue(parseServer("""{"type":"pong"}""") is ServerMsg.Unknown)
    }

    @Test fun clientMessagesMatchFixtures() {
        assertEquals(json(fx("hello.json")), json(encodeClient(ClientMsg.Hello("ttwds-app", 0))))
        assertEquals(json(fx("subscribe.json")), json(encodeClient(ClientMsg.Subscribe(listOf("spike"), 5))))
        assertEquals(json(fx("ping.json")), json(encodeClient(ClientMsg.Ping(5))))
        assertEquals(json(fx("command_equip.json")),
            json(encodeClient(ClientMsg.Command("c1", "spike.equipFirstWeapon"))))
    }
}
