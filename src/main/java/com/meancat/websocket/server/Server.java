package com.meancat.websocket.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.File;

public class Server {
    private static class ApplicationArguments {
        @Option(required = true, name = "--home", aliases = { "-h" }, usage = "Home directory of application.")
        public String home;
    }

    private static AnnotationConfigWebApplicationContext applicationContext;

    /**
     * Starts the service.
     *
     * @param args
     *            args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ApplicationArguments appArgs = new ApplicationArguments();

        CmdLineParser parser = new CmdLineParser(appArgs);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println();
            parser.printUsage(System.err);
            return;
        }

        // set system variables
        System.setProperty("app.home", appArgs.home);

        // setup logging
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(new File(SystemPropertyUtils.resolvePlaceholders("${app.home}/conf/logback.xml")));
        } catch (JoranException je) {
            // ignore since StatusPrinter will handle this
        }

        // fix JUL logging
        java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger("");
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < handlers.length; i++) {
            rootLogger.removeHandler(handlers[i]);
        }
        SLF4JBridgeHandler.install();

        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        System.out.println("Starting " + Server.getDetails());

        // Debugging Logging
        /*
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        // print logback's internal status
        StatusPrinter.print(lc);
        */

        // start spring
        applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(ServerConfiguration.class);
        applicationContext.refresh();

        applicationContext.registerShutdownHook();

        System.out.println("Started " + Server.getDetails());

        // wait for app to exit
        while (applicationContext.isActive()) {
            Thread.sleep(5000);
        }

        // exit application
        System.out.println("Stopped " + Server.getDetails());
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Gets details about the application.
     *
     * @return details
     */
    public static String getDetails() {
        return Server.class.getSimpleName() + " - version: " + getVersion();
    }

    public static String getVersion() {
        String version = Server.class.getPackage().getImplementationVersion();
        if (version == null)
            version = "(unpackaged deployment)";
        return version;
    }
}
