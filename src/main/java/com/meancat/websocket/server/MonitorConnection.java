package com.meancat.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@WebSocket
public class MonitorConnection {
    private static final Logger logger = LoggerFactory.getLogger(MonitorConnection.class);

    Session session;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        logger.info("connected! {}", session);
    }


    @OnWebSocketClose
    public void onClose(Session session, int code, String data) throws Exception {
        logger.info("closed! {} {}", code, data);
    }


    @OnWebSocketMessage
    public void onText(String data) throws Exception {
        logger.info("Text received: {}", data);
        session.getRemote().sendString("echo:" + data);
    }
}
