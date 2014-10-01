package com.meancat.websocket.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;


@Configuration
@ComponentScan(basePackageClasses = {ServerConfiguration.class})
public class ServerConfiguration {

    @Value("${http.bindIp:}")
    public String bindIp;

    @Value("${http.bindPort:8080}")
    public int bindPort;

    @Value("${http.contentPath:file:${app.home}/web}")
    public Resource contentPath;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer(Environment environment) throws Exception {

        ArrayList<Resource> resources = new ArrayList<>();

        // local config.properties
        resources.add(new DefaultResourceLoader().getResource(SystemPropertyUtils.resolvePlaceholders("file:${app.home}/conf/config.properties")));

        PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
        bean.setEnvironment(environment);
        bean.setLocations(resources.toArray(new Resource[resources.size()]));
        bean.setNullValue("@null");
        return bean;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server webServer(ApplicationContext applicationContext, WebAppContext context) {
        InetSocketAddress addr;
        if (bindIp == null || bindIp.equals("")) {
            addr = new InetSocketAddress(bindPort);
        } else {
            addr = new InetSocketAddress(bindIp, bindPort);
        }
        Server bean = new Server(addr);

        // is this being called from the app starting up or from a unit test?
        if (!(applicationContext instanceof AbstractRefreshableWebApplicationContext)) {
            return null; // do nothing.
        }
        AbstractRefreshableWebApplicationContext ctx = (AbstractRefreshableWebApplicationContext)applicationContext;

        bean.setHandler(context);

        ctx.setServletContext(context.getServletContext());

        return bean;
    }

    @Bean
    public WebAppContext webAppContext(ApplicationContext applicationContext,
                                       MonitorWebSocketServlet monitorWebSocketServlet
                                       ) throws IOException {
        WebAppContext context = new WebAppContext(contentPath.getFile().getAbsolutePath(), "/");
        context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);

        // websocket that our clients connect to:
        context.addServlet(new ServletHolder(monitorWebSocketServlet), "/ws/*");
        return context;
    }
}
