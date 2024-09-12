package cn.iecas.simulate.assessment.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



/**
 * WebSocket服务类
 */
@Component
@Slf4j
@ServerEndpoint("/webSocket/{userId}")
@CrossOrigin
public class WebSocketServer {

    private static int onlineCount = 0;

    private static final ConcurrentMap<Integer, WebSocketServer> webSocketMap = new ConcurrentHashMap();

    private int userId;

    private Session session;

    private JSONObject socketInfo;


    /**
     * 与前端成功建立连接后的回调函数
     * @param session
     * @param userId
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") int userId) {
        this.userId = userId;
        //session.setMaxIdleTimeout(1800);
        this.session = session;
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
        } else {
            webSocketMap.put(userId, this);
            WebSocketServer.onlineCount++;
        }
        log.info("用户 {} 连接成功，当前系统在线人数：{} ", userId, WebSocketServer.onlineCount);
    }


    /**
     * 断凯WebSocket连接
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            WebSocketServer.onlineCount--;
            log.info("用户 {} 已断开WebSocket连接，当前系统在线人数：{} ", userId, WebSocketServer.onlineCount);
        } else {
            log.info("不包含用户 {} 的WebSocket信息 ", userId);
        }
    }


    /**
     * 接收到客户端信息，然后广播
     */
    @OnMessage
    public void onMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            log.info("接收到信息：{} ，开始广播", message);
            Set<Map.Entry<Integer, WebSocketServer>> entries = webSocketMap.entrySet();
            for (Map.Entry<Integer, WebSocketServer> entry : entries) {
                this.sendMessage("send to " + entry.getValue().userId + ":" + message, entry.getValue().userId);
            }
        }
    }


    /**
     * WebSocket出错
     * @param e
     */
    @OnError
    public void onError(Throwable e) {
        log.info("用户 {} 出错，原因：{} ", userId, e.getMessage());
    }


    /**
     * 推送消息
     * @param message
     * @param userId
     */
    public void sendMessage(String message, int userId) {
        try {
            if (webSocketMap.containsKey(userId)) {
                webSocketMap.get(userId).session.getBasicRemote().sendText(message);
                log.info("成功向用户 {} 推送信息 {} ", userId, message);
            } else {
                throw new RuntimeException("用户 {} 的WebSocket连接不存在");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public static void main(String[] args) {
        WebSocketServer webSocketServer = new WebSocketServer();
        webSocketServer.sendMessage("", 10);
    }
}
