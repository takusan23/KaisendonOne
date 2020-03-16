package io.github.kaisendonone.StreamingAPI

import io.github.kaisendonone.API.AccessToken
import io.github.kaisendonone.JSON.StreamingJSON
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

/**
 * ストリーミングAPI
 * */
class TimelineStreaming(val token: AccessToken) {

    private val baseUrl = "wss://${token.instance}/api/v1/streaming/"

    lateinit var localWebSocketClient: WebSocketClient
    lateinit var userWebSocketClient: WebSocketClient

    /**
     * リアルタイムローカルTL
     * @param onMessage メッセージを受け取ったら
     * */
    fun streamingLocal(onMessage: (StreamingJSON?) -> Unit) {
        localWebSocketClient =
            streaming("${baseUrl}?stream=public:local&access_token=${token.token}", "local", onMessage)
    }

    /**
     * リアルタイムでホーム＋通知
     * @param onMessage メッセージを受け取ったら
     * */
    fun streamingUser(onMessage: (StreamingJSON?) -> Unit) {
        userWebSocketClient =
            streaming("${baseUrl}?stream=user&access_token=${token.token}", "home", onMessage)
    }

    /**
     * WebSocket
     * @param onMessage メッセージを受け取ったら
     * */
    private fun streaming(uri: String, timeline: String, onMessage: (StreamingJSON?) -> Unit): WebSocketClient {
        val webSocketClient = object : WebSocketClient(URI(uri)) {
            override fun onOpen(handshakedata: ServerHandshake?) {

            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {

            }

            override fun onMessage(message: String?) {
                onMessage(StreamingJSON(message, timeline, token.instance))
            }

            override fun onError(ex: Exception?) {

            }
        }
        webSocketClient.connect() // 接続
        return webSocketClient
    }

    fun destroy() {
        if (::userWebSocketClient.isInitialized) {
            userWebSocketClient.close()
        }
        if (::localWebSocketClient.isInitialized) {
            localWebSocketClient.close()
        }
    }

}