package lavalink.server.io

import lavalink.server.config.ServerConfig
import lavalink.server.logging.ConsoleLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Controller
class HandshakeInterceptorImpl @Autowired
constructor(private val serverConfig: ServerConfig, private val socketServer: SocketServer) : HandshakeInterceptor {

    /**
     * Checks credentials and sets the Lavalink version header
     *
     * @return true if authenticated
     */
    override fun beforeHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler,
                                 attributes: Map<String, Any>): Boolean {
        val password = request.headers.getFirst("Authorization")
        val matches = password == serverConfig.password

        if (matches) {
            ConsoleLogging.LogUpdate("Incoming connection from " + request.remoteAddress)
        } else {
            ConsoleLogging.LogError("Authentication failed from " + request.remoteAddress)
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
        }

        val resumeKey = request.headers.getFirst("Resume-Key")
        val resuming = resumeKey != null && socketServer.canResume(resumeKey)
        response.headers.add("Session-Resumed", resuming.toString())

        return matches
    }

    // No action required
    override fun afterHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler,
                                exception: Exception?) {
    }
}
