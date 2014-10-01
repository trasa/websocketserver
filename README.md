websocketserver
===============

Boilerplate Jetty Websocket Implementation with Client xhtml page
(Because I hate having to go back and re-learn this every time I start a new project)

MonitorWebSocketServlet
=======================
Spring Component that allows us to override with our own ConnectionCreator.

MonitorConnectionCreator
========================
Used to create websockets. Specifically, our Spring-powered MonitorConnection object, which is why this is ApplicationContextAware.

MonitorConnection
=================
The heart and soul of the client connection, where we read and write from the client. There are a bunch of org.eclipse.jetty.websocket.api.annotations available here, I'm only implementing a handful.

In this case, the MonitorConnection just echoes back what it gets from the client, nothing interesting.

index.xhtml
===========
A simple WebSocket Client that connects, disconnects, and sends and receives text.
