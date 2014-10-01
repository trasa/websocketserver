package com.meancat.websocket.server;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MonitorWebSocketServlet extends WebSocketServlet {
    @Autowired
    private MonitorConnectionCreator monitorConnectionCreator;

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(monitorConnectionCreator);
    }
}
