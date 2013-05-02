/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.bootstrap;

import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.publisher.AbstractStringDumpingEntryPublisher;
import org.sonatype.appcontext.publisher.SystemPropertiesEntryPublisher;
import org.sonatype.appcontext.source.PropertiesEntrySource;
import org.sonatype.appcontext.source.StaticEntrySource;
import com.sanxing.sesame.bootstrap.monitor.CommandMonitorThread;
import com.sanxing.sesame.bootstrap.monitor.KeepAliveThread;
import com.sanxing.sesame.bootstrap.monitor.commands.ExitCommand;
import com.sanxing.sesame.bootstrap.monitor.commands.HaltCommand;
import com.sanxing.sesame.bootstrap.monitor.commands.PingCommand;
import com.sanxing.sesame.bootstrap.monitor.commands.StopApplicationCommand;
import org.sonatype.sisu.jetty.Jetty8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static com.sanxing.sesame.bootstrap.monitor.CommandMonitorThread.LOCALHOST;
import static com.sanxing.sesame.bootstrap.monitor.KeepAliveThread.KEEP_ALIVE_PING_INTERVAL;
import static com.sanxing.sesame.bootstrap.monitor.KeepAliveThread.KEEP_ALIVE_PORT;
import static com.sanxing.sesame.bootstrap.monitor.KeepAliveThread.KEEP_ALIVE_TIMEOUT;

/**
 * Sesame bootstrap launcher.
 */
public class Launcher
{

    protected final Logger log;

    public static final String COMMAND_MONITOR_PORT = CommandMonitorThread.class.getName() + ".port";

    public static final String FIVE_SECONDS = "5000";

    public static final String ONE_SECOND = "1000";

    protected static final String BUNDLEBASEDIR_KEY = "bundleBasedir";

    protected static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    protected static final String SESAME_WORK = "sesame-work";

    protected Jetty8 server;

    protected Launcher() {
        Logger log = createLogger();
        if (log == null) {
            throw new NullPointerException();
        }
        this.log = log;
    }

    protected Logger createLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    public Integer start(final String[] args) throws Exception {
        if (args.length == 0) {
            log.error("Missing Jetty configuration file parameter");
            return 1; // exit
        }

        AppContext context = createAppContext();
        
        for (int i = 1; i < args.length; i++) {
        	log.debug(args[i]);
    		Class clazz = Launcher.class.getClassLoader().loadClass(args[i]);
    		Method main = clazz.getMethod("main", String[].class);
    		main.invoke(clazz, new Object[]{new String[0]});
        }

        server = new Jetty8(new File(args[0]), context);

        ensureTmpDirSanity();
        maybeEnableCommandMonitor();
        maybeEnableShutdownIfNotAlive();

        server.startJetty();
        return null; // continue running
    }

    /**
     * We have three properties file:
     *
     * default.properties -- embedded in this jar (not user editable)
     * this is the place to set java.io.tmp and debug options by users
     *
     * sesame.properties -- mandatory, will be picked up into context
     * this is place to set sesame properties like workdir location etc (as today)
     *
     * sesame-test.properties -- optional, if present, will override values from those above
     * this is place to set test properties (like jetty port) etc
     *
     * We push the whole app context into system properties, so that sesame[-test].properties
     * can be used to set any system properties (java.io.tmpdir, etc).
     */
    private AppContext createAppContext() throws Exception {
        File cwd = new File(".").getCanonicalFile();
        log.info("Current directory: {}", cwd);

        // create app context request, with ID "sesame", without parent, and due to SESEAME-4520 add "plexus" alias too
        final AppContextRequest request = Factory.getDefaultRequest("sesame", null, Arrays.asList("plexus"));

        // Kill the default logging publisher that is installed
        request.getPublishers().clear();

        // NOTE: sources list is "ascending by importance", 1st elem in list is "weakest" and last elem in list is
        // "strongest" (overrides). Factory already created us some sources, so we are just adding to that list without
        // disturbing the order of the list (we add to list head and tail)

        // Add the defaults as least important, is mandatory to be present
        addProperties(request, "defaults", "default.properties", true);

        // NOTE: These are loaded as resources, and its expected that <install>/conf is included in the classpath

        // Add the sesame.properties, is mandatory to be present
        addProperties(request, "sesame", "/sesame.properties", true);

        // Add the sesame-test.properties, not mandatory to be present
        addProperties(request, "sesame-test", "/sesame-test.properties", false);

        // Ultimate source of "bundleBasedir" (hence, is added as last in sources list)
        // Now, that will be always overridden by value got from cwd and that seems correct to me
        request.getSources().add(new StaticEntrySource(BUNDLEBASEDIR_KEY, cwd.getAbsolutePath()));

        // Install a publisher which will only log as TRACE (default version will log as DEBUG or INFO or WARN)
        request.getPublishers().add(new AbstractStringDumpingEntryPublisher()
        {
            @Override
            public void publishEntries(final AppContext context) {
                if (log.isTraceEnabled()) {
                    String dump = getDumpAsString(context);
                    log.trace("\n" + dump);
                }
            }
        });

        // we need to publish all entries coming from loaded properties
        request.getPublishers().add(new SystemPropertiesEntryPublisher(true));

        // create the context and use it as "parent" for Jetty8
        // when context created, the context is built and all publisher were invoked (system props set for example)
        AppContext context = Factory.create(request);

        // Make some entries canonical
        canonicalizeEntry(context, SESAME_WORK);

        if (log.isDebugEnabled()) {
            log.debug("Context:");
            for (Map.Entry<String,Object> entry : context.flatten().entrySet()) {
                log.debug("  {}='{}'", entry.getKey(), entry.getValue());
            }
        }

        return context;
    }

    protected void canonicalizeEntry(final AppContext context, final String key) throws IOException {
        if (!context.containsKey(key)) {
            log.warn("Unable to canonicalize missing entry: {}, key");
            return;
        }
        String value = String.valueOf(context.get(key));
        File file = new File(value).getCanonicalFile();
        value = file.getAbsolutePath();
        context.put(key, value);
    }

    protected Properties loadProperties(final Resource resource) throws IOException {
        assert resource != null;
        log.debug("Loading properties from: {}", resource);
        Properties props = new Properties();
        InputStream input = resource.getInputStream();
        try {
            props.load(input);
            if (log.isDebugEnabled()) {
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    log.debug("  {}='{}'", entry.getKey(), entry.getValue());
                }
            }
        }
        finally {
            input.close();
        }
        return props;
    }

    protected URL getResource(final String name) {
        // Now that Launcher is extend-able we'll need to load resources from common package
        return Launcher.class.getResource(name);
    }

    protected Properties loadProperties(final String resource, final boolean required) throws IOException {
        URL url = getResource( resource );
        if (url == null) {
            if (required) {
                log.error("Missing resource: {}", resource);
                throw new IOException("Missing resource: " + resource);
            }
            else {
                log.debug("Missing optional resource: {}", resource);
            }
            return null;
        }
        else {
            return loadProperties(Resource.newResource(url));
        }
    }

    protected void addProperties(final AppContextRequest request, final String name, final String resource, final boolean required) throws IOException {
        Properties props = loadProperties(resource, required);
        if (props != null) {
            request.getSources().add(new PropertiesEntrySource(name, props));
        }
    }

    protected void ensureTmpDirSanity() throws IOException {
        // Make sure that java.io.tmpdir points to a real directory
        String tmp = System.getProperty(JAVA_IO_TMPDIR, "tmp");
        File dir = new File(tmp).getCanonicalFile();
        log.info("Temp directory: {}", dir);

        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.debug("Created tmp dir: {}", dir);
            }
        }
        else if (!dir.isDirectory()) {
            log.warn("Tmp dir is configured to a location which is not a directory: {}", dir);
        }

        // Ensure we can actually create a new tmp file
        File file = File.createTempFile("sesame-launcher", ".tmp");
        file.createNewFile();
        file.delete();

        System.setProperty(JAVA_IO_TMPDIR, dir.getAbsolutePath());
    }

    protected void maybeEnableCommandMonitor() throws IOException {
        String commandMonitorPort = System.getProperty( COMMAND_MONITOR_PORT );
        if ( commandMonitorPort == null )
        {
            commandMonitorPort = System.getenv( COMMAND_MONITOR_PORT );
        }
        if (commandMonitorPort != null) {
            new CommandMonitorThread(
                Integer.parseInt( commandMonitorPort ),
                new StopApplicationCommand( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Launcher.this.commandStop();
                    }
                } ),
                new PingCommand(),
                new ExitCommand(),
                new HaltCommand()
            ).start();
        }
    }

    protected void maybeEnableShutdownIfNotAlive()
        throws IOException
    {
        String port = System.getProperty( KEEP_ALIVE_PORT );
        if ( port == null )
        {
            port = System.getenv( KEEP_ALIVE_PORT );
        }
        if ( port != null )
        {
            String pingInterval = System.getProperty( KEEP_ALIVE_PING_INTERVAL );
            if ( pingInterval == null )
            {
                pingInterval = System.getenv( KEEP_ALIVE_PING_INTERVAL );
                if ( pingInterval == null )
                {
                    pingInterval = FIVE_SECONDS;
                }
            }
            String timeout = System.getProperty( KEEP_ALIVE_TIMEOUT );
            if ( timeout == null )
            {
                timeout = System.getenv( KEEP_ALIVE_TIMEOUT );
                if ( timeout == null )
                {
                    timeout = ONE_SECOND;
                }
            }
            new KeepAliveThread(
                LOCALHOST,
                Integer.parseInt( port ),
                Integer.parseInt( pingInterval ),
                Integer.parseInt( timeout )
            ).start();
        }
    }

    public void commandStop() {
        ShutdownHelper.exit(0);
    }

    public void stop() throws Exception {
        server.stopJetty();
    }

    public static void main(final String[] args) throws Exception {
        new Launcher().start(args);
    }
}
