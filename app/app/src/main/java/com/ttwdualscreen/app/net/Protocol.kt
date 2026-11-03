package com.ttwdualscreen.app.net

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Protocol v0 (protocol/PROTOCOL.md). */
const val PROTOCOL_VERSION = 0

val protocolJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = true
    encodeDefaults = true
}

@Serializable
data class Colors(
    val hudMain: List<Int>,
    val hudAlt: List<Int>,
    val terminal: List<Int>,
    val pipboy: List<Int>,
    val menu: List<Int>,
) {
    fun named(): List<Pair<String, List<Int>>> =
        listOf("hudMain" to hudMain, "hudAlt" to hudAlt, "terminal" to terminal, "pipboy" to pipboy, "menu" to menu)
}

@Serializable
data class SpikeData(
    val frame: Long,
    val ms: Long,
    val saveLoaded: Boolean,
    val hp: Double? = null,
    val hpMax: Double? = null,
    val hardcore: Boolean? = null,
    val bInDCWasteland: Int? = null,
    val bInNVWasteland: Int? = null,
    val worldspace: String? = null,
    val cell: String? = null,
    val interior: Boolean? = null,
    val colors: Colors? = null,
)

/** Server -> client messages. */
sealed interface ServerMsg {
    @Serializable
    data class Welcome(
        val plugin: String,
        val protocol: Int,
        val game: String,
        val xnvse: String,
        val ttw: Boolean,
        val features: List<String> = emptyList(),
    ) : ServerMsg

    @Serializable
    data class State(val topic: String, val data: SpikeData) : ServerMsg

    @Serializable
    data class Result(
        val id: String,
        val ok: Boolean,
        val error: String? = null,
        val data: JsonObject? = null,
    ) : ServerMsg

    @Serializable
    data class Pong(val t: Long) : ServerMsg

    @Serializable
    data class Error(val message: String) : ServerMsg

    /** Unknown `type`, or a frame that is not a valid protocol object. */
    data class Unknown(val type: String?, val raw: String) : ServerMsg
}

/** Parse one server text frame. Never throws: anything unrecognised becomes [ServerMsg.Unknown]. */
fun parseServer(text: String): ServerMsg {
    val obj = try {
        protocolJson.parseToJsonElement(text) as? JsonObject
    } catch (e: SerializationException) {
        null
    } ?: return ServerMsg.Unknown(null, text)
    val type = try {
        obj["type"]?.jsonPrimitive?.contentOrNull
    } catch (e: IllegalArgumentException) {
        null
    }
    return try {
        when (type) {
            "welcome" -> protocolJson.decodeFromJsonElement(ServerMsg.Welcome.serializer(), obj)
            "state" -> protocolJson.decodeFromJsonElement(ServerMsg.State.serializer(), obj)
            "result" -> protocolJson.decodeFromJsonElement(ServerMsg.Result.serializer(), obj)
            "pong" -> protocolJson.decodeFromJsonElement(ServerMsg.Pong.serializer(), obj)
            "error" -> protocolJson.decodeFromJsonElement(ServerMsg.Error.serializer(), obj)
            else -> ServerMsg.Unknown(type, text)
        }
    } catch (e: SerializationException) {
        ServerMsg.Unknown(type, text)
    } catch (e: IllegalArgumentException) {
        ServerMsg.Unknown(type, text)
    }
}

/** Client -> server messages. */
@OptIn(ExperimentalSerializationApi::class)
sealed interface ClientMsg {
    @Serializable
    data class Hello(val client: String, val protocol: Int = PROTOCOL_VERSION) : ClientMsg {
        @EncodeDefault @SerialName("type") val type: String = "hello"
    }

    @Serializable
    data class Subscribe(val topics: List<String>, val rateHz: Int) : ClientMsg {
        @EncodeDefault @SerialName("type") val type: String = "subscribe"
    }

    @Serializable
    data class Command(val id: String, val name: String, val args: JsonObject = JsonObject(emptyMap())) : ClientMsg {
        @EncodeDefault @SerialName("type") val type: String = "command"
    }

    @Serializable
    data class Ping(val t: Long) : ClientMsg {
        @EncodeDefault @SerialName("type") val type: String = "ping"
    }
}

fun encodeClient(msg: ClientMsg): String = when (msg) {
    is ClientMsg.Hello -> protocolJson.encodeToString(ClientMsg.Hello.serializer(), msg)
    is ClientMsg.Subscribe -> protocolJson.encodeToString(ClientMsg.Subscribe.serializer(), msg)
    is ClientMsg.Command -> protocolJson.encodeToString(ClientMsg.Command.serializer(), msg)
    is ClientMsg.Ping -> protocolJson.encodeToString(ClientMsg.Ping.serializer(), msg)
}
