package com.umc.data.repository.community

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.umc.domain.model.community.chatting.*
import com.umc.domain.model.community.thread.CommunityReaction
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.model.community.thread.CommunityThreadSummary
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.repository.community.CommunityChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CommunityChatRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val appDataStoreRepository: AppDataStoreRepository,
    @param:Named("CommunityWebSocketUrl") private val webSocketUrl: String,
) : CommunityChatRepository {
    private val gson = Gson()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connectionState = MutableStateFlow(CommunityChatConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<CommunityChatConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<CommunityChatEvent>(extraBufferCapacity = 64)
    override val events: Flow<CommunityChatEvent> = _events.asSharedFlow()

    private val _errors = MutableSharedFlow<CommunityChatError>(extraBufferCapacity = 16)
    override val errors: Flow<CommunityChatError> = _errors.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var accessToken: String = ""

    override suspend fun connect() {
        if (_connectionState.value != CommunityChatConnectionState.DISCONNECTED) return
        _connectionState.value = CommunityChatConnectionState.CONNECTING
        accessToken = appDataStoreRepository.getAccessToken()
        webSocket = okHttpClient.newWebSocket(
            Request.Builder().url(webSocketUrl).build(),
            StompListener(),
        )
    }

    override fun disconnect() {
        stopHeartbeat()
        webSocket?.send(stompFrame("DISCONNECT"))
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connectionState.value = CommunityChatConnectionState.DISCONNECTED
    }

    override fun createMessage(threadId: String, command: CreateCommunityMessageCommand) =
        send("/app/community/threads/$threadId/messages", command)

    override fun editMessage(threadId: String, messageId: String, content: String) =
        send("/app/community/threads/$threadId/messages/$messageId/edit", mapOf("content" to content))

    override fun deleteMessage(threadId: String, messageId: String) =
        send("/app/community/threads/$threadId/messages/$messageId/delete", emptyMap<String, String>())

    override fun addReaction(threadId: String, messageId: String, emoji: String) =
        send("/app/community/threads/$threadId/messages/$messageId/reactions/add", mapOf("emoji" to emoji))

    override fun removeReaction(threadId: String, messageId: String, emoji: String) =
        send("/app/community/threads/$threadId/messages/$messageId/reactions/remove", mapOf("emoji" to emoji))

    override fun updateRead(threadId: String, lastReadMessageId: String) =
        send("/app/community/threads/$threadId/read", mapOf("lastReadMessageId" to lastReadMessageId))

    private fun send(destination: String, body: Any): String {
        check(_connectionState.value == CommunityChatConnectionState.CONNECTED) {
            "Community chat is not connected"
        }
        val socket = checkNotNull(webSocket) { "Community chat socket is unavailable" }
        val commandId = UUID.randomUUID().toString()
        val enqueued = socket.send(
            stompFrame(
                command = "SEND",
                headers = mapOf(
                    "destination" to destination,
                    "content-type" to "application/json",
                    "x-command-id" to commandId,
                ),
                body = gson.toJson(body),
            )
        )
        check(enqueued) { "Community chat command could not be queued" }
        return commandId
    }

    private inner class StompListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send(
                stompFrame(
                    command = "CONNECT",
                    headers = mapOf(
                        "accept-version" to "1.2",
                        "host" to response.request.url.host,
                        "Authorization" to "Bearer $accessToken",
                        "heart-beat" to "10000,10000",
                    ),
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.split('\u0000').forEach { rawFrame ->
                val frame = parseFrame(rawFrame) ?: return@forEach
                when (frame.command) {
                    "CONNECTED" -> {
                        if (this@CommunityChatRepositoryImpl.webSocket !== webSocket) return@forEach
                        subscribe(webSocket, "community-events", "/user/queue/community/threads/events")
                        subscribe(webSocket, "community-errors", "/user/queue/errors")
                        startHeartbeat(webSocket, frame.headers["heart-beat"])
                        _connectionState.value = CommunityChatConnectionState.CONNECTED
                    }
                    "MESSAGE" -> handleMessage(frame)
                    "ERROR" -> {
                        stopHeartbeat()
                        emitError(frame.body)
                        _connectionState.value = CommunityChatConnectionState.DISCONNECTED
                    }
                }
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (this@CommunityChatRepositoryImpl.webSocket !== webSocket) return
            stopHeartbeat()
            _connectionState.value = CommunityChatConnectionState.DISCONNECTED
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (this@CommunityChatRepositoryImpl.webSocket !== webSocket) return
            stopHeartbeat()
            _errors.tryEmit(CommunityChatError(code = "WS-CONNECTION", message = t.message.orEmpty(), retryable = true))
            _connectionState.value = CommunityChatConnectionState.DISCONNECTED
        }
    }

    private fun startHeartbeat(webSocket: WebSocket, serverHeartbeat: String?) {
        stopHeartbeat()
        val serverIncomingMillis = serverHeartbeat
            ?.substringAfter(',', missingDelimiterValue = "")
            ?.toLongOrNull()
            ?: HEARTBEAT_INTERVAL_MILLIS
        val intervalMillis = maxOf(HEARTBEAT_INTERVAL_MILLIS, serverIncomingMillis)

        heartbeatJob = repositoryScope.launch {
            while (isActive && this@CommunityChatRepositoryImpl.webSocket === webSocket) {
                delay(intervalMillis)
                if (!webSocket.send("\n")) {
                    webSocket.cancel()
                    return@launch
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun subscribe(webSocket: WebSocket, id: String, destination: String) {
        webSocket.send(
            stompFrame(
                command = "SUBSCRIBE",
                headers = mapOf("id" to id, "destination" to destination, "ack" to "auto"),
            )
        )
    }

    private companion object {
        const val HEARTBEAT_INTERVAL_MILLIS = 10_000L
    }

    private fun handleMessage(frame: StompFrame) {
        when (frame.headers["subscription"]) {
            "community-errors" -> emitError(frame.body)
            else -> parseEvent(frame.body)?.let(_events::tryEmit)
        }
    }

    private fun emitError(body: String) {
        val error = runCatching {
            gson.fromJson(body, CommunityChatError::class.java)
        }.getOrNull() ?: CommunityChatError(
            code = "WS-PROTOCOL",
            message = body.ifBlank { "알 수 없는 채팅 오류입니다." },
        )
        _errors.tryEmit(error)
    }

    private fun parseEvent(body: String): CommunityChatEvent? = runCatching {
        val envelope = gson.fromJson(body, JsonObject::class.java)
        val eventId = envelope.string("eventId")
        val type = envelope.string("type")
        val threadId = envelope.string("threadId")
        val occurredAt = envelope.string("occurredAt")
        val payload = envelope.getAsJsonObject("payload") ?: JsonObject()
        when (type) {
            "command.acknowledged" -> CommunityChatEvent.CommandAcknowledged(
                eventId, threadId, occurredAt,
                payload.string("commandId"),
                payload.string("command"),
                payload.nullableString("messageId"),
                payload.nullableString("clientMessageId"),
                payload.get("deduplicated")?.asBoolean ?: false,
            )
            "message.created", "message.updated", "message.deleted" -> CommunityChatEvent.MessageChanged(
                eventId, threadId, occurredAt,
                when (type) {
                    "message.created" -> CommunityChatEvent.MessageChanged.Type.CREATED
                    "message.updated" -> CommunityChatEvent.MessageChanged.Type.UPDATED
                    else -> CommunityChatEvent.MessageChanged.Type.DELETED
                },
                gson.fromJson(payload.get("message"), CommunityThreadMessage::class.java),
                payload.nullableString("clientMessageId"),
            )
            "reaction.changed" -> CommunityChatEvent.ReactionChanged(
                eventId, threadId, occurredAt,
                payload.string("messageId"),
                payload.getAsJsonArray("reactions")?.map {
                    gson.fromJson(it, CommunityReaction::class.java)
                }.orEmpty(),
            )
            "read.updated" -> CommunityChatEvent.ReadUpdated(
                eventId, threadId, occurredAt,
                payload.string("memberId"),
                payload.string("lastReadMessageId"),
            )
            "thread.invited" -> CommunityChatEvent.ThreadInvited(
                eventId, threadId, occurredAt,
                gson.fromJson(payload.get("thread"), CommunityThreadSummary::class.java),
            )
            "thread.updated", "thread.deleted", "member.kicked", "member.left" ->
                CommunityChatEvent.ThreadStateChanged(
                    eventId, threadId, occurredAt,
                    when (type) {
                        "thread.updated" -> CommunityChatEvent.ThreadStateChanged.Type.UPDATED
                        "thread.deleted" -> CommunityChatEvent.ThreadStateChanged.Type.DELETED
                        "member.kicked" -> CommunityChatEvent.ThreadStateChanged.Type.MEMBER_KICKED
                        else -> CommunityChatEvent.ThreadStateChanged.Type.MEMBER_LEFT
                    },
                    payload.nullableString("memberId"),
                    payload.nullableString("memberCount"),
                )
            else -> null
        }
    }.getOrNull()

    private data class StompFrame(val command: String, val headers: Map<String, String>, val body: String)

    private fun parseFrame(raw: String): StompFrame? {
        val normalized = raw.trimStart('\n', '\r')
        if (normalized.isBlank()) return null
        val separator = normalized.indexOf("\n\n")
        val head = if (separator >= 0) normalized.substring(0, separator) else normalized
        val lines = head.lines()
        val command = lines.firstOrNull()?.trim().orEmpty()
        if (command.isEmpty()) return null
        val headers = lines.drop(1).mapNotNull {
            val index = it.indexOf(':')
            if (index <= 0) null else it.substring(0, index) to it.substring(index + 1)
        }.toMap()
        return StompFrame(command, headers, if (separator >= 0) normalized.substring(separator + 2) else "")
    }

    private fun stompFrame(
        command: String,
        headers: Map<String, String> = emptyMap(),
        body: String = "",
    ) = buildString {
        append(command).append('\n')
        headers.forEach { (key, value) -> append(key).append(':').append(value).append('\n') }
        append('\n').append(body).append('\u0000')
    }

    private fun JsonObject.string(name: String) = get(name)?.takeUnless { it.isJsonNull }?.asString.orEmpty()
    private fun JsonObject.nullableString(name: String) = get(name)?.takeUnless { it.isJsonNull }?.asString
}
