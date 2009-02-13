// $Id

package com.unwiredappeal.tivo.streambaby.host;

//////////////////////////////////////////////////////////////////////
//
// File: Listener.java
//
// Copyright (c) 2003-2005 TiVo Inc.
//
//////////////////////////////////////////////////////////////////////

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tivo.hme.host.http.server.HttpConnection;
import com.tivo.hme.host.http.server.HttpRequest;
import com.tivo.hme.host.http.server.HttpServer;
import com.tivo.hme.host.http.share.IHttpConstants;
import com.tivo.hme.host.http.share.Query;
import com.tivo.hme.host.io.FastInputStream;
import com.tivo.hme.host.io.FastOutputStream;
import com.tivo.hme.host.util.Config;
import com.tivo.hme.host.util.Cookies;
import com.tivo.hme.interfaces.IApplication;
import com.tivo.hme.interfaces.IFactory;
import com.tivo.hme.interfaces.IHmeConstants;
import com.tivo.hme.interfaces.IListener;
import com.tivo.hme.interfaces.ILogger;
import com.unwiredappeal.tivo.utils.Log;

// REMIND: handleException should be passed up to IMain to decide whether to
// display the stack trace or not.

// REMIND: Application.flush checking should not be used at all when running in
// server mode.  It's a debugging aid that is good for development in PC mode.


/**
 * A listener for HME/HTTP connections. The listener is an http server that
 * contains a set of factories to handle specific requests.
 * 
 * @author      Adam Doppelt
 * @author      Arthur van Hoff
 * @author      Brigham Stevens
 * @author      Jonathan Payne
 * @author      Steven Samorodin
 */
@SuppressWarnings(value = "unchecked")
public class Listener extends HttpServer implements IListener
{
    /**
     * If true, print debug output.
     */
    public static boolean DEBUG;

    public static  int READ_BUFFER_SIZE = IHttpConstants.TCP_BUFFER_SIZE;
    /**
     * If true, warn on missed flushes from non event loop threads.
     */
    public static boolean DEBUG_FLUSHES;

    /**
     * The time at which the Listener was started.
     */
    public static long tm0;

    /**
     * Name of the acceptor thread - see Application.writeTerminator().
     */
    public final static String ACCEPTOR_NAME = "Acceptor";

    Map factories = new HashMap();
    int nConnections;
    ILogger logger;
    
    /**
     * Create a listener on the default port (7288).
     */
    public Listener(Config config, ILogger logger) throws IOException
    {
        super(config);
        Log.debug("config=" + config + " logger=" + logger);

        this.logger = logger;

        DEBUG = config.getBool("listener.debug", false);
        tm0 = System.currentTimeMillis();
        
        config.put("http.acceptor.name", ACCEPTOR_NAME);

        start();
    }

    /**
     * Add an individual application factory to the listener.
     */
    public synchronized void add(IFactory factory)
    {
        Log.debug("factory=" + factory);
        String uri = factory.getAppName();
        if (uri == null) {
            throw new RuntimeException("uri is not set");
        }
        if (getFactory(uri) != null) {
            System.out.println("WARNING : " + uri + " already added.");
        }
        if (factory.getAppTitle() == null) {
            throw new RuntimeException("title is not set");         
        }
        factory.setListener(this);
        factory.getFactoryData().put(IFactory.HME_DEBUG_KEY, new Boolean(Listener.DEBUG));
        factories.put(uri, factory);
        getLogger().log(ILogger.LOG_NOTICE, "added factory");
    }

    /**
     * Remove a factory.
     */
    public synchronized void remove(IFactory factory)
    {
        Log.debug("factory=" + factory);
        //factory.log(LOG_NOTICE, "removed factory");
        getLogger().log(ILogger.LOG_NOTICE, "removed factory");
        factories.remove(factory.getAppName());
    }

    /**
     * Lookup an application factory by name.
     */
    public IFactory getFactory(String name)
    {
        Log.debug("name=" + name);
        return (IFactory)factories.get(name);    
    }

    /**
     * Lookup all the factories
     */
    public Map getFactories()
    {
        Log.debug("");
        return factories;
    }

    /**
     * Takes the list of new and existing factories and adds the new ones.  All
     * the existing factories that are NOT in the new factories list are marked
     * inactive and are removed as soon as there are no active connections.
     */
    public void setFactories(List factoryList)
    {
        Log.debug("factoryList=" + factoryList);
        // convert the array of factories to a map
        Map newFactories = new HashMap();
        for (Iterator i = factoryList.iterator(); i.hasNext(); ) {
            IFactory f = (IFactory) i.next();
            newFactories.put(f.getAppName(), f);
        }

        // figure out which apps are going inactive
        ArrayList inactive = new ArrayList();
        for (Iterator i = factories.values().iterator(); i.hasNext(); ) {
            IFactory f = (IFactory) i.next();
            if (newFactories.get(f.getAppName()) == null) {
                inactive.add(f);
            }
        }

        // Set those apps inactive - the ones which have no active connections
        // will be deleted from the factories hash immediately.
        for (Iterator i = inactive.iterator(); i.hasNext(); ) {
            IFactory f = (IFactory) i.next();
            f.setActive(false);
        }

        // now add all the new factories - some will already be there
        for (Iterator i = newFactories.values().iterator(); i.hasNext(); ) {
            IFactory f = (IFactory) i.next();

            // only add if not already registered
            if (factories.get(f.getAppName()) == null) {
                add(f);
            }

            // set active in case the factory was previously marked as inactive
            f.setActive(true);
        }
    }

    public int getTotalConnections()
    {
        Log.debug("");
        return nConnections;
    }

    /**
     * Print debugging output, with a timestamp.
     */
    public static void dprintln(String s)
    {
        Log.debug("s=" + s);
        System.out.println((System.currentTimeMillis() - tm0) + "ms: " + s);
    }
    
    protected void handleException(Object ctx, Throwable t)
    {
        Log.debug("ctx=" + ctx + " t=" + t);
        if (t instanceof SocketException) {
            // this is usually a side-effect of a real I/O exception like
            // "Connection reset by peer"
            return;
        }
    }

    private String getHostAddressWithPort(HttpRequest http) {
    	/* We might be behind a NAT, so use the host header to figure out our public IP address */
    	String host = http.get("host");
    	if (host != null && host.length() > 0) {
    		int index = host.indexOf(':');
    		if (index < 0) {
    			host = host + ":" + http.getPort();
    		}
    		return host;
    	}
    	return http.getInterface().getHostAddress() + ":" + http.getPort();
    }
    
    /**
     * Accept a HME connection.
     */
    void handleHME(HttpRequest http, IFactory factory) throws IOException
    {
        Log.debug("http=" + http + " factory=" + factory);
        HttpConnection connection = http.getConnection();
        InputStream in = connection.getInputStream();
        OutputStream out = connection.getOutputStream();
            
        // kill nagle
        Socket s = connection.getSocket();
        s.setTcpNoDelay(true);

        // get existing cookie
        String cookie = http.get("cookie");
        if (cookie != null) {
            cookie = (String)Cookies.parseCookie(cookie).get("id");
        }

        out.write("HTTP/1.1 200 Ok\r\n".getBytes());
        out.write(("Content-type: " + IHmeConstants.MIME_TYPE + "\r\n").getBytes());
        // make up a new cookie
        if (cookie == null) {
            cookie = Cookies.createRandomCookie();
            out.write(("Set-Cookie: " +
                      "id=" + cookie +
                      "; path=/" +
                      "; expires=Tue, 26-Apr-2022 19:00:00 GMT" +
                      "\r\n").getBytes());
        }
        out.write("\r\n".getBytes());
        
        URL uri = new URL("http://" + getHostAddressWithPort(http));
        String path = http.getURI();
        uri = new URL(uri, path);

        // create an application context
        HostContext context = new HostContext();
        context.factory = factory;
        context.request = http;
        context.uri = uri;
        context.base = new URL(uri, factory.getAppName());
        context.params = new Query(uri).getMap();
        context.s = s;
        
        if (!(in instanceof FastInputStream)) {
         in = new FastInputStream(in, TCP_BUFFER_SIZE);
        }
        context.in = in;

        if(!(out instanceof FastOutputStream)) {
         out = new FastOutputStream(out, TCP_BUFFER_SIZE);
        }
        context.out = out;
        context.cookie = cookie;
        context.logger = logger;
        context.appClassName = (String)factory.getFactoryData().get(IFactory.HME_APPLICATION_CLASSNAME);

        synchronized (this) {
            nConnections += 1;
        }

        // create the application
        try {
            long tm = System.currentTimeMillis();
            IApplication app = factory.createApplication(context);
            try {
                app.open(context);
                factory.addApplication(app);

                if (DEBUG) {
                    //dprintln("Listener: " + ((FastOutputStream)context.out).getCount() + " bytes to create " + app);
                    dprintln("Listener: time=" + (System.currentTimeMillis() - tm) + "ms");
                }

                readEvents(app, context.in);
                
            } finally {
                app.close();
            }
        } catch (EOFException e) {
            // ignore this - that app is closed
        } catch (Throwable t) {
            getLogger().log(ILogger.LOG_WARNING, "Unexpected error: " + t);
            Log.printStackTrace(t);
            if (Listener.DEBUG) {
            	if (t instanceof RuntimeException)
            		throw (RuntimeException)(t);
            	else
            		t.printStackTrace();
            }
        }
    }

    void readEvents(IApplication app, InputStream in) throws IOException
    {
        Log.debug("app=" + app + " in=" + in);
        try {            
            while (app.handleChunk(in))
                ;
        } 
        finally {
            getLogger().log(ILogger.LOG_NOTICE, "connection to receiver closed");
        }
    }

    void reply(HttpRequest http, int code, String message) throws IOException
    {
        Log.debug("http=" + http + " code=" + code + " message=" + message);
        getLogger().log(ILogger.LOG_NOTICE, http.getInetAddress().getHostAddress() + " " + http.getURI() + " HTTP " + http.getMethod() + " - " + code + " - " + message);
        http.reply(code, message);
    }

    boolean factoryCompare(String path, String uri)
    {
        Log.debug("path=" + path + " uri=" + uri);
        // If factory = "/foo/bar/baz/" then
        //   path = "/foo/bar/baz" => true
        //   path = "/foo/bar/baz/" => true
        //   path = "/foo/bar/baz?arg=val" => true
        //   path = "/foo/bar/baz/?arg=val" => true
        //   path = "/foo/bar/bazbaz" => false
        int urilen = uri.length();

        if (path.startsWith(uri.substring(0, urilen - 1))) {
            // same up to but not including the "/", now check the last
            // character for "/" or "?"
            if (path.length() >= urilen) {
                int ch = path.charAt(urilen - 1);
                if (ch != '/' && ch != '?') {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Handle an HTTP request.
     */
    public void handle(HttpRequest http) throws IOException
    {
        Log.debug("http=" + http);
        // find the application factory that should get the request
        IFactory factory = null;
        String path = http.getURI();
        for (Iterator i = factories.values().iterator(); i.hasNext(); ) {
            IFactory f = (IFactory) i.next();
            if (factoryCompare(path, f.getAppName())) {
                factory = f;
                break;
            }
        }
                
        if (factory == null) {
            reply(http, 404, "no application for: " + path);
            return;
        }

        // Note the getURI.length() - 1 below.  That's in case the original
        // URL is missing the trailing slash, e.g., http://host:port/bar?arg=val
        String relpath = URLDecoder.decode(path.substring(factory.getAppName().length() - 1), "UTF-8");

        // If the relative path is empty (except for query-style arguments) then
        // this is an application instantiation.  Otherwise, pass the relative
        // path to the factory so it can handle it.
        if (relpath.startsWith("/")) {
            relpath = relpath.substring(1);
        }
        if (relpath.length() == 0 || relpath.startsWith("?")) {
            try {
                if (!factory.isActive()) {
                    reply(http, 404, "application is not active for: " + path);
                    return;
                }
                handleHME(http, factory);
                
            } catch (IOException e) {
                // this happens whenever the HME receiver disconnects
            }
        } else {
            getLogger().log(ILogger.LOG_NOTICE, http.getInetAddress().getHostAddress() + " " + relpath + " HTTP " + http.getMethod() + " - to factory " + factory.getAppName());
            try {
                InputStream assetStr = factory.fetchAsset(http);
                try {
                  OutputStream out = http.getOutputStream(assetStr.available());
                  byte data[] = new byte[READ_BUFFER_SIZE];
                  int n;
                  while ((n = assetStr.read(data, 0, data.length)) > 0) {
                      out.write(data, 0, n);
                  }
              } finally {
                  assetStr.close();
              }
                
            } catch (IOException e) {
                getLogger().log(ILogger.LOG_NOTICE, http.getInetAddress().getHostAddress() + " I/O Exception handling " + " HTTP " + http.getMethod() + " " + relpath + ": " + e.getMessage());
            }
        }
    }

    /* (non-Javadoc)
     * @see com.tivo.hme.hosting.IListener#getLogger()
     */
    public ILogger getLogger() {
        Log.debug("");
        return logger;
    }
}
