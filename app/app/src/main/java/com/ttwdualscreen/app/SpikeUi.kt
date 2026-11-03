package com.ttwdualscreen.app

import com.ttwdualscreen.app.net.ServerMsg
import com.ttwdualscreen.app.net.SpikeData

/** Everything the spike screen shows. */
data class SpikeUi(
    val connected: Boolean = false,
    val welcome: ServerMsg.Welcome? = null,
    val state: SpikeData? = null,
    val loop: LoopStatus = LoopStatus.SocketDead,
    val fps: Double? = null,
    val rttMs: Long? = null,
    val lastResult: String? = null,
)
