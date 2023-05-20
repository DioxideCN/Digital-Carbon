package org.sangonomiya.app.extension.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 这个是WebSocket通信单元，在对象内存储了session会话和awaitUsername信息。
 * 在onOpen后会以发起建立请求的用户的username为主键将session以单元对象的形式
 * 存入concurrentHashMap中保留。
 * <p>
 * 客户端通知服务端：服务端会验证socket连接信息并响应
 * <p>
 * 服务端主动通知客户端：客户端setInterval持续接受消息
 *
 * @author Dioxide.CN
 * @date 2023/3/19 23:10
 * @since 1.0
 */
@Slf4j
@Component
@SuppressWarnings("all")
@ServerEndpoint("/websocket/{username}/{token}")
public class WebSocketUnit {

    private static JwtUnit jwtUnit;

    // 调用JUC下的ConcurrentHashMap以<username, session>存储所有有效会话
    private static final ConcurrentHashMap<String, WebSocketUnit> webSocketMap = new ConcurrentHashMap<>();
    // 原子计数考虑线程安全
    private static final AtomicInteger onlineCount = new AtomicInteger();

    private Session session; // 每个客户端信道的会话对象
    private String awaitUsername; // 用户标识符

    // 将该多对象WebSocket专门委托给spring bean容器进行循环注入
    @Autowired
    public void setJwtUnit(JwtUnit jwtUnit) {
        WebSocketUnit.jwtUnit = jwtUnit;
    }

    /**
     * 连接建立成功调用的方法
     * @param session 会话
     * @param username 连接方
     * @param token 签名
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("token") String token) {
        if (username == null || token == null) return;
        awaitUsername = jwtUnit.getUserNameFromToken(token);
        if (!username.equals(awaitUsername)) return;

        this.session = session;
        webSocketMap.put(awaitUsername, this); // 将会话保留到map中
        addOnlineCount(); // 在线数加1
        log.info("{} started a new session {}", awaitUsername, getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketMap.remove(this.awaitUsername); // 从map中移除

        subOnlineCount(); // 在线数减1
        log.info("{} closed session {}", this.awaitUsername, getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     * @param session 会话
     * @param message 客户端消息
     * @param username 发起人
     * @param token 签名
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("username") String username, @PathParam("token") String token) {
        if (username == null || token == null) return;
        if (!username.equals(jwtUnit.getUserNameFromToken(token))) return;

        log.info("receive client message from {} {}", username, message);
        // 回复此客户端
        this.notifyClient(session, message);
    }

    /**
     * websocket会话异常
     * @param session 会话
     * @param error 异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("unexpected error occurred");
        error.printStackTrace();
    }

    // 广播消息
    public static void broadcast(){
        webSocketMap.forEach((username, unit) -> {
            unit.notifyClient("测试广播: " + username + " 你好");
        });
    }

    /**
     * 主动通知指定Session的客户端
     * @param session
     * @param message
     */
    public void notifyClient(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主动通知该对象的客户端
     * @param message 消息
     */
    public void notifyClient(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 主动同步通知username的客户端
     * @param username 用户
     * @param message 消息
     */
    public static synchronized void notifyClient(String username, String message) {
        WebSocketUnit targetSession = webSocketMap.get(username);
        if (targetSession == null)
            return;
        targetSession.notifyClient(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount.get();
    }

    public static synchronized void addOnlineCount() {
        onlineCount.getAndIncrement();
    }

    public static synchronized void subOnlineCount() {
        onlineCount.getAndDecrement();
    }

}
