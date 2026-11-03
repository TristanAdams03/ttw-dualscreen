package com.ttwdualscreen.app.net

import com.ttwdualscreen.app.FpsMeter
import com.ttwdualscreen.app.LoopStatus
import com.ttwdualscreen.app.SpikeUi
import com.ttwdualscreen.app.StallDetector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

const val DEFAULT_URL = "ws://127.0.0.1:7960"

/**
 * Spike WebSocket client: hello -> subscribe{spike, 5}; ping every 1 s; reconnects forever with
 * backoff 0.5 s doubling to a 3 s cap. Plain JVM (no Android types) so it runs in JVM tests.
 *
 * The request carries only OkHttp's defaults: `Host: 127.0.0.1:7960` and no `Origin`
 * (the plugin rejects any Origin header, or a Host other than 127.0.0.1/localhost).
 */
class SpikeClient(
    private val url: String = DEFAULT_URL,
    private val clock: () -> Long = System::currentTimeMillis,
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stall = StallDetector()
    private val fps = FpsMeter()
    private val nextId = AtomicInteger(0)
    private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT)

    private val _ui = MutableStateFlow(SpikeUi())
    val ui: StateFlow<SpikeUi> = _ui.asStateFlow()

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log.asStateFlow()

    @Volatile private var ws: WebSocket? = null
    private var job: Job? = null

    fun log(line: String) {
        val stamped = "${synchronized(timeFmt) { timeFmt.format(Date(clock())) }} $line"
        _log.update { (it + stamped).takeLast(LOG_LINES) }
    }

    fun start() {
        if (job != null) return
        job = scope.launch { connectLoop() }
        scope.launch { pingLoop() }
        scope.launch { statusLoop() }
    }

    fun stop() {
        ws?.close(1000, "bye")
        ws = null
        scope.cancel()
    }

    /** Sends a `command` (ids c1, c2, ...). Returns the id, or null when not connected. */
    fun command(name: String): String? {
        val socket = ws ?: run { log("command $name: not connected"); return null }
        val id = "c${nextId.incrementAndGet()}"
        socket.send(encodeClient(ClientMsg.Command(id, name)))
        log("-> $id $name")
        return id
    }

    private suspend fun connectLoop() {
        var backoff = BACKOFF_START_MS
        while (scope.isActive) {
            val closed = CompletableDeferred<Unit>()
            var opened = false
            log("connecting $url")
            val request = Request.Builder().url(url).build()
            val socket = http.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    opened = true
                    val now = clock()
                    stall.reset(now)
                    fps.reset()
                    ws = webSocket
                    webSocket.send(encodeClient(ClientMsg.Hello(CLIENT_NAME)))
                    webSocket.send(encodeClient(ClientMsg.Subscribe(listOf("spike"), RATE_HZ)))
                    _ui.update { it.copy(connected = true) }
                    log("connected")
                    applyLoop(stall.status(now))
                }

                override fun onMessage(webSocket: WebSocket, text: String) = handle(text)

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(1000, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    log("closed $code $reason")
                    closed.complete(Unit)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    log("failure: ${t.javaClass.simpleName}: ${t.message}" +
                        (response?.let { " (HTTP ${it.code})" } ?: ""))
                    closed.complete(Unit)
                }
            })
            closed.await()
            if (ws === socket) ws = null
            _ui.update { it.copy(connected = false, fps = null) }
            applyLoop(LoopStatus.SocketDead)
            if (opened) backoff = BACKOFF_START_MS
            delay(backoff)
            backoff = (backoff * 2).coerceAtMost(BACKOFF_CAP_MS)
        }
    }

    private fun handle(text: String) {
        val now = clock()
        when (val m = parseServer(text)) {
            is ServerMsg.Welcome -> {
                _ui.update { it.copy(welcome = m) }
                log("welcome plugin=${m.plugin} game=${m.game} xnvse=${m.xnvse} ttw=${m.ttw}")
            }
            is ServerMsg.State -> {
                stall.onState(now)
                val f = fps.onFrame(m.data.frame, now)
                _ui.update { it.copy(state = m.data, fps = f ?: it.fps) }
                applyLoop(stall.status(now))
            }
            is ServerMsg.Pong -> {
                stall.onPong(now)
                _ui.update { it.copy(rttMs = (now - m.t).takeIf { d -> d >= 0 }) }
                applyLoop(stall.status(now))
            }
            is ServerMsg.Result -> {
                val line = "${m.id} " + (if (m.ok) "ok ${m.data ?: ""}" else "FAIL ${m.error}")
                _ui.update { it.copy(lastResult = line) }
                log("<- $line")
            }
            is ServerMsg.Error -> log("<- error: ${m.message}")
            is ServerMsg.Unknown -> log("<- unknown ${m.type}: ${m.raw.take(120)}")
        }
    }

    private val loopLock = Any()

    /** Publishes the loop status; logs each transition once. Not alive -> fps reads 0. */
    private fun applyLoop(status: LoopStatus) {
        val prev = synchronized(loopLock) {
            val p = _ui.value.loop
            _ui.update { it.copy(loop = status, fps = if (status == LoopStatus.Alive) it.fps else 0.0) }
            p
        }
        if (prev != status) log("loop ${prev.label} -> ${status.label}")
    }

    private suspend fun pingLoop() {
        while (scope.isActive) {
            ws?.send(encodeClient(ClientMsg.Ping(clock())))
            delay(PING_MS)
        }
    }

    /** Status depends on elapsed time, so re-evaluate it even when nothing arrives. */
    private suspend fun statusLoop() {
        while (scope.isActive) {
            delay(STATUS_TICK_MS)
            val socket = ws ?: continue
            val status = stall.status(clock())
            applyLoop(status)
            if (status == LoopStatus.SocketDead) {
                log("no pong for ${stall.socketDeadMs} ms, reconnecting")
                ws = null
                socket.cancel()
            }
        }
    }

    companion object {
        const val CLIENT_NAME = "ttwds-app"
        const val RATE_HZ = 5
        const val PING_MS = 1000L
        const val STATUS_TICK_MS = 250L
        const val BACKOFF_START_MS = 500L
        const val BACKOFF_CAP_MS = 3000L
        const val LOG_LINES = 50
    }
}
